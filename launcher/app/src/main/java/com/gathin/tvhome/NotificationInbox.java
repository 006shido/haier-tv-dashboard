package com.gathin.tvhome;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;

/** Bounded, local-only history. No notification content leaves the TV. */
final class NotificationInbox {
    static SharedPreferences prefs(Context c) { return c.getSharedPreferences("notification_inbox", Context.MODE_PRIVATE); }
    static synchronized JSONArray entries(Context c) {
        try { return new JSONArray(prefs(c).getString("entries", "[]")); }
        catch (Exception ignored) { return new JSONArray(); }
    }
    static synchronized void add(Context c, String id, String pkg, String title, String text,
                                 String category, boolean important, long time) {
        try {
            title=clip(title);text=clip(text);
            JSONArray old = entries(c), next = new JSONArray();
            boolean read = false;
            for (int i = 0; i < old.length(); i++) {
                JSONObject row = old.optJSONObject(i);if(row==null)continue;
                if (id.equals(row.optString("id")) && title.equals(row.optString("title"))
                        && text.equals(row.optString("text"))) read = row.optBoolean("read");
            }
            JSONObject row = new JSONObject().put("id", id).put("pkg", pkg).put("title", clip(title))
                    .put("text", clip(text)).put("category", category).put("important", important)
                    .put("time", time).put("read", read);
            next.put(row);
            for (int i = 0; i < old.length() && next.length() < 60; i++) {
                JSONObject previous = old.optJSONObject(i);
                if (previous!=null&&!id.equals(previous.optString("id"))) next.put(previous);
            }
            prefs(c).edit().putString("entries", next.toString()).apply();
        } catch (Exception ignored) { /* A malformed notification must not interrupt Home. */ }
    }
    private static String clip(String text) { return text==null?"":text.length() > 1200 ? text.substring(0,1200) : text; }
    static int unread(Context c) {
        JSONArray rows = entries(c); int count = 0;
        for (int i=0; i<rows.length(); i++) {JSONObject row=rows.optJSONObject(i);if(row!=null&&!row.optBoolean("read"))count++;}
        return count;
    }
    static synchronized void markRead(Context c) {
        JSONArray rows = entries(c);
        boolean changed=false;
        try { for(int i=0;i<rows.length();i++){JSONObject row=rows.optJSONObject(i);if(row!=null&&!row.optBoolean("read")){row.put("read",true);changed=true;}} }
        catch (Exception ignored) { return; }
        if(changed)prefs(c).edit().putString("entries", rows.toString()).apply();
    }
}
