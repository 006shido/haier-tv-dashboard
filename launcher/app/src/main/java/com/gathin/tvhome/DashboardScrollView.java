package com.gathin.tvhome;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.ScrollView;

/** DashboardLayout owns focus scrolling; avoid a second native adjustment fighting it. */
public final class DashboardScrollView extends ScrollView {
    public DashboardScrollView(Context context,AttributeSet attributes){super(context,attributes);}
    @Override protected int computeScrollDeltaToGetChildRectOnScreen(Rect rect){return 0;}
}
