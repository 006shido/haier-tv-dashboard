package com.gathin.tvhome;

import android.app.Activity;
import android.content.*;
import android.widget.Toast;

/** Switches which Home UI the helper opens without changing system permissions. */
final class HomeSwitcher {
    private static final String PREFS="home_destination", STOCK="google_tv";
    static boolean usesGoogle(Context context){return context.getSharedPreferences(PREFS,0).getBoolean(STOCK,false);}
    static int label(Context context){return usesGoogle(context)?R.string.use_custom_home:R.string.switch_google_tv;}
    static void toggle(Activity activity){
        boolean previous=usesGoogle(activity),useStock=!previous;
        Intent target=useStock?new Intent().setComponent(new ComponentName("com.google.android.apps.tv.launcherx","com.google.android.apps.tv.launcherx.home.HomeActivity"))
                :new Intent(activity,MainActivity.class);
        target.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        android.content.SharedPreferences prefs=activity.getSharedPreferences(PREFS,0);
        if(!prefs.edit().putBoolean(STOCK,useStock).commit()){
            Toast.makeText(activity,R.string.not_available,Toast.LENGTH_SHORT).show();return;
        }
        try{activity.startActivity(target);}
        catch(ActivityNotFoundException|SecurityException error){
            prefs.edit().putBoolean(STOCK,previous).apply();
            Toast.makeText(activity,R.string.not_available,Toast.LENGTH_SHORT).show();
        }
    }
}
