<#
.SYNOPSIS
    Measures repeated CalendarScreen month swipes and applies the Phase 1 gate.

.DESCRIPTION
    The script only accepts an authorized physical ADB device. It launches the
    existing app (without installing, clearing data, or changing animation
    scales), resets gfxinfo, performs deterministic swipes inside the calendar
    card, verifies the expected month after every swipe, and parses
    gfxinfo/frame stats.

    Left means next month. Right means previous month.
    Parsed results are written to the system temporary directory by default;
    no raw device dump is persisted.

.EXAMPLE
    powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\measure-calendar-swipe.ps1 -Serial <serial>

.EXAMPLE
    powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\measure-calendar-swipe.ps1 -Serial <serial> -Count 10 -Direction Right -DurationMs 300
#>

[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [ValidateNotNullOrEmpty()]
    [string]$Serial,

    [ValidateRange(1, 1000)]
    [int]$Count = 10,

    [ValidateSet('Left', 'Right')]
    [string]$Direction = 'Left',

    [ValidateRange(1, 5000)]
    [int]$DurationMs = 300,

    [string]$OutputPath
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$projectRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '..')).Path
. (Join-Path $PSScriptRoot 'lib\android-device.ps1')
$packageName = 'com.example.lichvannien'
$activityName = "$packageName/.MainActivity"
$invariantCulture = [System.Globalization.CultureInfo]::InvariantCulture
$remoteDumpPath = "/data/local/tmp/calendar-swipe-$PID-$([Guid]::NewGuid().ToString('N')).xml"

if ([string]::IsNullOrWhiteSpace($Serial)) {
    throw '-Serial must not be empty.'
}

function Invoke-Adb {
    param(
        [Parameter(Mandatory = $true)]
        [string[]]$Arguments
    )

    return Invoke-AndroidAdb -AdbPath $script:adbPath -Arguments $Arguments
}

function Assert-AppInstalled {
    Assert-AndroidPackageInstalled -AdbPath $script:adbPath -Serial $Serial -PackageName $packageName
}

function Start-CalendarActivity {
    [void](Start-AndroidActivity -AdbPath $script:adbPath -Serial $Serial -ActivityName $activityName)
}

function Get-UiHierarchy {
    try {
        [void](Invoke-Adb -Arguments @('-s', $Serial, 'shell', 'uiautomator', 'dump', '--compressed', $remoteDumpPath))
        return (Invoke-Adb -Arguments @('-s', $Serial, 'shell', 'cat', $remoteDumpPath))
    }
    finally {
        try {
            [void](Invoke-Adb -Arguments @('-s', $Serial, 'shell', 'rm', '-f', $remoteDumpPath))
        }
        catch {
            # The dump is a disposable file in /data/local/tmp. Cleanup failure
            # must not hide the measurement result.
        }
    }
}

function Try-GetCalendarHeader {
    try {
        $hierarchy = Get-UiHierarchy
        $headerMatch = [regex]::Match(
            $hierarchy,
            '(?i)Tháng\s+(?<month>\d{1,2})\s*,\s*(?<year>\d{4})'
        )
        if (-not $headerMatch.Success) {
            return $null
        }

        return [pscustomobject]@{
            Month = [int]$headerMatch.Groups['month'].Value
            Year = [int]$headerMatch.Groups['year'].Value
        }
    }
    catch {
        return $null
    }
}

function Test-SameMonthHeader {
    param(
        [object]$LeftHeader,
        [object]$RightHeader
    )

    return ($null -ne $LeftHeader -and $null -ne $RightHeader -and
        $LeftHeader.Month -eq $RightHeader.Month -and $LeftHeader.Year -eq $RightHeader.Year)
}

function Format-MonthHeader {
    param([object]$Header)

    if ($null -eq $Header) {
        return 'NOT FOUND'
    }
    return ('Tháng {0}, {1}' -f $Header.Month, $Header.Year)
}

function Wait-CalendarHeader {
    param(
        [int]$TimeoutMs = 15000,
        [object]$ExpectedHeader
    )

    $deadline = [DateTime]::UtcNow.AddMilliseconds($TimeoutMs)
    $lastHeader = $null

    do {
        $candidate = Try-GetCalendarHeader
        if ($candidate) {
            $lastHeader = $candidate
            if ($null -eq $ExpectedHeader -or (Test-SameMonthHeader -LeftHeader $candidate -RightHeader $ExpectedHeader)) {
                return $candidate
            }
        }

        if ([DateTime]::UtcNow -ge $deadline) {
            break
        }
        Start-Sleep -Milliseconds 250
    } while ($true)

    return $lastHeader
}

function Get-ScreenSize {
    $wmOutput = Invoke-Adb -Arguments @('-s', $Serial, 'shell', 'wm', 'size')
    $sizeMatches = [regex]::Matches(
        $wmOutput,
        '(?im)(?:Physical|Override)\s+size:\s*(?<width>\d+)x(?<height>\d+)'
    )
    if ($sizeMatches.Count -eq 0) {
        throw "Could not determine screen size from wm size on '$Serial'."
    }

    $sizeMatch = $sizeMatches[$sizeMatches.Count - 1]
    return [pscustomobject]@{
        Width = [int]$sizeMatch.Groups['width'].Value
        Height = [int]$sizeMatch.Groups['height'].Value
    }
}

function Get-MetricNumber {
    param(
        [Parameter(Mandatory = $true)] [string]$Text,
        [Parameter(Mandatory = $true)] [string]$Pattern
    )

    $match = [regex]::Match(
        $Text,
        $Pattern,
        ([System.Text.RegularExpressions.RegexOptions]::IgnoreCase -bor [System.Text.RegularExpressions.RegexOptions]::Multiline)
    )
    if (-not $match.Success) {
        return $null
    }

    return [double]::Parse($match.Groups['value'].Value.Replace(',', '.'), $invariantCulture)
}

function Get-GpuPercentile {
    param(
        [Parameter(Mandatory = $true)] [string]$Text,
        [Parameter(Mandatory = $true)] [int]$Percentile
    )

    $directPattern = "^\s*${Percentile}th\s+gpu\s+percentile:\s*(?<value>\d+(?:\.\d+)?)ms\s*$"
    $direct = Get-MetricNumber -Text $Text -Pattern $directPattern
    if ($null -ne $direct) {
        return $direct
    }

    # Accept platform variants that put GPU completion percentiles under a
    # titled section instead of using the AOSP "95th gpu percentile" label.
    $lines = @($Text -split '\r?\n')
    for ($lineIndex = 0; $lineIndex -lt $lines.Count; $lineIndex++) {
        if ($lines[$lineIndex] -notmatch '(?i)gpu.*(?:completion|percentile)') {
            continue
        }

        $lastLine = [Math]::Min($lineIndex + 12, $lines.Count - 1)
        for ($candidateIndex = $lineIndex; $candidateIndex -le $lastLine; $candidateIndex++) {
            $candidatePattern = "^\s*${Percentile}th\s+percentile:\s*(?<value>\d+(?:\.\d+)?)ms\s*$"
            $candidate = Get-MetricNumber -Text $lines[$candidateIndex] -Pattern $candidatePattern
            if ($null -ne $candidate) {
                return $candidate
            }
        }
    }

    return $null
}

function Get-FrameStatsOverThreshold {
    param(
        [Parameter(Mandatory = $true)] [string]$FrameStats,
        [Parameter(Mandatory = $true)] [int]$TotalFrames,
        [int]$ThresholdMs = 50
    )

    $lines = @($FrameStats -split '\r?\n')
    $headerIndex = -1
    $headers = $null

    for ($lineIndex = 0; $lineIndex -lt $lines.Count; $lineIndex++) {
        if ($lines[$lineIndex] -match '(?i)^\s*Flags\s*,.*\bIntendedVsync\b.*\bFrameCompleted\b') {
            $headerIndex = $lineIndex
            $headers = @($lines[$lineIndex].Trim() -split ',' | ForEach-Object { $_.Trim() })
            break
        }
    }

    if ($headerIndex -lt 0) {
        return [pscustomobject]@{
            Available = $false
            Count = $null
            ParsedFrames = 0
            MaxMs = $null
            Reason = 'framestats CSV header not found'
        }
    }

    $intendedIndex = -1
    $completedIndex = -1
    for ($headerColumn = 0; $headerColumn -lt $headers.Count; $headerColumn++) {
        if ($headers[$headerColumn] -ieq 'IntendedVsync') {
            $intendedIndex = $headerColumn
        }
        if ($headers[$headerColumn] -ieq 'FrameCompleted') {
            $completedIndex = $headerColumn
        }
    }

    if ($intendedIndex -lt 0 -or $completedIndex -lt 0) {
        return [pscustomobject]@{
            Available = $false
            Count = $null
            ParsedFrames = 0
            MaxMs = $null
            Reason = 'framestats is missing IntendedVsync or FrameCompleted'
        }
    }

    $parsedFrames = 0
    $overThreshold = 0
    $maxMs = 0.0

    for ($lineIndex = $headerIndex + 1; $lineIndex -lt $lines.Count; $lineIndex++) {
        $line = $lines[$lineIndex].Trim()
        if ([string]::IsNullOrWhiteSpace($line) -or $line.StartsWith('---')) {
            continue
        }

        $values = @($line -split ',')
        $requiredIndex = [Math]::Max($intendedIndex, $completedIndex)
        if ($values.Count -le $requiredIndex) {
            continue
        }

        $intendedValue = $values[$intendedIndex].Trim()
        $completedValue = $values[$completedIndex].Trim()
        if ($intendedValue -notmatch '^-?\d+$' -or $completedValue -notmatch '^-?\d+$') {
            continue
        }

        $intendedNs = [Int64]::Parse($intendedValue, $invariantCulture)
        $completedNs = [Int64]::Parse($completedValue, $invariantCulture)
        if ($intendedNs -le 0 -or $completedNs -le 0 -or $completedNs -lt $intendedNs) {
            continue
        }

        $parsedFrames++
        $durationMs = [double]($completedNs - $intendedNs) / 1000000.0
        if ($durationMs -ge $ThresholdMs) {
            $overThreshold++
        }
        if ($durationMs -gt $maxMs) {
            $maxMs = $durationMs
        }
    }

    $complete = $parsedFrames -gt 0 -and ($TotalFrames -le 0 -or $parsedFrames -ge $TotalFrames)
    return [pscustomobject]@{
        Available = $complete
        Count = if ($complete) { $overThreshold } else { $null }
        ParsedFrames = $parsedFrames
        MaxMs = if ($complete) { $maxMs } else { $null }
        Reason = if ($complete) { $null } else { "parsed $parsedFrames frame(s), expected at least $TotalFrames" }
    }
}

function Resolve-ExternalOutputPath {
    param([string]$RequestedPath)

    if ([string]::IsNullOrWhiteSpace($RequestedPath)) {
        $fileName = 'calendar-swipe-{0}.json' -f [DateTime]::UtcNow.ToString('yyyyMMdd-HHmmss-fff')
        $candidate = Join-Path ([IO.Path]::GetTempPath()) $fileName
    }
    else {
        if ([IO.Path]::IsPathRooted($RequestedPath)) {
            $candidate = $RequestedPath
        }
        else {
            $candidate = Join-Path (Get-Location).Path $RequestedPath
        }
    }

    $candidate = [IO.Path]::GetFullPath($candidate)
    $root = [IO.Path]::GetFullPath($projectRoot).TrimEnd([char[]]@('\', '/'))
    $rootPrefix = "$root$([IO.Path]::DirectorySeparatorChar)"
    if ([StringComparer]::OrdinalIgnoreCase.Equals($candidate, $root) -or $candidate.StartsWith($rootPrefix, [StringComparison]::OrdinalIgnoreCase)) {
        throw "OutputPath must be outside the source tree: $projectRoot"
    }

    return $candidate
}

function Format-Milliseconds {
    param([object]$Value)

    if ($null -eq $Value) {
        return 'N/A'
    }
    return (([double]$Value).ToString('0.##', $invariantCulture) + ' ms')
}

$script:adbPath = Get-AndroidAdbPath -ProjectRoot $projectRoot
$resultPath = Resolve-ExternalOutputPath -RequestedPath $OutputPath
$targetDevice = Get-AuthorizedPhysicalAndroidDevice -AdbPath $script:adbPath -Serial $Serial
Assert-AppInstalled
Start-CalendarActivity

$initialHeader = Wait-CalendarHeader -TimeoutMs 15000
if (-not $initialHeader) {
    throw "CalendarScreen header was not found on '$Serial'. Complete onboarding and leave the app on CalendarScreen; the script does not clear app data."
}

$directionStep = if ($Direction -eq 'Left') { 1 } else { -1 }
$initialDate = [DateTime]::new($initialHeader.Year, $initialHeader.Month, 1)
$expectedFinalDate = $initialDate.AddMonths($directionStep * $Count)
$expectedFinalHeader = [pscustomobject]@{
    Month = $expectedFinalDate.Month
    Year = $expectedFinalDate.Year
}

$screenSize = Get-ScreenSize
$swipeY = [int][Math]::Round($screenSize.Height * 0.55)
$leftX = [int][Math]::Round($screenSize.Width * 0.20)
$rightX = [int][Math]::Round($screenSize.Width * 0.80)
$settleMs = [Math]::Max(150, [Math]::Min(500, [int][Math]::Round($DurationMs * 0.5)))

[void](Invoke-Adb -Arguments @('-s', $Serial, 'shell', 'dumpsys', 'gfxinfo', $packageName, 'reset'))
Start-Sleep -Milliseconds 100

$transitions = [System.Collections.Generic.List[object]]::new()
for ($swipeIndex = 1; $swipeIndex -le $Count; $swipeIndex++) {
    if ($Direction -eq 'Left') {
        $startX = $rightX
        $endX = $leftX
    }
    else {
        $startX = $leftX
        $endX = $rightX
    }

    [void](Invoke-Adb -Arguments @(
            '-s', $Serial,
            'shell',
            'input',
            'swipe',
            $startX.ToString(),
            $swipeY.ToString(),
            $endX.ToString(),
            $swipeY.ToString(),
            $DurationMs.ToString()
        ))
    Start-Sleep -Milliseconds $settleMs

    $expectedDate = $initialDate.AddMonths($directionStep * $swipeIndex)
    $expectedHeader = [pscustomobject]@{
        Month = $expectedDate.Month
        Year = $expectedDate.Year
    }
    $actualHeader = Wait-CalendarHeader -TimeoutMs ([Math]::Max(5000, $settleMs * 4)) -ExpectedHeader $expectedHeader
    $transitions.Add([pscustomobject]@{
            Index = $swipeIndex
            Expected = Format-MonthHeader $expectedHeader
            Actual = Format-MonthHeader $actualHeader
            Pass = [bool](Test-SameMonthHeader -LeftHeader $actualHeader -RightHeader $expectedHeader)
        })
}

$finalTransition = if ($transitions.Count -gt 0) { $transitions[$transitions.Count - 1] } else { $null }

$gfxInfo = Invoke-Adb -Arguments @('-s', $Serial, 'shell', 'dumpsys', 'gfxinfo', $packageName)
$frameStats = Invoke-Adb -Arguments @('-s', $Serial, 'shell', 'dumpsys', 'gfxinfo', $packageName, 'framestats')

$totalFrames = Get-MetricNumber -Text $gfxInfo -Pattern '^\s*Total frames rendered:\s*(?<value>\d+)\s*$'
$jankMatch = [regex]::Match(
    $gfxInfo,
    '(?im)^\s*Janky frames:\s*(?<count>\d+)\s*\((?<percent>[0-9]+(?:[.,][0-9]+)?)%\)'
)
$jankyFrames = $null
$jankyPercent = $null
if ($jankMatch.Success) {
    $jankyFrames = [int]$jankMatch.Groups['count'].Value
    $jankyPercent = [double]::Parse($jankMatch.Groups['percent'].Value.Replace(',', '.'), $invariantCulture)
}

$p95Ms = Get-MetricNumber -Text $gfxInfo -Pattern '^\s*95th percentile:\s*(?<value>\d+(?:\.\d+)?)ms\s*$'
$p99Ms = Get-MetricNumber -Text $gfxInfo -Pattern '^\s*99th percentile:\s*(?<value>\d+(?:\.\d+)?)ms\s*$'
$highInputLatency = Get-MetricNumber -Text $gfxInfo -Pattern '^\s*Number High input latency:\s*(?<value>\d+)\s*$'
$slowUiThread = Get-MetricNumber -Text $gfxInfo -Pattern '^\s*Number Slow UI thread:\s*(?<value>\d+)\s*$'
$gpuPercentiles = [ordered]@{}
foreach ($percentile in @(50, 90, 95, 99)) {
    $gpuPercentiles["p$percentile"] = Get-GpuPercentile -Text $gfxInfo -Percentile $percentile
}

$frameThreshold = Get-FrameStatsOverThreshold -FrameStats $frameStats -TotalFrames ([int]$totalFrames) -ThresholdMs 50
$failedTransitions = @($transitions | Where-Object { -not $_.Pass })
$monthPass = $transitions.Count -eq $Count -and $failedTransitions.Count -eq 0
$totalFramesPass = $null -ne $totalFrames -and $totalFrames -gt 0
$p95Pass = $null -ne $p95Ms -and $p95Ms -le 16
$p99Pass = $null -ne $p99Ms -and $p99Ms -le 32
$jankPass = $null -ne $jankyPercent -and $jankyPercent -lt 3
$noLongFramePass = $frameThreshold.Available -and $frameThreshold.Count -eq 0
$slowUiPass = $null -ne $slowUiThread -and $slowUiThread -eq 0
$highInputPass = $null -ne $highInputLatency -and $highInputLatency -le 2
$gpuAvailable = $null -ne $gpuPercentiles.p95 -and $null -ne $gpuPercentiles.p99

$checks = @(
    [pscustomobject]@{
        Name = 'Month transitions'
        Pass = [bool]$monthPass
        Observed = "$($transitions.Count - $failedTransitions.Count)/$Count exact transition(s)"
        Target = "each $Direction swipe advances exactly one month"
    }
    [pscustomobject]@{
        Name = 'Total frames'
        Pass = [bool]$totalFramesPass
        Observed = if ($null -eq $totalFrames) { 'N/A' } else { [int]$totalFrames }
        Target = '> 0'
    }
    [pscustomobject]@{
        Name = 'Frame p95'
        Pass = [bool]$p95Pass
        Observed = Format-Milliseconds $p95Ms
        Target = '<= 16 ms'
    }
    [pscustomobject]@{
        Name = 'Frame p99'
        Pass = [bool]$p99Pass
        Observed = Format-Milliseconds $p99Ms
        Target = '<= 32 ms'
    }
    [pscustomobject]@{
        Name = 'Janky frames'
        Pass = [bool]$jankPass
        Observed = if ($null -eq $jankyPercent) { 'N/A' } else { "$jankyFrames ($($jankyPercent.ToString('0.##', $invariantCulture))%)" }
        Target = '< 3%'
    }
    [pscustomobject]@{
        Name = 'Frames >= 50 ms'
        Pass = [bool]$noLongFramePass
        Observed = if (-not $frameThreshold.Available) { "N/A ($($frameThreshold.Reason))" } else { "$($frameThreshold.Count) (max $(Format-Milliseconds $frameThreshold.MaxMs))" }
        Target = '0'
    }
    [pscustomobject]@{
        Name = 'Slow UI thread'
        Pass = [bool]$slowUiPass
        Observed = if ($null -eq $slowUiThread) { 'N/A' } else { [int]$slowUiThread }
        Target = '0'
    }
    [pscustomobject]@{
        Name = 'High input latency'
        Pass = [bool]$highInputPass
        Observed = if ($null -eq $highInputLatency) { 'N/A' } else { [int]$highInputLatency }
        Target = '<= 2'
    }
    [pscustomobject]@{
        Name = 'GPU percentiles'
        Pass = $true
        Observed = if ($gpuAvailable) { "p50=$(Format-Milliseconds $gpuPercentiles.p50); p90=$(Format-Milliseconds $gpuPercentiles.p90); p95=$(Format-Milliseconds $gpuPercentiles.p95); p99=$(Format-Milliseconds $gpuPercentiles.p99)" } else { 'N/A (informational only)' }
        Target = 'informational only'
    }
)

$failedChecks = @($checks | Where-Object { -not $_.Pass })
$gatePassed = $failedChecks.Count -eq 0
$gateStatus = if ($gatePassed) { 'PASS' } else { 'FAIL' }

$result = [ordered]@{
    TimestampUtc = [DateTime]::UtcNow.ToString('o')
    Package = $packageName
    Device = [ordered]@{
        Serial = $targetDevice.Serial
        Model = $targetDevice.Model
        Android = $targetDevice.Android
    }
    Gesture = [ordered]@{
        Count = $Count
        Direction = $Direction
        DurationMs = $DurationMs
        SettleMs = $settleMs
        ScreenWidth = $screenSize.Width
        ScreenHeight = $screenSize.Height
        SwipeY = $swipeY
        StartX = if ($Direction -eq 'Left') { $rightX } else { $leftX }
        EndX = if ($Direction -eq 'Left') { $leftX } else { $rightX }
    }
    Headers = [ordered]@{
        Initial = Format-MonthHeader $initialHeader
        ExpectedFinal = Format-MonthHeader $expectedFinalHeader
        ActualFinal = if ($finalTransition) { $finalTransition.Actual } else { 'NOT FOUND' }
        Transitions = @($transitions)
    }
    Metrics = [ordered]@{
        TotalFrames = $totalFrames
        JankyFrames = $jankyFrames
        JankyPercent = $jankyPercent
        P95Ms = $p95Ms
        P99Ms = $p99Ms
        HighInputLatency = $highInputLatency
        SlowUiThread = $slowUiThread
        FramesAtLeast50Ms = $frameThreshold.Count
        FrameStatsParsedFrames = $frameThreshold.ParsedFrames
        MaxFrameMs = $frameThreshold.MaxMs
        GpuPercentilesMs = $gpuPercentiles
    }
    Checks = $checks
    UnmeasuredCriteria = @(
        'This ADB gate does not measure visual frame timing. Run :benchmark:connectedBenchmarkAndroidTest to collect FrameTimingMetric.'
    )
    Gate = $gateStatus
    ResultPath = $resultPath
}

$resultJson = $result | ConvertTo-Json -Depth 8
$resultDirectory = Split-Path -Parent $resultPath
if (-not (Test-Path -LiteralPath $resultDirectory)) {
    throw "Result directory does not exist: $resultDirectory"
}
$utf8NoBom = New-Object -TypeName System.Text.UTF8Encoding -ArgumentList $false
[IO.File]::WriteAllText($resultPath, $resultJson, $utf8NoBom)

Write-Output ''
Write-Output 'CalendarScreen swipe regression gate'
Write-Output ("Device: {0} | Android {1} | Model {2}" -f $targetDevice.Serial, $targetDevice.Android, $targetDevice.Model)
Write-Output ("Gesture: {0} swipe(s), {1}, {2} ms; screen {3}x{4}" -f $Count, $Direction, $DurationMs, $screenSize.Width, $screenSize.Height)
Write-Output ("Header: {0} -> expected {1} -> actual {2}" -f (Format-MonthHeader $initialHeader), (Format-MonthHeader $expectedFinalHeader), $(if ($finalTransition) { $finalTransition.Actual } else { 'NOT FOUND' }))
foreach ($transition in $transitions) {
    $transitionStatus = if ($transition.Pass) { 'PASS' } else { 'FAIL' }
    Write-Output ("{0}: swipe #{1} | expected {2} | actual {3}" -f $transitionStatus, $transition.Index, $transition.Expected, $transition.Actual)
}
Write-Output ''
foreach ($check in $checks) {
    $checkStatus = if ($check.Pass) { 'PASS' } else { 'FAIL' }
    Write-Output ("{0}: {1} | observed {2} | target {3}" -f $checkStatus, $check.Name, $check.Observed, $check.Target)
}
Write-Output ''
Write-Output "Gate: $gateStatus"
Write-Output 'Visual frame timing: measured separately by :benchmark:connectedBenchmarkAndroidTest (FrameTimingMetric).'
Write-Output "Result JSON: $resultPath"

if (-not $gatePassed) {
    exit 1
}

exit 0
