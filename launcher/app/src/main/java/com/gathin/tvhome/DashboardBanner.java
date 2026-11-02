package com.gathin.tvhome;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.View;

/** Quiet translucent greeting surface above the local scenic wallpaper. */
public final class DashboardBanner extends View {
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    public DashboardBanner(Context c, AttributeSet a){super(c,a);setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
    @Override protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        float w=getWidth(),h=getHeight(),d=getResources().getDisplayMetrics().density;
        RectF bounds=new RectF(d/2,d/2,w-d/2,h-d/2);
        paint.setStyle(Paint.Style.FILL);
        paint.setShader(new LinearGradient(0,0,w,0,0xc4212935,0x6b25303e,Shader.TileMode.CLAMP));
        canvas.drawRoundRect(bounds,14*d,14*d,paint);paint.setShader(null);
        paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(d);paint.setColor(0x26ffffff);
        canvas.drawRoundRect(bounds,14*d,14*d,paint);paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xfff4f5f8);paint.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));paint.setTextSize(23*d);
        canvas.drawText(getContext().getString(R.string.dashboard_welcome),22*d,37*d,paint);
        paint.setColor(0xffb8c4d2);paint.setTypeface(Typeface.create("sans-serif",Typeface.NORMAL));paint.setTextSize(12*d);
        canvas.drawText(getContext().getString(R.string.dashboard_subtitle),22*d,60*d,paint);
    }
}
