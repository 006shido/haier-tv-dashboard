# Personal TV dashboard

This is a personal dashboard fork of [Gathin23/cleanyourtv](https://github.com/Gathin23/cleanyourtv), under the original [MIT licence](LICENSE). It changes the launcher rather than Haier's firmware. This repository contains the launcher source. Device-cleanup tools and documentation remain in the upstream repository.

The dashboard has been exercised on a Haier Matrix TV DE running Android TV 14. Other firmware can expose different settings activities and Home behaviour. The custom dashboard menus currently use English text; upstream translations remain available for the original controls.

## Features

- Scenic or locally selected wallpaper, greeting, clock and remote battery label.
- Favourites, a horizontal website row, and a scrolling app grid with persistent ordering.
- Hold OK on an app or website for its options; choose **Move**, use the D-pad, then OK or Back to place it.
- Website editing, colours and removal. New installs start with an empty website list. Existing installations retain their saved links.
- A translucent notification drawer showing three complete cards, with a bounded local history of 60 entries.
- Inputs, Wi-Fi, Bluetooth and TV settings shortcuts. HDMI, casting, voice services and paired accessories remain managed by Android.
- **Switch to Google TV** / **Use this Home screen** in launcher settings and the gear's hold menu on the tested Google TV device.

The Google icon opens account settings; it does not display the active Google TV profile photo. The battery label shows the remote's reported Bluetooth value, which can disagree with the firmware's low-battery warning. Notification access receives system-posted notifications, not every in-app recommendation. Wallpaper selection supports local TV storage or USB through an installed document picker.

## Build from source

Use JDK 17 and Android SDK platform/build-tools 34. No new launcher runtime dependencies are required.

```powershell
cd launcher
.\gradlew.bat --no-daemon testReleaseUnitTest lintRelease assembleRelease
```

On Linux or macOS use `./gradlew` with the same tasks. The APK is generated at `launcher/app/build/outputs/apk/release/app-release.apk`. R8 shrinks and obfuscates release code; retain its matching `launcher/app/build/outputs/mapping/release/mapping.txt` locally for troubleshooting. The bundled scenic image is a compressed WebP; custom imports are sampled to a maximum of 1920 × 1080.

The existing release configuration uses a local Android debug signing key for personal sideloading. For a public distribution, use a signing key you control and keep it outside Git; retain that key for future updates. A differently signed APK cannot replace an existing installation. Do not upload signing keys or publish a local debug keystore.

## Install and try

Pair Android platform-tools with the TV's current wireless debugging address using the TV's own pairing UI. Then install only the APK:

```text
adb -s <TV_ADDRESS:PORT> install -r <PATH_TO_BUILT_APK>
adb -s <TV_ADDRESS:PORT> shell am start -n com.gathin.tvhome/.MainActivity
```

Start by opening **Home** as an ordinary app. Do not use the upstream build-and-install/debloat scripts just to try this dashboard: those scripts have a separate device-cleanup purpose. Keep the original launcher and TV system services enabled.

No root or firmware changes are needed. Making this the default Home depends on the TV. On the tested Haier, the stock Home remains installed and an explicitly enabled accessibility helper redirects its Home screen. That helper is optional, is not enabled by installation, and can be paused with **Switch to Google TV**. Do not enable remote helpers and disable stock packages as a blind setup recipe.

## Optional access

Notification access is enabled by the owner in Android's notification-access settings. The local inbox can contain private message text; clear it from the drawer when desired. The launcher has no Internet permission or telemetry. Browser shortcuts open another app, whose own network activity and privacy behaviour are separate.

Bluetooth battery reading requires nearby-device/Bluetooth permission. A direct native accessories panel on the tested Haier additionally uses an owner-approved `WRITE_SECURE_SETTINGS` grant. That is a broad system permission and is **not required to try Home**. Without it, use the standard TV settings route. Installation does not grant it automatically. If the owner explicitly chooses that route, the corresponding grant and undo are:

```text
adb -s <TV_ADDRESS:PORT> shell pm grant com.gathin.tvhome android.permission.WRITE_SECURE_SETTINGS
adb -s <TV_ADDRESS:PORT> shell pm revoke com.gathin.tvhome android.permission.WRITE_SECURE_SETTINGS
```

Accessibility and notification access can be revoked in the TV's settings. Turn off debugging after setup when it is no longer needed. No app needs to be removed, no device reset is part of setup, and selecting a website does not install an APK.

## Repository hygiene and checks

Upload source through Git rather than uploading the whole laptop folder. `.gitignore` excludes builds/APKs, local SDK paths, virtual environments, ADB keys, TV backups, traces, screenshots and personal work notes. Personal websites, notification history, imported wallpaper and app order live in the TV's private app data and are not bundled in the source.

Run launcher unit tests, lint and the release build using the command above. The larger upstream Python/tooling suite belongs to the original repository and is not included in this launcher-only snapshot. In the local Windows workspace, that suite did not pass (filesystem-symlink and shell/network compatibility failures were among the reported causes).

Test remote focus, app/website Move, settings, both Home destinations, wallpaper and notification scrolling on the target TV. A successful build does not establish compatibility with every TV. Do not publish private TV screenshots, account names, device addresses, keys or notification contents.

No device-cleanup tools, protected-package rules or CI workflows are modified or included in this launcher-only snapshot. This dashboard is a personal restyle, not an upstream-ready small launcher switch. See the local validation report supplied with a tested build for the checks actually performed; no general frame-rate or latency improvement is claimed without controlled measurements.

The custom release currently exceeds the upstream 576 KiB APK limit. The original size gate is left intact and will fail for this dashboard until that remaining size work is completed. The full Python suite also needs validation on Linux: the Windows run encountered filesystem-symlink and shell/network compatibility failures. Do not describe this checkout as passing every upstream check.
