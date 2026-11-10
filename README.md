# OpenHear

An open-source, clean-room, ad-free Android companion utility for Bluetooth hearables, built with **Kotlin Multiplatform** shared code and **Compose Multiplatform** UI.

## Features

- **Real-time battery telemetry:** Left earbud, right earbud, and charging-case levels.
- **Beast Mode:** Low-latency mode status and control.
- **7-band equalizer:** 60 Hz, 120 Hz, 250 Hz, 750 Hz, 2 kHz, 5 kHz, and 15 kHz with presets and custom gains.
- **Touch remapping:** Independent single-tap, double-tap, and long-press actions for each earbud.
- **Privacy-first:** Local Bluetooth communication with no account, cloud service, analytics, or telemetry.
- **Android integration:** Runtime Bluetooth permissions and a Beast Mode Quick Settings tile.

## Architecture

```text
┌──────────────────────────────────────────────────────┐
│                  Compose Android UI                  │
│   ScanScreen → DashboardScreen → EQ / Touch Mapping │
└──────────────────────────┬───────────────────────────┘
                           │
                           ▼
┌──────────────────────────────────────────────────────┐
│              DeviceConnectionManager                 │
└──────────────────────────┬───────────────────────────┘
                           │
                           ▼
┌──────────────────────────────────────────────────────┐
│             Hearable abstraction layer               │
│     BaseHearable ← Airdopes800Hearable ← Factory     │
└──────────────────────────┬───────────────────────────┘
                           │
                           ▼
┌──────────────────────────────────────────────────────┐
│              TOTA protocol and crypto                │
│ FrameCodec · AES-128-CBC · SHA-256 · CRC-32          │
└──────────────────────────┬───────────────────────────┘
                           │
                           ▼
┌──────────────────────────────────────────────────────┐
│             Android RFCOMM transport                 │
│ BluetoothSocket · BES service UUID · channel 13      │
└──────────────────────────────────────────────────────┘
```

The `shared` module contains protocol, device, state, and Compose code. `androidApp` is the only product target. A headless `testJvm` target exists solely to run protocol and crypto unit tests on development hosts such as Fedora; it is not a desktop application.

## Requirements

- JDK 17
- Android SDK Platform 34 and Build Tools 34.0.0
- Android device with Bluetooth Classic support for hearable integration testing

Set `ANDROID_HOME` or `ANDROID_SDK_ROOT`, or create an ignored `local.properties` file:

```properties
sdk.dir=/absolute/path/to/Android/Sdk
```

## Build and test on Fedora

Run host-side protocol and crypto tests:

```bash
./gradlew :shared:testJvmTest
```

Build the Android debug APK:

```bash
./gradlew :androidApp:assembleDebug
```

Install it on a connected Android device:

```bash
./gradlew :androidApp:installDebug
```

Build unsigned release artifacts:

```bash
./gradlew :androidApp:assembleRelease :androidApp:bundleRelease
```

Outputs are written under `androidApp/build/outputs/`. Release signing is intentionally not stored in this repository; configure a private keystore before distribution.

## Android permissions

Android 12 and newer require runtime `BLUETOOTH_SCAN` and `BLUETOOTH_CONNECT` permissions. Earlier Android versions use legacy Bluetooth permissions declared with the appropriate SDK limit.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md) for clean-room rules, driver guidance, and verification commands.

## License

Licensed under the [Apache License 2.0](LICENSE).

## Legal disclaimer

OpenHear is an independent open-source project and is not affiliated with, endorsed by, or associated with Imagine Marketing Limited or boAt. Product names and trademarks belong to their respective owners.

This software is provided **AS IS**, without warranty of any kind. Use it at your own risk and listen at safe volume levels.
