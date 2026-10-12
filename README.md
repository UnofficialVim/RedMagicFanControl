# RedMagic Fan Control

A Magisk module and Android companion app for controlling and automating the internal cooling fan found in supported RedMagic devices.

## Features
Currently:
  
Planned:

- Fan status monitoring
- Fan speed control
- Call-based fan behavior
- Local socket API
- Native Android companion app

## Status

Early development

Current progress:

* [x] Magisk module structure
* [ ] daemon - Refer to [redmagicfancontrold](https://github.com/UnofficialVim/redmagicfancontrold) for updates on the daemon
* [ ] Device detection
* [ ] Call state monitoring
* [ ] Temp-based fan curves
* [ ] API
* [ ] Android app
* [ ] RPM monitoring
* [ ] Multi-device support

## Supported Devices

Planned support:

* RedMagic 9 Pro

Additional devices may be supported if compatible fan interfaces are available.

## Requirements

### Module

* Magisk
* Root access
* Supported RedMagic device

### App

* Android 12+ (API 31+)
* Root access (granted to the app in Magisk) - the daemon's socket and config are root-only, so the app reaches them through `su` and `nc -U`

## Building the app

The app lives in `RedMagicFanControlApp/` (Kotlin, Jetpack Compose, package `com.unofficialvim.redmagicfancontrol`).

### Prerequisites

* **JDK 17 or newer** (21 recommended). Set `JAVA_HOME`, or let Android Studio use its bundled JDK. A JetBrains-vendor JDK is *not* required.
* **Android SDK** with the **Android 36 platform** (`compileSdk = 36`). Easiest way is Android Studio; otherwise use `sdkmanager "platforms;android-36" "build-tools;36.0.0"`.
* Tell Gradle where the SDK is, either by setting `ANDROID_HOME` or by creating `RedMagicFanControlApp/local.properties` (git-ignored; Android Studio creates it for you):

  ```
  sdk.dir=C\:/Users/<you>/AppData/Local/Android/Sdk
  ```

The Gradle wrapper (9.6.0) is checked in, so no separate Gradle install is needed.

### Build and install

From `RedMagicFanControlApp/`:

```
# Windows
gradlew.bat assembleDebug
# Linux / macOS
./gradlew assembleDebug
```

The APK is written to `app/build/outputs/apk/debug/app-debug.apk`. With a device (or emulator) connected, `gradlew installDebug` builds and installs it in one step.

Or open `RedMagicFanControlApp/` in Android Studio and press Run.

### Configuring the app

Open **Settings** (gear icon, top right) and set:

* **Config file** - path of the daemon's `config.json`. The app reads it into memory and rewrites it when you edit profiles, then sends `reloadConfig` to the daemon.
* **Daemon socket** - the daemon creates `rmfc_socket` in its working directory. The defaults (`/data/adb/modules/redmagic-fan-control/...`) are only guesses; point them at the real locations.

Then tap **Connection test**. It checks the socket, `su`, `nc -U` and the config file, and prints a log you can copy. The app connects directly if it can, and otherwise through `su -c "nc -U <socket>"`, so grant it root in Magisk when prompted.

### Notes

* The Android emulator's `su` only works for the shell user, so the root bridge can't be exercised there; test on a rooted device. The UI and config editing work fine on an emulator.
* The daemon serves one client at a time, so close other socket clients (e.g. `socat`) while using the app.

## Disclaimer

This project modifies fan behavior at the system level. Use at your own risk. The authors are not responsible for overheating, hardware damage, data loss, or other issues resulting from use of this software.

