package com.gathin.tvhome;

import android.app.Activity;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import androidx.recyclerview.widget.RecyclerView;

/** Keeps section ordering and D-pad focus stable in the established scrolling dashboard. */
final class DashboardLayout {
    static void attach(Activity activity,RecyclerView grid,AppAdapter adapter,int columns){
        float density=activity.getResources().getDisplayMetrics().density;
        int gap=Math.round(144*density);
        ScrollView scroll=activity.findViewById(R.id.dashboardScroll);
        View websites=activity.findViewById(R.id.websiteRow);
        View heading=activity.findViewById(R.id.dashboardAppsHeading);
        grid.setNestedScrollingEnabled(false);
        grid.addItemDecoration(new RecyclerView.ItemDecoration(){
            @Override public void getItemOffsets(Rect out,View view,RecyclerView parent,RecyclerView.State state){
                int pos=parent.getChildAdapterPosition(view);
                if(pos>=columns&&pos<columns*2)out.top=gap;
            }
        });
        Runnable resize=()->{
            if(grid.getWidth()==0)return;
            int width=AppAdapter.dashboardTileWidth(grid.getWidth()-grid.getPaddingLeft()-grid.getPaddingRight(),columns,density);
            int row=AppAdapter.itemHeightPx(AppAdapter.tileHeightPx(width),false,density);
            int rows=Math.max(1,(adapter.getItemCount()+columns-1)/columns);
            int height=grid.getPaddingTop()+rows*row+gap+Math.round(24*density);
            ViewGroup.LayoutParams lp=grid.getLayoutParams();if(lp.height!=height){lp.height=height;grid.setLayoutParams(lp);}
            FrameLayout.LayoutParams web=(FrameLayout.LayoutParams)websites.getLayoutParams();
            int top=grid.getPaddingTop()+row+Math.round(8*density);
            if(web.topMargin!=top){web.topMargin=top;websites.setLayoutParams(web);}
            FrameLayout.LayoutParams title=(FrameLayout.LayoutParams)heading.getLayoutParams();
            title.topMargin=top+Math.round(110*density);heading.setLayoutParams(title);
        };
        grid.addOnLayoutChangeListener((v,l,t,r,b,ol,ot,or,ob)->{if(r-l!=or-ol)grid.post(resize);});
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver(){
            @Override public void onChanged(){grid.post(resize);}
        });
        grid.post(resize);
        scroll.getViewTreeObserver().addOnGlobalFocusChangeListener((old,focus)->{
            if(focus==null)return;
            focus.post(()->{
                if(!focus.isFocused()||!focus.isAttachedToWindow())return;
                Rect rect=new Rect();focus.getDrawingRect(rect);
                try{scroll.offsetDescendantRectToMyCoords(focus,rect);}catch(IllegalArgumentException e){return;}
                int margin=Math.round(18*density),bottomExtra=focus.getId()==R.id.card?Math.round(28*density):0;
                int target=scroll.getScrollY();
                if(rect.top<120*density)target=0;
                else if(rect.top<target+margin)target=Math.max(0,rect.top-margin);
                else if(rect.bottom+bottomExtra>target+scroll.getHeight()-margin)target=rect.bottom+bottomExtra-scroll.getHeight()+margin;
                if(target!=scroll.getScrollY())scroll.smoothScrollTo(0,target);
            });
        });
    }
}
