[CmdletBinding()]
param(
    [Parameter(Mandatory)]
    [ValidateSet('compile', 'build', 'unit-test', 'test-class', 'lint', 'verify', 'device-check', 'install', 'launch', 'connected-test', 'gfxinfo', 'macrobenchmark-compile', 'macrobenchmark-build', 'macrobenchmark')]
    [string]$Task,

    [string]$Serial,

    [string[]]$Tests,

    [switch]$AllowDeviceMutation
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
. (Join-Path $PSScriptRoot 'lib\android-device.ps1')
$packageName = 'com.example.lichvannien'
$activityName = "$packageName/.MainActivity"
$debugApk = Join-Path $projectRoot 'app\build\outputs\apk\debug\app-debug.apk'
$testApk = Join-Path $projectRoot 'app\build\outputs\apk\androidTest\debug\app-debug-androidTest.apk'

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

function Require-DeviceMutationApproval {
    if (-not $AllowDeviceMutation) {
        throw 'This task installs or runs tests on the device. Re-run with -AllowDeviceMutation only after user authorization.'
    }
}

switch ($Task) {
    'compile' {
        Invoke-Gradle -Arguments @(':app:compileDebugKotlin')
    }
    'build' {
        Invoke-Gradle -Arguments @(':app:assembleDebug')
        if (-not (Test-Path -LiteralPath $debugApk)) {
            throw "Build completed without the expected APK: $debugApk"
        }
        Write-Output "Debug APK: $debugApk"
    }
    'unit-test' {
        Invoke-Gradle -Arguments @(':app:testDebugUnitTest')
    }
    'test-class' {
        if ($Tests.Count -eq 0) {
            throw 'test-class requires at least one fully qualified Gradle test pattern via -Tests.'
        }
        $testArguments = @(':app:testDebugUnitTest')
        foreach ($testPattern in $Tests) {
            $testArguments += '--tests'
            $testArguments += $testPattern
        }
        Invoke-Gradle -Arguments $testArguments
    }
    'lint' {
        Invoke-Gradle -Arguments @(':app:lintDebug')
        Write-Output "Lint report: $(Join-Path $projectRoot 'app\build\reports\lint-results-debug.html')"
    }
    'verify' {
        Invoke-Gradle -Arguments @(':app:testDebugUnitTest', ':app:lintDebug')
        Write-Output "Lint report: $(Join-Path $projectRoot 'app\build\reports\lint-results-debug.html')"
        Write-Output "Unit-test report: $(Join-Path $projectRoot 'app\build\reports\tests\testDebugUnitTest\index.html')"
    }
    'device-check' {
        $adb = Get-AndroidAdbPath -ProjectRoot $projectRoot
        $device = Get-AuthorizedPhysicalAndroidDevice -AdbPath $adb -Serial $Serial
        Write-Output "Device: $($device.Serial)"
        Write-Output "Model: $($device.Model)"
        Write-Output "Android: $($device.Android)"
    }
    'install' {
        Require-DeviceMutationApproval
        $adb = Get-AndroidAdbPath -ProjectRoot $projectRoot
        $targetSerial = (Get-AuthorizedPhysicalAndroidDevice -AdbPath $adb -Serial $Serial).Serial
        Invoke-Gradle -Arguments @(':app:assembleDebug')
        Invoke-Checked -FilePath $adb -Arguments @('-s', $targetSerial, 'install', '-r', $debugApk)
    }
    'launch' {
        $adb = Get-AndroidAdbPath -ProjectRoot $projectRoot
        $targetSerial = (Get-AuthorizedPhysicalAndroidDevice -AdbPath $adb -Serial $Serial).Serial
        [void](Start-AndroidActivity -AdbPath $adb -Serial $targetSerial -ActivityName $activityName)
    }
    'connected-test' {
        Require-DeviceMutationApproval
        $adb = Get-AndroidAdbPath -ProjectRoot $projectRoot
        $targetSerial = (Get-AuthorizedPhysicalAndroidDevice -AdbPath $adb -Serial $Serial).Serial
        Invoke-Gradle -Arguments @(':app:assembleDebug', ':app:assembleDebugAndroidTest')
        if (-not (Test-Path -LiteralPath $testApk)) {
            throw "Instrumented-test APK not found: $testApk"
        }
        Invoke-Checked -FilePath $adb -Arguments @('-s', $targetSerial, 'install', '-r', $debugApk)
        Invoke-Checked -FilePath $adb -Arguments @('-s', $targetSerial, 'install', '-r', '-t', $testApk)
        Invoke-Checked -FilePath $adb -Arguments @('-s', $targetSerial, 'shell', 'am', 'instrument', '-w', '-r', "$packageName.test/androidx.test.runner.AndroidJUnitRunner")
    }
    'gfxinfo' {
        $adb = Get-AndroidAdbPath -ProjectRoot $projectRoot
        $targetSerial = (Get-AuthorizedPhysicalAndroidDevice -AdbPath $adb -Serial $Serial).Serial
        Invoke-Checked -FilePath $adb -Arguments @('-s', $targetSerial, 'shell', 'dumpsys', 'gfxinfo', $packageName)
    }
    'macrobenchmark-build' {
        Invoke-Gradle -Arguments @(':app:assembleBenchmark', ':benchmark:assembleBenchmark')
    }
    'macrobenchmark-compile' {
        Invoke-Gradle -Arguments @(':benchmark:compileBenchmarkKotlin')
    }
    'macrobenchmark' {
        Require-DeviceMutationApproval
        $adb = Get-AndroidAdbPath -ProjectRoot $projectRoot
        $targetSerial = (Get-AuthorizedPhysicalAndroidDevice -AdbPath $adb -Serial $Serial).Serial
        Assert-OnlySelectedAndroidDeviceConnected -AdbPath $adb -Serial $targetSerial
        Invoke-Gradle -Arguments @(':benchmark:connectedBenchmarkAndroidTest')
    }
}
