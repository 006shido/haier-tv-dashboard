# 3.3.1-navigation — 3 November 2026

Android version code: 14. Package: `com.gathin.tvhome`.

## Changes

- Website entry/exit follows the visible app column in both vertical directions, including when the website row has scrolled sideways.
- The established outer scrolling layout is retained. Native automatic focus scrolling no longer competes with the dashboard's own focus reveal.
- The optional accessibility helper focuses the first native system power-menu button after the launcher popup closes. It never activates a power action.
- The Home app opens a setup screen with Set as default Home and Try Home first. Android owns the default-Home chooser; Launcher settings can reopen it.
- README rewritten around installation, setup, usage, permissions and limitations. Unneeded upstream device-cleanup installers omitted from this launcher-only copy.

## Verified

- JDK 17 release unit tests, lint and assembleRelease completed successfully (lint has zero errors, warnings remain).
- Installed on Haier Matrix TV DE, Android TV 14, as an update preserving local data.
- D-pad navigation from a favourite through the Websites row into Apps and back returned to the matching third column.
- The native Power off button received input focus automatically. D-pad Right moved focus to the native Restart button; Left returned and Back closed the dialog. No power action was activated.
- The app setup screen opened. Set as default Home displayed Android's chooser with Home listed; the test backed out without changing the existing selection. Try Home first opened the dashboard.

## Permission disclosure

The optional Home accessibility helper now declares permission to read screen contents. This is broader than a Home-only redirect helper. The owner explicitly approved this change on the test TV. Code reads the active accessibility root for at most two seconds after an explicit power-menu request, targets only SystemUI's global-actions container, and sends only an input-focus action. It does not click a power button or retain screen contents. New users must review Android's accessibility consent before enabling the helper. The normal launcher/default-Home setup does not require enabling it.

## Limits

This is a device-tested prerelease, not a guarantee for every TV. No actual shutdown/restart or fresh-device reboot/default-Home takeover was tested. Some Haier firmware ignores Android's chosen Home and needs the optional helper. The APK remains above upstream's 576 KiB target. The larger upstream Python suite did not pass in the Windows workspace; it is not part of this launcher-only repository. No general performance improvement is claimed.

Earlier snapshot APKs remain in the previous release. Installing this APK over the repackaged version-code-13 previous build is allowed because this build uses code 14 and the same signing key. Reinstalling a lower-version snapshot may be rejected by Android. The private signing key and user data are not included.
