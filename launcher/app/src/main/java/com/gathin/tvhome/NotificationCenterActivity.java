package com.gathin.tvhome;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

/** Three-card viewport with a continuous, one-card scrolling track. */
public final class NotificationCenterActivity extends Activity {
    private static final int PAGE_SIZE=3;
    private final List<JSONObject> entries=new ArrayList<>();
    private LinearLayout panel;
    private RecyclerView rows;
    private LinearLayoutManager layout;
    private final NotificationAdapter adapter=new NotificationAdapter();
    private final java.util.Map<String,AppMeta> appMeta=new java.util.HashMap<>();
    private int pendingFocus=-1;
    private boolean animate;
    private FrameLayout listViewport;
    private TextView settingsAction;
    private int cardStep;
    private TextView subtitle, counter, close;
    private int pageStart;
    private boolean closing;
    private final SharedPreferences.OnSharedPreferenceChangeListener changes=(p,k)->{
        if("entries".equals(k))runOnUiThread(this::reload);
    };
    private int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
    private TextView text(String value,int size,int color){
        TextView v=new TextView(this);v.setText(value);v.setTextSize(size);v.setTextColor(color);
        v.setTypeface(Typeface.create("sans-serif",Typeface.NORMAL));v.setIncludeFontPadding(false);return v;
    }
    private GradientDrawable shape(int fill,int stroke,int radius){
        GradientDrawable d=new GradientDrawable();d.setColor(fill);d.setCornerRadius(dp(radius));
        if(stroke!=0)d.setStroke(dp(1),stroke);return d;
    }
    private StateListDrawable background(int normal,int radius){
        StateListDrawable d=new StateListDrawable();
        d.addState(new int[]{android.R.attr.state_focused},shape(0xffeeeeF2,0,radius));
        d.addState(new int[]{},shape(normal,0,radius));return d;
    }
    private ColorStateList ink(int normal,int focused){
        return new ColorStateList(new int[][]{new int[]{android.R.attr.state_focused},new int[]{}},new int[]{focused,normal});
    }
    private TextView action(String label,Runnable run){
        TextView v=text(label,11,0xffb2b2bd);v.setGravity(Gravity.CENTER);v.setFocusableInTouchMode(true);v.setClickable(true);
        v.setBackground(background(0xb3242c38,10));v.setTextColor(ink(0xffbebec8,0xff17171b));
        v.setOnClickListener(w->run.run());return v;
    }
    @Override public void onCreate(Bundle state){
        super.onCreate(state);overridePendingTransition(0,0);
        Config config=Config.load(this);animate=config.animations&&!config.reducedMotion;
        FrameLayout root=new FrameLayout(this);root.setBackgroundColor(0x33000000);root.setOnClickListener(v->finish());
        panel=new LinearLayout(this);panel.setOrientation(LinearLayout.VERTICAL);panel.setPadding(dp(18),dp(22),dp(18),dp(16));panel.setClickable(true);
        GradientDrawable shell=new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,new int[]{0xd91c2430,0xe8131923});
        shell.setCornerRadius(dp(22));shell.setStroke(dp(1),0x55a4b5cb);panel.setBackground(shell);
        int width=Math.round(getResources().getDisplayMetrics().widthPixels/3f);
        FrameLayout.LayoutParams frame=new FrameLayout.LayoutParams(width,-1,Gravity.END);frame.setMargins(dp(12),dp(12),dp(12),dp(12));root.addView(panel,frame);
        LinearLayout header=new LinearLayout(this);header.setGravity(Gravity.CENTER_VERTICAL);
        TextView title=text("Notifications",23,0xfff5f5f7);title.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));
        header.addView(title,new LinearLayout.LayoutParams(0,-2,1));
        close=action("×",this::finish);close.setTextSize(18);close.setContentDescription("Close notifications");
        header.addView(close,new LinearLayout.LayoutParams(dp(28),dp(28)));panel.addView(header);
        subtitle=text("",11,0xff898995);LinearLayout.LayoutParams subLp=new LinearLayout.LayoutParams(-1,-2);subLp.topMargin=dp(8);subLp.bottomMargin=dp(20);panel.addView(subtitle,subLp);
        listViewport=new FrameLayout(this);listViewport.setClipChildren(true);listViewport.setClipToPadding(true);
        rows=new RecyclerView(this);
        layout=new LinearLayoutManager(this){
            @Override public boolean requestChildRectangleOnScreen(RecyclerView parent,View child,
                    android.graphics.Rect rect,boolean immediate,boolean focusedChildVisible){return false;}
        };
        rows.setLayoutManager(layout);rows.setItemAnimator(null);rows.setOverScrollMode(View.OVER_SCROLL_NEVER);
        rows.setClipToPadding(true);rows.setAdapter(adapter);
        rows.addOnScrollListener(new RecyclerView.OnScrollListener(){
            @Override public void onScrollStateChanged(RecyclerView view,int state){if(state==RecyclerView.SCROLL_STATE_IDLE)applyPendingFocus();}
        });
        rows.addOnChildAttachStateChangeListener(new RecyclerView.OnChildAttachStateChangeListener(){
            @Override public void onChildViewAttachedToWindow(View view){rows.post(NotificationCenterActivity.this::applyPendingFocus);}
            @Override public void onChildViewDetachedFromWindow(View view){}
        });
        listViewport.addView(rows,new FrameLayout.LayoutParams(-1,-1));
        panel.addView(listViewport,new LinearLayout.LayoutParams(-1,0,1));
        listViewport.addOnLayoutChangeListener((v,l,t,r,b,ol,ot,or,ob)->{if(b-t!=ob-ot)layoutCards();});
        LinearLayout pageLine=new LinearLayout(this);pageLine.setGravity(Gravity.CENTER_VERTICAL);pageLine.setPadding(0,dp(10),0,dp(12));
        counter=text("",10,0xff90909d);pageLine.addView(counter,new LinearLayout.LayoutParams(0,-2,1));
        TextView hint=text("↑ ↓  Browse",10,0xff696975);pageLine.addView(hint);panel.addView(pageLine);
        LinearLayout footer=new LinearLayout(this);TextView access=action("Notification settings",this::access),clear=action("Clear all",this::clear);
        settingsAction=access;
        LinearLayout.LayoutParams left=new LinearLayout.LayoutParams(0,dp(34),1);left.rightMargin=dp(8);footer.addView(access,left);footer.addView(clear,new LinearLayout.LayoutParams(0,dp(34),1));panel.addView(footer);
        setContentView(root);close.requestFocus();panel.setTranslationX(width+dp(24));
        if(animate)panel.animate().translationX(0).setDuration(220).setInterpolator(new android.view.animation.DecelerateInterpolator()).start();
        else panel.setTranslationX(0);
        close.setOnKeyListener((v,key,event)->{if(key!=KeyEvent.KEYCODE_DPAD_DOWN||entries.isEmpty())return false;if(event.getAction()==KeyEvent.ACTION_DOWN)focusNotification(pageStart);return true;});
    }
    @Override public void finish(){
        if(closing)return;closing=true;if(panel==null){super.finish();return;}
        if(!animate){super.finish();return;}
        panel.animate().translationX(panel.getWidth()+dp(24)).setDuration(160).withEndAction(()->{super.finish();overridePendingTransition(0,0);}).start();
    }
    @Override protected void onResume(){super.onResume();NotificationInbox.markRead(this);NotificationInbox.prefs(this).registerOnSharedPreferenceChangeListener(changes);reload();}
    @Override protected void onPause(){NotificationInbox.prefs(this).unregisterOnSharedPreferenceChangeListener(changes);NotificationInbox.markRead(this);super.onPause();}
    private boolean allowed(){
        ComponentName component=new ComponentName(this,TvNotificationListener.class);
        if(android.os.Build.VERSION.SDK_INT>=27)return getSystemService(NotificationManager.class).isNotificationListenerAccessGranted(component);
        String value=Settings.Secure.getString(getContentResolver(),"enabled_notification_listeners");
        if(value!=null)for(String key:value.split(":"))if(component.equals(ComponentName.unflattenFromString(key)))return true;return false;
    }
    private void access(){try{startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));}catch(android.content.ActivityNotFoundException e){Toast.makeText(this,"This TV does not expose notification access settings.",Toast.LENGTH_SHORT).show();}}
    private void clear(){new AlertDialog.Builder(this).setTitle("Clear notification history?").setMessage("This clears the list here. Your apps stay unchanged.").setNegativeButton("Cancel",null).setPositiveButton("Clear",(d,w)->NotificationInbox.prefs(this).edit().remove("entries").apply()).show();}
    private void reload(){
        String focused=getCurrentFocus()!=null&&getCurrentFocus().getTag() instanceof String?(String)getCurrentFocus().getTag():null;
        entries.clear();JSONArray saved=NotificationInbox.entries(this);
        for(int i=0;i<saved.length();i++){JSONObject row=saved.optJSONObject(i);if(row!=null)entries.add(row);}
        entries.sort((a,b)->Long.compare(b.optLong("time"),a.optLong("time")));
        int focusIndex=-1;for(int i=0;i<entries.size();i++)if(entries.get(i).optString("id").equals(focused))focusIndex=i;
        if(focusIndex>=0){if(focusIndex<pageStart)pageStart=focusIndex;else if(focusIndex>=pageStart+PAGE_SIZE)pageStart=focusIndex-PAGE_SIZE+1;}
        pageStart=Math.min(pageStart,Math.max(0,entries.size()-PAGE_SIZE));
        subtitle.setText(!allowed()?"Turn on notification access to receive updates":entries.isEmpty()?"You're all caught up":entries.size()+" updates · Latest first");
        renderPage(focusIndex>=0?focusIndex-pageStart:-1);
    }
    private void renderPage(int focusSlot){
        updateCounter();layoutCards();adapter.notifyDataSetChanged();
        if(entries.isEmpty()){
            subtitle.setText(allowed()?"You're all caught up":"Turn on notification access to receive updates");
            close.requestFocus();
        }else{
            layout.scrollToPositionWithOffset(pageStart,0);
            if(focusSlot>=0)rows.post(()->focusNotification(Math.min(pageStart+focusSlot,entries.size()-1)));
        }
    }
    private void layoutCards(){
        int height=listViewport.getHeight();if(height<=0)return;
        int step=Math.max(1,(height-dp(16))/PAGE_SIZE)+dp(8);
        if(step!=cardStep){cardStep=step;adapter.notifyDataSetChanged();layout.scrollToPositionWithOffset(pageStart,0);}
    }
    private void updateCounter(){counter.setText(entries.isEmpty()?"":(pageStart+1)+"–"+Math.min(entries.size(),pageStart+PAGE_SIZE)+" of "+entries.size());}
    private void scrollWindow(int position){
        int next=pageStart;
        if(position<next)next=position;
        else if(position>=next+PAGE_SIZE)next=position-PAGE_SIZE+1;
        next=Math.max(0,Math.min(next,Math.max(0,entries.size()-PAGE_SIZE)));
        if(next==pageStart)return;
        pageStart=next;updateCounter();
        View first=rows.getChildAt(0);
        int current=first==null?0:rows.getChildAdapterPosition(first)*cardStep-layout.getDecoratedTop(first);
        int delta=pageStart*cardStep-current;
        if(animate)rows.smoothScrollBy(0,delta);else rows.scrollBy(0,delta);
    }
    private void focusNotification(int position){
        if(position<0||position>=entries.size())return;
        pendingFocus=position;scrollWindow(position);applyPendingFocus();
    }
    private void applyPendingFocus(){
        if(pendingFocus<0)return;
        RecyclerView.ViewHolder holder=rows.findViewHolderForAdapterPosition(pendingFocus);
        if(holder!=null&&holder.itemView instanceof FrameLayout){
            View card=((FrameLayout)holder.itemView).getChildAt(0);
            if(card!=null){pendingFocus=-1;card.requestFocus();}
        }
    }
    private void revealCard(View card){
        Object id=card.getTag();
        for(int i=0;i<entries.size();i++)if(entries.get(i).optString("id").equals(id)){scrollWindow(i);return;}
    }
    private final class NotificationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
        @Override public int getItemCount(){return entries.isEmpty()?1:entries.size();}
        @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent,int type){
            FrameLayout frame=new FrameLayout(parent.getContext());
            frame.setLayoutParams(new RecyclerView.LayoutParams(-1,-2));
            return new RecyclerView.ViewHolder(frame){};
        }
        @Override public void onBindViewHolder(RecyclerView.ViewHolder holder,int position){
            FrameLayout frame=(FrameLayout)holder.itemView;frame.removeAllViews();
            RecyclerView.LayoutParams lp=(RecyclerView.LayoutParams)frame.getLayoutParams();
            lp.height=entries.isEmpty()?Math.max(dp(100),listViewport.getHeight()):Math.max(dp(40),cardStep)-(position==entries.size()-1?dp(8):0);frame.setLayoutParams(lp);
            if(entries.isEmpty()){
                TextView empty=text("Nothing new right now\nYour updates will appear here.",15,0xff9b9ba8);empty.setGravity(Gravity.CENTER);frame.addView(empty,new FrameLayout.LayoutParams(-1,-1));return;
            }
            LinearLayout card=card(entries.get(position));
            card.setOnKeyListener((v,key,event)->{
                if(key!=KeyEvent.KEYCODE_DPAD_UP&&key!=KeyEvent.KEYCODE_DPAD_DOWN)return false;
                if(event.getAction()==KeyEvent.ACTION_DOWN){
                    int current=holder.getBindingAdapterPosition();if(current==RecyclerView.NO_POSITION)return true;
                    int next=current+(key==KeyEvent.KEYCODE_DPAD_DOWN?1:-1);
                    if(next<0)close.requestFocus();else if(next>=entries.size())settingsAction.requestFocus();else focusNotification(next);
                }return true;
            });
            FrameLayout.LayoutParams cardLp=new FrameLayout.LayoutParams(-1,-1);if(position<entries.size()-1)cardLp.bottomMargin=dp(8);frame.addView(card,cardLp);
        }
    }
    private static final class AppMeta{
        final String label;final android.graphics.drawable.Drawable.ConstantState icon;
        AppMeta(String label,android.graphics.drawable.Drawable icon){this.label=label;this.icon=icon==null?null:icon.getConstantState();}
    }
    private LinearLayout card(JSONObject row){
        String pkg=row.optString("pkg");AppMeta metaInfo=appMeta.get(pkg);
        if(metaInfo==null){
            String label=pkg;android.graphics.drawable.Drawable icon=null;
            try{android.content.pm.ApplicationInfo app=getPackageManager().getApplicationInfo(pkg,0);label=getPackageManager().getApplicationLabel(app).toString();icon=getPackageManager().getApplicationIcon(app);}catch(android.content.pm.PackageManager.NameNotFoundException ignored){}
            metaInfo=new AppMeta(label,icon);appMeta.put(pkg,metaInfo);
        }
        String label=metaInfo.label;android.graphics.drawable.Drawable icon=metaInfo.icon==null?null:metaInfo.icon.newDrawable(getResources());
        LinearLayout card=new LinearLayout(this);card.setOrientation(LinearLayout.VERTICAL);card.setGravity(Gravity.CENTER_VERTICAL);card.setPadding(dp(14),dp(12),dp(14),dp(12));
        card.setBackground(background(0xaa262e3a,14));card.setFocusableInTouchMode(true);card.setClickable(true);card.setTag(row.optString("id"));
        LinearLayout meta=new LinearLayout(this);meta.setGravity(Gravity.CENTER_VERTICAL);
        if(icon!=null){ImageView image=new ImageView(this);image.setImageDrawable(icon);LinearLayout.LayoutParams ilp=new LinearLayout.LayoutParams(dp(22),dp(22));ilp.rightMargin=dp(9);meta.addView(image,ilp);}
        TextView appName=text(label,10,0xffa9a9b6);appName.setSingleLine(true);appName.setEllipsize(TextUtils.TruncateAt.END);meta.addView(appName,new LinearLayout.LayoutParams(0,-2,1));
        String time=android.text.format.DateUtils.getRelativeTimeSpanString(row.optLong("time"),System.currentTimeMillis(),60000).toString();TextView when=text(time,9,0xff7c7c89);meta.addView(when);card.addView(meta);
        String heading=row.optString("title"),body=row.optString("text");if(heading.isEmpty()||"Notification".equals(heading))heading=label;
        TextView title=text(heading,14,0xfff3f3f6);title.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));title.setSingleLine(true);title.setEllipsize(TextUtils.TruncateAt.END);
        LinearLayout.LayoutParams titleLp=new LinearLayout.LayoutParams(-1,-2);titleLp.topMargin=dp(9);card.addView(title,titleLp);
        TextView detail=text(body.equals(heading)?"":body,11,0xffaaaab6);detail.setMaxLines(2);detail.setEllipsize(TextUtils.TruncateAt.END);detail.setLineSpacing(dp(2),1);
        if(!detail.getText().toString().isEmpty()){LinearLayout.LayoutParams blp=new LinearLayout.LayoutParams(-1,-2);blp.topMargin=dp(4);card.addView(detail,blp);}
        card.setOnFocusChangeListener((v,focus)->{appName.setTextColor(focus?0xff62626e:0xffa9a9b6);when.setTextColor(focus?0xff70707b:0xff7c7c89);title.setTextColor(focus?0xff18181d:0xfff3f3f6);detail.setTextColor(focus?0xff595965:0xffaaaab6);if(focus)revealCard(v);});
        card.setOnClickListener(v->{if(!TvNotificationListener.open(row.optString("id")))new AlertDialog.Builder(this).setTitle(row.optString("title")).setMessage(row.optString("text")).setPositiveButton("Done",null).show();});
        return card;
    }
}

