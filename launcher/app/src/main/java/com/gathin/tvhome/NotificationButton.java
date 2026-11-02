package com.gathin.tvhome;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;

/** Fixed circular bell with a badge that never changes the top-bar layout. */
public final class NotificationButton extends android.widget.TextView {
    private final Paint badgePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint numberPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int unread;

    public NotificationButton(Context context, AttributeSet attributes) {
        super(context, attributes);
        badgePaint.setColor(0xffdceaff);
        numberPaint.setColor(0xff162539);
        numberPaint.setTextAlign(Paint.Align.CENTER);
        numberPaint.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        numberPaint.setTextSize(8 * getResources().getDisplayMetrics().scaledDensity);
    }

    void setUnreadCount(int count) {
        if (unread == count) return;
        unread = Math.max(0, count);
        invalidate();
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (unread == 0) return;
        float density = getResources().getDisplayMetrics().density;
        float radius = 8 * density;
        float x = getWidth() - radius, y = radius;
        canvas.drawCircle(x, y, radius, badgePaint);
        Paint.FontMetrics metrics = numberPaint.getFontMetrics();
        canvas.drawText(unread > 99 ? "99+" : String.valueOf(unread), x,
                y - (metrics.ascent + metrics.descent) / 2, numberPaint);
    }
}
