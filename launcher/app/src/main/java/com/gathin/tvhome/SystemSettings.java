package com.gathin.tvhome;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.provider.Settings;

/** The standard settings intent, or, when nothing usable answers it, the first vendor settings entry
 *  from settings_fallback_components that is installed (Xiaomi China, TCL / FFALCON, Fire OS), else the
 *  AOSP TV settings app's main screen where it is installed. */
final class SystemSettings {
    private SystemSettings() {}

    static Intent bluetoothIntent(Context context) {
        Intent standard = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
        if (context.getPackageManager().resolveActivity(standard, PackageManager.MATCH_DEFAULT_ONLY) != null) return standard;
        // Haier Google TV 14 exposes the existing accessories panel through its settings slice activity.
        if (context.checkSelfPermission(android.Manifest.permission.WRITE_SECURE_SETTINGS)
                == PackageManager.PERMISSION_GRANTED) {
            Intent panel = new Intent().setComponent(new ComponentName("com.android.tv.settings",
                    "com.android.tv.settings.slice.SliceActivity"))
                    .putExtra("slice_uri", "content://com.android.tv.settings.accessories.sliceprovider/general")
                    .putExtra("startup_verification_required", false);
            panel.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            if (context.getPackageManager().resolveActivity(panel, 0) != null) return panel;
        }
        return intent(context, Settings.ACTION_SETTINGS);
    }

    static Intent intent(Context context, String action) {
        PackageManager pm = context.getPackageManager();
        Intent standard = new Intent(action);
        ResolveInfo resolved = pm.resolveActivity(standard, PackageManager.MATCH_DEFAULT_ONLY);
        // Fire OS resolves the standard actions to a CTS compliance stub that opens nothing;
        // treat it as missing so the vendor fallback runs.
        if (resolved != null && !resolved.activityInfo.name.contains("CTSDummy")) return standard;
        Intent vendor = vendorIntent(context);
        if (vendor != null) return vendor;
        // Some firmware ships the AOSP TV settings app with no filter for the standard actions (HY300 Pro+
        // projector, rk3566, Android 14): open its main screen directly. Not a vendor entry, so the Settings
        // menu's "Native settings" row stays as it was.
        Intent aosp = new Intent().setComponent(AOSP_TV_SETTINGS);
        try {
            pm.getPackageInfo(AOSP_TV_SETTINGS.getPackageName(), 0);
            return aosp;
        } catch (PackageManager.NameNotFoundException e) {
            return standard;
        }
    }

    private static final ComponentName AOSP_TV_SETTINGS =
            new ComponentName("com.android.tv.settings", "com.android.tv.settings.MainSettings");

    /** The first vendor settings entry from settings_fallback_components that is installed, or null
     *  when none is. China-market firmware keeps pages the AOSP TV settings has no row for -
     *  Xiaomi's Account & Security, where the ADB switch lives - in the vendor's own settings app;
     *  MainActivity's Settings menu offers it as "Native settings" while one is installed. */
    static Intent vendorIntent(Context context) {
        PackageManager pm = context.getPackageManager();
        for (String value : context.getResources().getStringArray(R.array.settings_fallback_components)) {
            ComponentName component = ComponentName.unflattenFromString(value);
            if (component == null) continue;
            // Explicit resolveActivity() always succeeds, so check the package exists instead.
            try {
                pm.getPackageInfo(component.getPackageName(), 0);
            } catch (PackageManager.NameNotFoundException e) {
                continue;
            }
            return new Intent().setComponent(component);
        }
        return null;
    }

    /** The TV's App info page for pkg, or null where the firmware has none (CVTE's settings app, #1202). */
    static Intent appInfo(Context context, String pkg) {
        Intent info = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + pkg));
        return context.getPackageManager().resolveActivity(info, PackageManager.MATCH_DEFAULT_ONLY) != null ? info : null;
    }
}
