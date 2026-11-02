# 3.3.2-focus — 3 November 2026

Android version code: 15. Package: `com.gathin.tvhome`.

## Changes

- A transparent rounded white outline makes the selected native Power off/Restart option visible and follows D-pad focus. It does not intercept input or activate a power action.
- The outline has no opaque rectangular background and is removed when the dialog closes.
- App focus rings follow the tile corner radius; launcher popups clip their contents to the rounded outline.
- README explains power-menu controls and the accessibility feature's exact scope.
- Includes the previous navigation fixes and app-based default-Home setup.

## Verified

- Release unit tests, lintRelease and assembleRelease passed with JDK 17. Lint has zero errors; existing warnings remain.
- Installed version 15 on the Haier Android TV 14 as an update preserving local data.
- Visually checked the rounded white outline on Power off and Restart after D-pad Right, then closed with Back. The accessibility overlay was absent after closing.
- No shutdown or restart was activated. No full feature or cross-device regression test is claimed.

## Accessibility and limits

The optional helper uses Android's broad read-screen accessibility capability, already approved on the test TV. Initial power-button focus is bounded to two seconds; while the explicitly opened SystemUI power menu remains open, focus/window events update the selected button outline from its bounds. Closing the menu removes the outline and focus-event subscription. No screen contents are retained and no power button is clicked automatically.

This is a device-tested prerelease. Firmware can expose different power dialogs; the helper does not restyle the TV's native dialog background. The APK exceeds upstream's 576 KiB target. The upstream Python/tooling suite is not included in this launcher-only repository. Previous release APKs remain available; Android may reject installing an older version over this one. Private signing keys and TV data are not published.
