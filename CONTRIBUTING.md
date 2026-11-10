# Contributing to OpenHear

Thank you for contributing to OpenHear. This project targets Android and keeps protocol, device, state, and Compose code in the shared module.

## Clean-room development

1. Do not commit proprietary applications, firmware, vendor SDKs, decompiled sources, packet captures containing private data, or extracted vendor resources.
2. Document protocol behavior from independently observed Bluetooth traffic and public platform interfaces.
3. Use trademarks only where needed to identify compatible hardware. Do not use them in package identifiers, namespaces, or project branding.
4. Never commit Bluetooth addresses, keystores, signing credentials, API keys, or machine-specific paths.

## Adding a hearable driver

1. Implement `BaseHearable` in `shared/src/commonMain/kotlin/com/openhear/common/device/`.
2. Register the driver in `HearableFactory.kt` using stable advertisement identifiers.
3. Add protocol serialization and parsing coverage under `shared/src/commonTest/kotlin/`.
4. Keep Android transport details under `shared/src/androidMain/`.

## Development workflow

Use JDK 17 and Android SDK Platform 34.

Run host-side protocol and crypto tests on Fedora or another JVM host:

```bash
./gradlew :shared:testJvmTest
```

Build and lint Android:

```bash
./gradlew :androidApp:assembleDebug :androidApp:lintDebug
```

Install on a connected Android device for Bluetooth hardware testing:

```bash
./gradlew :androidApp:installDebug
```

Before opening a pull request, ensure tests, Android compilation, and lint pass. Describe hardware validation separately; automated tests must not depend on a personal device address or mutate physical hardware.
