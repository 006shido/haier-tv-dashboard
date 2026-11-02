package com.gathin.tvhome;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

/** App entry point: try Home or choose it through Android's own default-Home picker. */
public final class SetupActivity extends Activity {
    private int dp(int value){return Math.round(value*getResources().getDisplayMetrics().density);}
    private boolean isDefaultHome(){
        ResolveInfo home=getPackageManager().resolveActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME),0);
        return home!=null&&home.activityInfo!=null&&getPackageName().equals(home.activityInfo.packageName);
    }
    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        if(isDefaultHome()){openHome();return;}
        LinearLayout page=new LinearLayout(this);page.setOrientation(LinearLayout.VERTICAL);
        page.setGravity(Gravity.CENTER_VERTICAL);page.setPadding(dp(80),dp(40),dp(80),dp(40));page.setBackgroundColor(0xff101419);
        page.addView(text(getString(R.string.home_setup_title),32,Color.WHITE));
        TextView detail=text(getString(R.string.home_setup_description),17,0xffb8c2cf);
        LinearLayout.LayoutParams explanation=new LinearLayout.LayoutParams(-1,-2);explanation.topMargin=dp(16);explanation.bottomMargin=dp(24);page.addView(detail,explanation);
        TextView choose=button(getString(R.string.set_default_home),()->startActivity(new Intent(this,HomeSettingsActivity.class)));
        page.addView(choose,new LinearLayout.LayoutParams(dp(420),dp(54)));
        LinearLayout.LayoutParams tryLayout=new LinearLayout.LayoutParams(dp(420),dp(54));tryLayout.topMargin=dp(12);
        page.addView(button(getString(R.string.try_home),this::openHome),tryLayout);
        TextView hint=text(getString(R.string.home_setup_hint),14,0xff8996a7);
        LinearLayout.LayoutParams hintLayout=new LinearLayout.LayoutParams(-1,-2);hintLayout.topMargin=dp(24);page.addView(hint,hintLayout);
        setContentView(page);choose.requestFocus();
    }
    @Override protected void onResume(){super.onResume();if(isDefaultHome()&&!isFinishing())openHome();}
    private TextView text(String value,int size,int color){TextView view=new TextView(this);view.setText(value);view.setTextSize(size);view.setTextColor(color);return view;}
    private TextView button(String label,Runnable action){
        TextView view=text(label,18,Color.WHITE);view.setGravity(Gravity.CENTER);view.setFocusableInTouchMode(true);view.setClickable(true);
        GradientDrawable normal=new GradientDrawable();normal.setColor(0xff222d3a);normal.setCornerRadius(dp(12));
        GradientDrawable focused=new GradientDrawable();focused.setColor(0xff344f6c);focused.setCornerRadius(dp(12));focused.setStroke(dp(2),Color.WHITE);
        StateListDrawable states=new StateListDrawable();states.addState(new int[]{android.R.attr.state_focused},focused);states.addState(new int[]{},normal);view.setBackground(states);
        view.setOnClickListener(v->action.run());return view;
    }
    private void openHome(){startActivity(new Intent(this,MainActivity.class));finish();}
}
