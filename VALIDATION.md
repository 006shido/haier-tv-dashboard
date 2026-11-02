# 3.3.3 - 3 November 2026

Android version code: 17. Package: `com.gathin.tvhome`.

## Fix

3.3.2 fixed the selection outline but left the native power dialog's rectangular outer shadow visible. This release covers the exterior of the rounded native panel with a dimmed image of the launcher, retaining the native controls, D-pad focus and white selection outline. It does not click any power action. Pressing Back removes the stationary backdrop mask before the native dismissal animation, preventing a lingering transparent rectangle.

The backdrop is captured only from the launcher when its power menu is requested, kept in memory, never saved, and released on closing or failure. The accessibility overlay remains non-focusable and non-interactive. The helper temporarily requests key observation while this menu is open so Back can release the cover immediately; it restores the previous key-filter flag on close. No additional permission is required.

## Validation

Release unit tests, lintRelease and assembleRelease passed. The application code matches the previously checked 3.3.3-corners build. This package changes only the displayed version to 3.3.3 and increments the Android version code for an in-place update; assembleRelease passed. Installed the version 3.3.3 package on Haier Android TV 14 preserving local data; Android reports versionName 3.3.3 and versionCode 17. Rounded exterior and both selection positions visually checked, then cancelled with injected Back; no shutdown or restart was activated. The physical remote Back transition needs owner confirmation because injected keys do not reliably exercise accessibility key filtering.

## Limits

This is a device-tested prerelease. The corner mask uses the tested native panel's 28dp corner radius and a 60% dimmed launcher frame; other firmware may render differently. The backdrop stays still while this menu is open. The native power button behavior is unchanged. The optional accessibility helper retains its broad read-screen capability; see README for its scope. No full regression test or actual power-cycle test is claimed. The APK exceeds upstream's size target. Earlier builds remain available, but Android may reject downgrades. No private TV data or signing keys are published.
