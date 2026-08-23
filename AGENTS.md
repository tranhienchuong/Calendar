# Android build, test, and real-device rules

Use the repository wrapper and `scripts/android.ps1` for Android work. Run commands from the repository root.

## Build

- Build the current debug APK: `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\android.ps1 -Task build`
- The expected artifact is `app\build\outputs\apk\debug\app-debug.apk`.
- Treat a non-zero script exit code as a failed build. Report the Gradle task and the first actionable error; do not install an APK from a failed or stale build.

## Verification

- Run focused unit tests while changing business logic: `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\android.ps1 -Task unit-test`.
- Run lint for UI, resource, manifest, Gradle, or dependency changes: `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\android.ps1 -Task lint`.
- Before handing off a feature or fix, run both unit tests and lint. State the exact commands and result.
- Run instrumented tests only on a dedicated test device, or when the user explicitly authorizes the connected device. Instrumented tests install APKs and may alter app-local test data.

## Real-device ADB

- Work only with a physical, authorized device. Never start or select an emulator for this project.
- Inspect the attached device first: `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\android.ps1 -Task device-check -Serial <serial>`.
- Pass `-Serial <serial>` whenever more than one device is connected. The script rejects serials beginning with `emulator-`.
- Build and install the debug APK while preserving package data: `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\android.ps1 -Task install -Serial <serial> -AllowDeviceMutation`.
- Launch and wait for the main activity: `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\android.ps1 -Task launch -Serial <serial>`.
- Run the full instrumented suite on the selected device only: `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\android.ps1 -Task connected-test -Serial <serial> -AllowDeviceMutation`.
- `-AllowDeviceMutation` is an explicit acknowledgement that install/test commands modify the selected device. Never use it without user authorization.

## Performance reproduction

- For touch or animation reports, collect a repeatable baseline on the real device: reset `gfxinfo`, perform the exact gesture, then capture `gfxinfo` again.
- Record the device model, Android version, gesture count/duration, and `Janky frames`, frame percentiles, `High input latency`, `Slow UI thread`, and GPU percentiles.
- Restore any temporary device settings before handoff, including all three animation scales.

## Guardrails

- Keep build output, test output, and device serials out of source files.
- Do not uninstall the application, clear application data, alter global device settings, or stop unrelated ADB/Gradle processes unless the user explicitly asks.
- When a build is already running, wait for it or ask before stopping it; do not start competing Gradle builds.
