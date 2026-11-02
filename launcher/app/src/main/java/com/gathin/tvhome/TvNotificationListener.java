package com.gathin.tvhome;

import android.app.Notification;
import android.app.PendingIntent;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

public final class TvNotificationListener extends NotificationListenerService {
    private static TvNotificationListener connected;
    private final AppChangeReceiver appChanges = new AppChangeReceiver();
    @Override public void onCreate() {
        super.onCreate();
        android.content.IntentFilter filter = new android.content.IntentFilter(android.content.Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(android.content.Intent.ACTION_PACKAGE_REMOVED);
        filter.addDataScheme("package");
        if (android.os.Build.VERSION.SDK_INT >= 33) registerReceiver(appChanges, filter, RECEIVER_NOT_EXPORTED);
        else registerReceiver(appChanges, filter);
    }
    @Override public void onListenerConnected() {
        connected = this;
        try {
            StatusBarNotification[] active = getActiveNotifications();
            if (active != null) for (StatusBarNotification n : active) save(n);
        } catch (RuntimeException ignored) { }
    }
    @Override public void onListenerDisconnected() { connected = null; }
    @Override public void onDestroy() {
        if(connected == this) connected = null;
        unregisterReceiver(appChanges);
        super.onDestroy();
    }
    @Override public void onNotificationPosted(StatusBarNotification n) { save(n); }
    private void save(StatusBarNotification sbn) {
        if (sbn == null || !sbn.getUser().equals(android.os.Process.myUserHandle())) return;
        Notification n = sbn.getNotification();
        if ((n.flags & Notification.FLAG_GROUP_SUMMARY) != 0) return;
        CharSequence title = n.extras.getCharSequence(Notification.EXTRA_TITLE, "Notification");
        CharSequence text = n.extras.getCharSequence(Notification.EXTRA_BIG_TEXT);
        if (text == null) text = n.extras.getCharSequence(Notification.EXTRA_TEXT, "");
        Ranking ranking = new Ranking();
        boolean important = getCurrentRanking().getRanking(sbn.getKey(), ranking)
                && ranking.getImportance() >= android.app.NotificationManager.IMPORTANCE_HIGH;
        String category = Notification.CATEGORY_RECOMMENDATION.equals(n.category) ? "suggestions" : "notifications";
        NotificationInbox.add(this, sbn.getKey(), sbn.getPackageName(), String.valueOf(title),
                String.valueOf(text), category, important, sbn.getPostTime());
    }
    static boolean open(String key) {
        TvNotificationListener service = connected;
        if (service == null) return false;
        try {
            StatusBarNotification[] active = service.getActiveNotifications();
            if(active != null) for(StatusBarNotification sbn : active) {
                if (!key.equals(sbn.getKey())) continue;
                PendingIntent intent = sbn.getNotification().contentIntent;
                if (intent != null) { intent.send(); return true; }
            }
        } catch (PendingIntent.CanceledException | RuntimeException ignored) { }
        return false;
    }
}
