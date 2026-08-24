Set-StrictMode -Version Latest

function Get-AndroidAdbPath {
    param(
        [Parameter(Mandatory = $true)]
        [string]$ProjectRoot
    )

    $candidates = @()
    if ($env:ANDROID_SDK_ROOT) {
        $candidates += Join-Path $env:ANDROID_SDK_ROOT 'platform-tools\adb.exe'
    }
    if ($env:ANDROID_HOME) {
        $candidates += Join-Path $env:ANDROID_HOME 'platform-tools\adb.exe'
    }

    $localProperties = Join-Path $ProjectRoot 'local.properties'
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

function Invoke-AndroidAdb {
    param(
        [Parameter(Mandatory = $true)]
        [string]$AdbPath,

        [Parameter(Mandatory = $true)]
        [string[]]$Arguments
    )

    $prevEap = $ErrorActionPreference
    try {
        $ErrorActionPreference = 'Continue'
        $commandOutput = & $AdbPath @Arguments 2>&1
        $exitCode = $LASTEXITCODE
    } finally {
        $ErrorActionPreference = $prevEap
    }
    $output = (@($commandOutput | ForEach-Object { $_.ToString() })) -join [Environment]::NewLine

    if ($exitCode -ne 0) {
        $details = $output.Trim()
        if ($details.Length -gt 1000) {
            $details = $details.Substring(0, 1000)
        }
        throw "ADB command failed with exit code ${exitCode}: adb $($Arguments -join ' ')`n$details"
    }

    return $output
}

function Get-AndroidDeviceRows {
    param(
        [Parameter(Mandatory = $true)]
        [string]$AdbPath
    )

    $devicesOutput = Invoke-AndroidAdb -AdbPath $AdbPath -Arguments @('devices', '-l')
    foreach ($line in ($devicesOutput -split '\r?\n')) {
        if ($line -match '^\s*List of devices attached\s*$') {
            continue
        }
        if ($line -match '^\s*(?<deviceSerial>\S+)\s+(?<deviceState>\S+)(?:\s|$)') {
            [pscustomobject]@{
                Serial = $Matches.deviceSerial
                State = $Matches.deviceState
            }
        }
    }
}

function Get-AuthorizedPhysicalAndroidDevice {
    param(
        [Parameter(Mandatory = $true)]
        [string]$AdbPath,

        [string]$Serial
    )

    if ($Serial -and $Serial.StartsWith('emulator-', [System.StringComparison]::OrdinalIgnoreCase)) {
        throw "Refusing emulator serial '$Serial'. Connect a physical test device instead."
    }

    $rows = @(Get-AndroidDeviceRows -AdbPath $AdbPath)
    if ($Serial) {
        $target = $rows | Where-Object { $_.Serial -ceq $Serial } | Select-Object -First 1
        if (-not $target) {
            $knownSerials = @($rows | ForEach-Object { $_.Serial })
            if ($knownSerials.Count -eq 0) {
                throw "ADB serial '$Serial' is not connected. Unlock and authorize the physical device."
            }
            throw "ADB serial '$Serial' is not connected. Connected serials: $($knownSerials -join ', ')."
        }
        if ($target.State -ne 'device') {
            throw "ADB serial '$Serial' is not ready (state: $($target.State)). Unlock the physical device and accept the USB debugging prompt."
        }
    }
    else {
        $physicalRows = @(
            $rows | Where-Object {
                $_.State -eq 'device' -and -not $_.Serial.StartsWith('emulator-', [System.StringComparison]::OrdinalIgnoreCase)
            }
        )
        if ($physicalRows.Count -eq 0) {
            throw 'No authorized physical device is connected. Enable USB debugging, unlock the device, accept the RSA prompt, then run adb devices.'
        }
        if ($physicalRows.Count -gt 1) {
            throw "Multiple physical devices are connected: $($physicalRows.Serial -join ', '). Re-run with -Serial <serial>."
        }
        $target = $physicalRows[0]
        $Serial = $target.Serial
    }

    $state = (Invoke-AndroidAdb -AdbPath $AdbPath -Arguments @('-s', $Serial, 'get-state')).Trim()
    if ($state -ne 'device') {
        throw "ADB serial '$Serial' did not report device state: '$state'."
    }

    $qemu = (Invoke-AndroidAdb -AdbPath $AdbPath -Arguments @('-s', $Serial, 'shell', 'getprop', 'ro.kernel.qemu')).Trim()
    $bootQemu = (Invoke-AndroidAdb -AdbPath $AdbPath -Arguments @('-s', $Serial, 'shell', 'getprop', 'ro.boot.qemu')).Trim()
    if ($qemu -match '(?i)^(1|true)$' -or $bootQemu -match '(?i)^(1|true)$') {
        throw "Refusing virtual device '$Serial' (qemu property is enabled). Use a physical Android device."
    }

    return [pscustomobject]@{
        Serial = $Serial
        Model = (Invoke-AndroidAdb -AdbPath $AdbPath -Arguments @('-s', $Serial, 'shell', 'getprop', 'ro.product.model')).Trim()
        Android = (Invoke-AndroidAdb -AdbPath $AdbPath -Arguments @('-s', $Serial, 'shell', 'getprop', 'ro.build.version.release')).Trim()
    }
}

function Assert-OnlySelectedAndroidDeviceConnected {
    param(
        [Parameter(Mandatory = $true)]
        [string]$AdbPath,

        [Parameter(Mandatory = $true)]
        [string]$Serial
    )

    $readySerials = @(
        Get-AndroidDeviceRows -AdbPath $AdbPath |
            Where-Object { $_.State -eq 'device' } |
            ForEach-Object { $_.Serial }
    )
    if ($readySerials.Count -ne 1 -or $readySerials[0] -cne $Serial) {
        throw "Benchmark execution requires only the selected physical device to be ready. Ready devices: $($readySerials -join ', ')."
    }
}

function Assert-AndroidPackageInstalled {
    param(
        [Parameter(Mandatory = $true)]
        [string]$AdbPath,

        [Parameter(Mandatory = $true)]
        [string]$Serial,

        [Parameter(Mandatory = $true)]
        [string]$PackageName
    )

    $packagePath = Invoke-AndroidAdb -AdbPath $AdbPath -Arguments @('-s', $Serial, 'shell', 'pm', 'path', $PackageName)
    if ($packagePath -notmatch '(?m)^package:') {
        throw "Package '$PackageName' is not installed on '$Serial'."
    }
}

function Start-AndroidActivity {
    param(
        [Parameter(Mandatory = $true)]
        [string]$AdbPath,

        [Parameter(Mandatory = $true)]
        [string]$Serial,

        [Parameter(Mandatory = $true)]
        [string]$ActivityName
    )

    $startOutput = Invoke-AndroidAdb -AdbPath $AdbPath -Arguments @('-s', $Serial, 'shell', 'am', 'start', '-W', '-n', $ActivityName)
    if ($startOutput -match '(?im)^(?:Error|Exception|Unable to start)') {
        throw "Could not open $ActivityName on '$Serial'.`n$startOutput"
    }

    return $startOutput
}
