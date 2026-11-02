package com.gathin.tvhome;

import android.accessibilityservice.AccessibilityService;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

/** Transparent, non-interactive selection outline over an existing system power button. */
final class PowerFocusOutline {
    private final WindowManager windows;
    private final View view;
    private final float density;
    private boolean attached;
    private final Rect previous=new Rect();

    PowerFocusOutline(AccessibilityService service){
        windows=service.getSystemService(WindowManager.class);
        density=service.getResources().getDisplayMetrics().density;
        view=new View(service){
            private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
            @Override protected void onDraw(Canvas canvas){
                float inset=2*density;
                RectF box=new RectF(inset,inset,getWidth()-inset,getHeight()-inset);
                paint.setStyle(Paint.Style.FILL);paint.setColor(0x14ffffff);
                canvas.drawRoundRect(box,18*density,18*density,paint);
                paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(2.5f*density);paint.setColor(Color.WHITE);
                canvas.drawRoundRect(box,18*density,18*density,paint);
            }
        };
        view.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
        view.setFocusable(false);view.setClickable(false);view.setBackgroundColor(Color.TRANSPARENT);
    }

    void show(Rect bounds){
        if(bounds.isEmpty()||windows==null)return;
        if(attached&&previous.equals(bounds))return;
        previous.set(bounds);
        int margin=Math.round(6*density);
        WindowManager.LayoutParams params=new WindowManager.LayoutParams(bounds.width()+2*margin,
                bounds.height()+2*margin,WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        |WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,PixelFormat.TRANSLUCENT);
        params.gravity=Gravity.TOP|Gravity.LEFT;
        params.x=bounds.left-margin;params.y=bounds.top-margin;
        params.setTitle("Home power selection");
        if(attached)windows.updateViewLayout(view,params);
        else{windows.addView(view,params);attached=true;}
    }

    void close(){
        if(attached){try{windows.removeViewImmediate(view);}catch(IllegalArgumentException ignored){}attached=false;}
        previous.setEmpty();
    }
}
