package com.gathin.tvhome;

import android.accessibilityservice.AccessibilityService;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

/** Non-interactive native power selection and rounded backdrop mask. No screen image is stored. */
final class PowerFocusOutline {
    private final WindowManager windows;
    private final View view;
    private final float density;
    private final Rect selected=new Rect();
    private final Rect panel=new Rect();
    private Bitmap backdrop;
    private boolean attached;

    PowerFocusOutline(AccessibilityService service){
        windows=service.getSystemService(WindowManager.class);
        density=service.getResources().getDisplayMetrics().density;
        view=new View(service){
            private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
            private final Path clip=new Path();
            @Override protected void onDraw(Canvas canvas){
                // SystemUI's rectangular shadow is outside its rounded panel. Replace only that
                // exterior with the launcher's own dimmed frame; leave all native controls intact.
                if(backdrop!=null&&!backdrop.isRecycled()){
                    int save=canvas.save();
                    clip.reset();clip.addRoundRect(new RectF(panel),28*density,28*density,Path.Direction.CW);
                    canvas.clipOutPath(clip);
                    paint.setStyle(Paint.Style.FILL);paint.setColor(Color.WHITE);
                    canvas.drawBitmap(backdrop,null,new Rect(0,0,getWidth(),getHeight()),paint);
                    canvas.drawColor(0x99000000);
                    canvas.restoreToCount(save);
                }
                RectF box=new RectF(selected);box.inset(-4*density,-4*density);
                paint.setStyle(Paint.Style.FILL);paint.setColor(0x14ffffff);
                canvas.drawRoundRect(box,18*density,18*density,paint);
                paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(2.5f*density);paint.setColor(Color.WHITE);
                canvas.drawRoundRect(box,18*density,18*density,paint);
            }
        };
        view.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
        view.setFocusable(false);view.setClickable(false);view.setBackgroundColor(Color.TRANSPARENT);
    }

    void show(Rect bounds,Rect panelBounds,Bitmap image){
        if(bounds.isEmpty()||panelBounds.isEmpty()||windows==null)return;
        selected.set(bounds);panel.set(panelBounds);backdrop=image;
        if(!attached){
            WindowManager.LayoutParams params=new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                            |WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,PixelFormat.TRANSLUCENT);
            params.gravity=Gravity.TOP|Gravity.LEFT;
            params.setTitle("Home power selection");
            windows.addView(view,params);attached=true;
        }
        view.invalidate();
    }

    void close(){
        if(attached){try{windows.removeViewImmediate(view);}catch(IllegalArgumentException ignored){}attached=false;}
        backdrop=null;selected.setEmpty();panel.setEmpty();
    }
}
