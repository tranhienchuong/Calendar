# Android build, test, and real-device rules

Use the repository wrapper and `scripts/android.ps1` for Android work. Run commands from the repository root.

## Build

- Use the shortest matching gate while developing; Gradle's daemon, configuration cache, build cache, and file-system watching are intentionally enabled for warm runs.
- Compile-only feedback after a UI or Kotlin edit: `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\android.ps1 -Task compile`.
- Build the current debug APK: `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\android.ps1 -Task build`
- The expected artifact is `app\build\outputs\apk\debug\app-debug.apk`.
- Treat a non-zero script exit code as a failed build. Report the Gradle task and the first actionable error; do not install an APK from a failed or stale build.

## Verification

- Run focused unit tests while changing business logic: `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\android.ps1 -Task test-class -Tests <fully.qualified.TestClass>`.
- Run the full local unit suite: `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\android.ps1 -Task unit-test`.
- Run lint for UI, resource, manifest, Gradle, or dependency changes: `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\android.ps1 -Task lint`.
- Before handing off a feature or fix, run the combined local quality gate once: `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\android.ps1 -Task verify`. State the exact commands and result.
- Run instrumented tests only on a dedicated test device, or when the user explicitly authorizes the connected device. Instrumented tests install APKs and may alter app-local test data.

## Real-device ADB

- Work only with a physical, authorized device. Never start or select an emulator for this project.
- Inspect the attached device first: `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\android.ps1 -Task device-check -Serial <serial>`.
- Pass `-Serial <serial>` whenever more than one device is connected. The script rejects serials beginning with `emulator-`.
- Build and install the debug APK while preserving package data: `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\android.ps1 -Task install -Serial <serial> -AllowDeviceMutation`.
- Launch and wait for the main activity: `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\android.ps1 -Task launch -Serial <serial>`.
- Run the full instrumented suite on the selected device only: `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\android.ps1 -Task connected-test -Serial <serial> -AllowDeviceMutation`.
- `-AllowDeviceMutation` acknowledges device writes. Never use it without user authorization.

## Performance reproduction

- For touch or animation reports, collect a repeatable baseline on the real device: reset `gfxinfo`, perform the exact gesture, then capture `gfxinfo` again.
- Record the device model, Android version, gesture count/duration, and `Janky frames`, frame percentiles, `High input latency`, `Slow UI thread`, and GPU percentiles.
- Restore any temporary device settings before handoff, including all three animation scales.
- Run the CalendarScreen month-swipe regression gate with an installed app whose onboarding is already complete: `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\measure-calendar-swipe.ps1 -Serial <serial> -Count 10 -Direction Left -DurationMs 300`. The gate validates the header after every swipe, not merely the net final month.
- `Left` advances to the next month and `Right` goes to the previous month. The script launches the existing activity, but never installs an APK, clears app data, or changes animation scales; it shares `scripts/lib/android-device.ps1` with `android.ps1` and rejects `emulator-*` and other QEMU devices.
- The gate exits `0` on pass and `1` on fail. It writes only parsed JSON to the system temp directory by default; an explicit `-OutputPath` must point outside the source tree. The ADB gate does not measure visual frame timing.

## Guardrails

- Keep build output, test output, and device serials out of source files.
- Do not uninstall the application, clear application data, alter global device settings, or stop unrelated ADB/Gradle processes unless the user explicitly asks.
- When a build is already running, wait for it or ask before stopping it; do not start competing Gradle builds.
