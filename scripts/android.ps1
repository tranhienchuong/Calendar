[CmdletBinding()]
param(
    [Parameter(Mandatory)]
    [ValidateSet('build', 'unit-test', 'lint', 'device-check', 'install', 'launch', 'connected-test', 'gfxinfo')]
    [string]$Task,

    [string]$Serial,

    [switch]$AllowDeviceMutation
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$packageName = 'com.example.lichvannien'
$activityName = "$packageName/.MainActivity"
$debugApk = Join-Path $projectRoot 'app\build\outputs\apk\debug\app-debug.apk'
$testApk = Join-Path $projectRoot 'app\build\outputs\apk\androidTest\debug\app-debug-androidTest.apk'

function Get-AdbPath {
    $candidates = @()
    if ($env:ANDROID_SDK_ROOT) {
        $candidates += Join-Path $env:ANDROID_SDK_ROOT 'platform-tools\adb.exe'
    }
    if ($env:ANDROID_HOME) {
        $candidates += Join-Path $env:ANDROID_HOME 'platform-tools\adb.exe'
    }

    $localProperties = Join-Path $projectRoot 'local.properties'
    if (Test-Path -LiteralPath $localProperties) {
        $sdkLine = Get-Content -LiteralPath $localProperties |
            Where-Object { $_ -match '^sdk\.dir=' } |
            Select-Object -First 1
        if ($sdkLine) {
            $sdkRoot = ($sdkLine -replace '^sdk\.dir=', '').Trim()
            $sdkRoot = $sdkRoot.Replace('\:', ':').Replace('\\', '\')
            $candidates += Join-Path $sdkRoot 'platform-tools\adb.exe'
        }
    }

    foreach ($candidate in $candidates | Select-Object -Unique) {
        if (Test-Path -LiteralPath $candidate) {
            return (Resolve-Path -LiteralPath $candidate).Path
        }
    }

    throw 'Cannot find adb.exe. Set ANDROID_SDK_ROOT or add sdk.dir to local.properties.'
}

function Invoke-Checked {
    param(
        [Parameter(Mandatory)] [string]$FilePath,
        [Parameter()] [string[]]$Arguments
    )

    & $FilePath @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "Command failed with exit code ${LASTEXITCODE}: $FilePath $($Arguments -join ' ')"
    }
}

function Invoke-Gradle {
    param([Parameter(Mandatory)] [string[]]$Arguments)

    $gradle = Join-Path $projectRoot 'gradlew.bat'
    if (-not (Test-Path -LiteralPath $gradle)) {
        throw "Gradle wrapper not found at $gradle"
    }

    Push-Location $projectRoot
    try {
        Invoke-Checked -FilePath $gradle -Arguments $Arguments
    }
    finally {
        Pop-Location
    }
}

function Get-PhysicalSerial {
    param([Parameter(Mandatory)] [string]$AdbPath)

    if ($Serial -and $Serial.StartsWith('emulator-', [System.StringComparison]::OrdinalIgnoreCase)) {
        throw "Refusing emulator serial '$Serial'. Connect a physical test device instead."
    }

    $deviceLines = & $AdbPath devices
    if ($LASTEXITCODE -ne 0) {
        throw 'adb devices failed.'
    }

    $physicalSerials = @(
        $deviceLines |
            Where-Object { $_ -match '^(\S+)\s+device$' } |
            ForEach-Object { ($_.Trim() -split '\s+')[0] } |
            Where-Object { -not $_.StartsWith('emulator-', [System.StringComparison]::OrdinalIgnoreCase) }
    )

    if ($Serial) {
        if ($physicalSerials -notcontains $Serial) {
            throw "Physical device '$Serial' is not connected and authorized. Run adb devices and unlock/authorize the device."
        }
        return $Serial
    }

    if ($physicalSerials.Count -eq 0) {
        throw 'No authorized physical device is connected. Enable USB debugging, unlock the device, accept the RSA prompt, then run adb devices.'
    }
    if ($physicalSerials.Count -gt 1) {
        throw "Multiple physical devices are connected: $($physicalSerials -join ', '). Re-run with -Serial <serial>."
    }

    return $physicalSerials[0]
}

function Require-DeviceMutationApproval {
    if (-not $AllowDeviceMutation) {
        throw 'This task installs or runs tests on the device. Re-run with -AllowDeviceMutation only after user authorization.'
    }
}

switch ($Task) {
    'build' {
        Invoke-Gradle -Arguments @(':app:assembleDebug', '--no-daemon')
        if (-not (Test-Path -LiteralPath $debugApk)) {
            throw "Build completed without the expected APK: $debugApk"
        }
        Write-Output "Debug APK: $debugApk"
    }
    'unit-test' {
        Invoke-Gradle -Arguments @(':app:testDebugUnitTest', '--no-daemon')
    }
    'lint' {
        Invoke-Gradle -Arguments @(':app:lintDebug', '--no-daemon')
        Write-Output "Lint report: $(Join-Path $projectRoot 'app\build\reports\lint-results-debug.html')"
    }
    'device-check' {
        $adb = Get-AdbPath
        $targetSerial = Get-PhysicalSerial -AdbPath $adb
        Invoke-Checked -FilePath $adb -Arguments @('-s', $targetSerial, 'get-state')
        $model = (& $adb -s $targetSerial shell getprop ro.product.model).Trim()
        $androidVersion = (& $adb -s $targetSerial shell getprop ro.build.version.release).Trim()
        Write-Output "Device: $targetSerial"
        Write-Output "Model: $model"
        Write-Output "Android: $androidVersion"
    }
    'install' {
        Require-DeviceMutationApproval
        $adb = Get-AdbPath
        $targetSerial = Get-PhysicalSerial -AdbPath $adb
        Invoke-Gradle -Arguments @(':app:assembleDebug', '--no-daemon')
        Invoke-Checked -FilePath $adb -Arguments @('-s', $targetSerial, 'install', '-r', $debugApk)
    }
    'launch' {
        $adb = Get-AdbPath
        $targetSerial = Get-PhysicalSerial -AdbPath $adb
        Invoke-Checked -FilePath $adb -Arguments @('-s', $targetSerial, 'shell', 'am', 'start', '-W', '-n', $activityName)
    }
    'connected-test' {
        Require-DeviceMutationApproval
        $adb = Get-AdbPath
        $targetSerial = Get-PhysicalSerial -AdbPath $adb
        Invoke-Gradle -Arguments @(':app:assembleDebug', ':app:assembleDebugAndroidTest', '--no-daemon')
        if (-not (Test-Path -LiteralPath $testApk)) {
            throw "Instrumented-test APK not found: $testApk"
        }
        Invoke-Checked -FilePath $adb -Arguments @('-s', $targetSerial, 'install', '-r', $debugApk)
        Invoke-Checked -FilePath $adb -Arguments @('-s', $targetSerial, 'install', '-r', '-t', $testApk)
        Invoke-Checked -FilePath $adb -Arguments @('-s', $targetSerial, 'shell', 'am', 'instrument', '-w', '-r', "$packageName.test/androidx.test.runner.AndroidJUnitRunner")
    }
    'gfxinfo' {
        $adb = Get-AdbPath
        $targetSerial = Get-PhysicalSerial -AdbPath $adb
        Invoke-Checked -FilePath $adb -Arguments @('-s', $targetSerial, 'shell', 'dumpsys', 'gfxinfo', $packageName)
    }
}
