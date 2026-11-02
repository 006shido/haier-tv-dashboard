package com.gathin.tvhome;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public final class AppChangeReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        if(intent.getData() == null) return;
        String pkg = intent.getData().getSchemeSpecificPart();
        String action = intent.getAction();
        boolean replacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);
        String event;
        if(Intent.ACTION_PACKAGE_ADDED.equals(action)) event = replacing ? "App updated" : "App installed";
        else if(Intent.ACTION_PACKAGE_REMOVED.equals(action) && !replacing) event = "App removed";
        else return;
        String label = pkg;
        try { label = context.getPackageManager().getApplicationLabel(context.getPackageManager().getApplicationInfo(pkg,0)).toString(); }
        catch (android.content.pm.PackageManager.NameNotFoundException ignored) { }
        long now = System.currentTimeMillis();
        NotificationInbox.add(context, "package:"+pkg+":"+event, pkg, event, label, "apps", false, now);
    }
}
