package com.gathin.tvhome;

import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.TextView;

/** High contrast focus treatment anchored to each tile, including after adapter rebinds. */
final class DashboardFocus {
    private static final java.util.WeakHashMap<View,Drawable> RINGS=new java.util.WeakHashMap<>();
    static void apply(View card,TextView name,boolean focused,boolean animate){
        float d=card.getResources().getDisplayMetrics().density;
        Drawable ring=RINGS.get(card);
        if(focused&&ring==null){ring=new Ring(d);RINGS.put(card,ring);}
        card.setForeground(focused?ring:null);
        card.animate().cancel();
        float scale=focused?1.025f:1f,lift=0;
        if(animate)card.animate().scaleX(scale).scaleY(scale).translationY(lift).setDuration(150).start();
        else {card.setScaleX(scale);card.setScaleY(scale);card.setTranslationY(lift);}
        card.setElevation(0);
        name.setTranslationY(0);name.setAlpha(focused?1f:.65f);
        name.setTextColor(0xfff4f4f7);name.setBackground(null);
    }
    private static final class Ring extends Drawable {
        private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);private final float d;
        Ring(float density){d=density;}
        @Override public void draw(Canvas c){
            Rect b=getBounds();RectF r=new RectF(b);r.inset(3*d,3*d);p.setStyle(Paint.Style.STROKE);
            float radius=Math.max(0,AppAdapter.cornerRadius(b.width(),d)-3*d);
            p.setStrokeWidth(5*d);p.setColor(0xff15171c);c.drawRoundRect(r,radius,radius,p);
            p.setStrokeWidth(2.5f*d);p.setColor(0xfffafafa);c.drawRoundRect(r,radius,radius,p);
        }
        @Override public void setAlpha(int a){p.setAlpha(a);invalidateSelf();}
        @Override public void setColorFilter(ColorFilter f){p.setColorFilter(f);invalidateSelf();}
        @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
    }
}
