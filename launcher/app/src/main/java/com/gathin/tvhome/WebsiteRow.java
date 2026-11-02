package com.gathin.tvhome;

import android.app.AlertDialog;
import android.content.*;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.*;
import android.widget.*;
import org.json.*;

/** User-configured links opened in the installed browser. Stores only on this TV. */
public final class WebsiteRow extends LinearLayout {
    private final LinearLayout links;
    private final HorizontalScrollView scroll;
    private final int columns;
    private final boolean animate;
    private JSONArray sites;
    private TextView title;
    private int moving=-1;
    private boolean consumeSelectUp;
    private android.animation.ObjectAnimator movePulse;
    private int firstVisible;
    private static final int[] COLORS={0xff232730,0xff234d73,0xff245c58,0xff3d5740,0xff594779,0xff723d53,0xff795334,0xff683a3e};
    private static final String[] COLOR_NAMES={"Default","Ocean blue","Teal","Forest green","Violet","Rose","Amber","Burgundy"};
    private int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
    public WebsiteRow(Context c,AttributeSet a){
        super(c,a);Config config=Config.load(c);columns=config.columns;animate=config.animations&&!config.reducedMotion;setOrientation(VERTICAL);setClipChildren(false);setClipToPadding(false);
        title=new TextView(c);title.setText(R.string.dashboard_websites);title.setTextSize(16);title.setTextColor(0xfff0f1f5);addView(title,new LayoutParams(-1,dp(28)));
        scroll=new HorizontalScrollView(c){
            @Override public boolean requestChildRectangleOnScreen(View child,android.graphics.Rect rect,boolean immediate){return false;}
            @Override protected int computeScrollDeltaToGetChildRectOnScreen(android.graphics.Rect rect){return 0;}
        };scroll.setHorizontalScrollBarEnabled(false);scroll.setHorizontalFadingEdgeEnabled(false);scroll.setOverScrollMode(OVER_SCROLL_NEVER);scroll.setClipChildren(true);scroll.setClipToPadding(true);scroll.setPadding(0,dp(4),0,dp(4));
        links=new LinearLayout(c);links.setOrientation(HORIZONTAL);scroll.addView(links);addView(scroll,new LayoutParams(-1,0,1));
        try{sites=new JSONArray(c.getSharedPreferences("dashboard_sites",0).getString("links","[]"));}catch(JSONException e){sites=new JSONArray();}
        render();
    }
    private GradientDrawable background(boolean focus,int color){GradientDrawable b=new GradientDrawable();b.setColor(color);b.setCornerRadius(dp(10));b.setStroke(dp(focus?2:1),focus?Color.WHITE:0xff3b414d);return b;}
    private android.graphics.drawable.StateListDrawable background(int color){
        android.graphics.drawable.StateListDrawable states=new android.graphics.drawable.StateListDrawable();
        states.addState(new int[]{android.R.attr.state_focused},background(true,color));
        states.addState(new int[]{},background(false,color));return states;
    }
    void focusFirst(){if(links.getChildCount()>0)links.getChildAt(0).requestFocus();}
    @Override protected void onMeasure(int widthSpec,int heightSpec){
        int w=View.MeasureSpec.getSize(widthSpec);
        for(int i=0;i<links.getChildCount();i++){
            android.view.ViewGroup.LayoutParams lp=links.getChildAt(i).getLayoutParams();
            int width=(w+dp(16))/columns-dp(16);
            if(lp.width!=width){lp.width=width;links.getChildAt(i).setLayoutParams(lp);}
        }
        super.onMeasure(widthSpec,heightSpec);
    }
    private void render(){
        stopPulse();
        links.removeAllViews();
        for(int i=0;i<sites.length();i++){
            JSONObject item=sites.optJSONObject(i);if(item==null)continue;
            TextView b=tile(item.optString("name"),item.optInt("color",COLORS[0]));b.setOnClickListener(v->{if(moving>=0)finishMove();else open(item.optString("url"));});
            b.setOnLongClickListener(v->{if(moving>=0)return true;final int index=links.indexOfChild(v);new AlertDialog.Builder(getContext()).setTitle(item.optString("name")).setItems(new String[]{"Move","Edit website","Change colour","Remove website"},(dialog,which)->{if(which==0){moving=index;post(()->{if(moving==index)render();});}else if(which==1)edit(index);else if(which==2)chooseColor(index);else{sites.remove(index);save();render();}}).show();return true;});
        }
        tile("+  Add website",COLORS[0]).setOnClickListener(v->edit(-1));
        if(moving>=0&&moving<sites.length()){
            title.setText("Move left or right, then press OK");
            View selected=links.getChildAt(moving);selected.post(()->{if(moving>=0&&selected.isAttachedToWindow())selected.requestFocus();});
            if(animate){movePulse=android.animation.ObjectAnimator.ofFloat(selected,"alpha",1f,0.6f,1f);
                movePulse.setDuration(800);movePulse.setRepeatCount(android.animation.ValueAnimator.INFINITE);movePulse.start();}
        }else title.setText(R.string.dashboard_websites);
    }
    private void stopPulse(){if(movePulse!=null){movePulse.cancel();movePulse=null;}for(int i=0;i<links.getChildCount();i++)links.getChildAt(i).setAlpha(1f);}
    void finishMove(){boolean changed=moving>=0;moving=-1;consumeSelectUp=false;stopPulse();title.setText(R.string.dashboard_websites);if(changed)save();}
    boolean moveKey(int key,KeyEvent event){
        boolean select=key==KeyEvent.KEYCODE_DPAD_CENTER||key==KeyEvent.KEYCODE_ENTER||key==KeyEvent.KEYCODE_NUMPAD_ENTER;
        if(select&&consumeSelectUp&&event.getAction()==KeyEvent.ACTION_UP){consumeSelectUp=false;return true;}
        if(moving<0)return false;
        if(event.getAction()==KeyEvent.ACTION_DOWN){
            if(select||key==KeyEvent.KEYCODE_BACK||key==KeyEvent.KEYCODE_ESCAPE){finishMove();if(select)consumeSelectUp=true;}
            else if(key==KeyEvent.KEYCODE_DPAD_LEFT||key==KeyEvent.KEYCODE_DPAD_RIGHT){
                int next=moving+(key==KeyEvent.KEYCODE_DPAD_LEFT?-1:1);
                if(next>=0&&next<sites.length())try{Object item=sites.get(moving);sites.put(moving,sites.get(next));sites.put(next,item);View card=links.getChildAt(moving);links.removeViewAt(moving);links.addView(card,next);moving=next;save();card.post(()->{card.requestFocus();reveal(card);});}catch(JSONException ignored){}
            }
        }
        return select||key==KeyEvent.KEYCODE_BACK||key==KeyEvent.KEYCODE_ESCAPE||key==KeyEvent.KEYCODE_DPAD_LEFT||key==KeyEvent.KEYCODE_DPAD_RIGHT||key==KeyEvent.KEYCODE_DPAD_UP||key==KeyEvent.KEYCODE_DPAD_DOWN;
    }
    @Override protected void onDetachedFromWindow(){finishMove();super.onDetachedFromWindow();}
    private void reveal(View card){
        int index=links.indexOfChild(card);if(index<0)return;
        if(index<firstVisible)firstVisible=index;
        else if(index>=firstVisible+columns)firstVisible=index-columns+1;
        firstVisible=Math.max(0,Math.min(firstVisible,links.getChildCount()-columns));
        int stride=card.getWidth()+dp(16);
        if(animate)scroll.smoothScrollTo(firstVisible*stride,0);else scroll.scrollTo(firstVisible*stride,0);
    }
    private TextView tile(String text,int color){
        TextView b=new TextView(getContext());b.setText(text);b.setTextSize(14);b.setTextColor(0xffe5e7ee);b.setGravity(Gravity.CENTER);b.setSingleLine(true);b.setEllipsize(android.text.TextUtils.TruncateAt.END);b.setPadding(dp(12),0,dp(12),0);b.setFocusable(true);b.setClickable(true);b.setBackground(background(color));
        b.setFocusableInTouchMode(true);
        int width=getWidth()>0?(getWidth()+dp(16))/columns-dp(16):dp(200);
        LayoutParams lp=new LayoutParams(width,dp(58));lp.setMarginEnd(dp(16));links.addView(b,lp);
        b.setOnKeyListener((v,key,event)->{if(moveKey(key,event))return true;if(key==KeyEvent.KEYCODE_DPAD_LEFT||key==KeyEvent.KEYCODE_DPAD_RIGHT){if(event.getAction()==KeyEvent.ACTION_DOWN){int next=links.indexOfChild(v)+(key==KeyEvent.KEYCODE_DPAD_LEFT?-1:1);if(next>=0&&next<links.getChildCount())links.getChildAt(next).requestFocus();}return true;}if(key==KeyEvent.KEYCODE_DPAD_UP||key==KeyEvent.KEYCODE_DPAD_DOWN){if(event.getAction()==KeyEvent.ACTION_DOWN&&getContext() instanceof MainActivity){int column=DashboardGeometry.visibleColumn(links.indexOfChild(v),firstVisible,columns);if(key==KeyEvent.KEYCODE_DPAD_UP)((MainActivity)getContext()).focusDashboardApps(column);else ((MainActivity)getContext()).focusDashboardOtherApps(column);}return true;}return false;});
        b.setOnFocusChangeListener((v,focused)->{if(focused)v.post(()->reveal(v));});return b;
    }
    private void chooseColor(int index){
        JSONObject item=sites.optJSONObject(index);if(item==null)return;
        CharSequence[] choices=new CharSequence[COLORS.length];int selected=0;
        for(int i=0;i<COLORS.length;i++){
            android.text.SpannableString label=new android.text.SpannableString("\u25cf   "+COLOR_NAMES[i]);
            label.setSpan(new android.text.style.ForegroundColorSpan(i==0?0xffa6a9b3:COLORS[i]),0,1,android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            choices[i]=label;if(item.optInt("color",COLORS[0])==COLORS[i])selected=i;
        }
        new AlertDialog.Builder(getContext()).setTitle("Website colour")
            .setSingleChoiceItems(choices,selected,(dialog,which)->{
                try{item.put("color",COLORS[which]);save();render();links.getChildAt(index).requestFocus();}catch(JSONException ignored){}
                dialog.dismiss();
            }).setNegativeButton("Cancel",null).show();
    }
    private void edit(int index){
        LinearLayout form=new LinearLayout(getContext());form.setOrientation(VERTICAL);form.setPadding(dp(24),dp(8),dp(24),dp(8));
        EditText name=new EditText(getContext());name.setHint("Website name");name.setSingleLine(true);form.addView(name);
        EditText url=new EditText(getContext());url.setHint("https://example.com");url.setSingleLine(true);url.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_VARIATION_URI);form.addView(url);
        JSONObject old=sites.optJSONObject(index);if(old!=null){name.setText(old.optString("name"));url.setText(old.optString("url"));}
        AlertDialog dialog=new AlertDialog.Builder(getContext()).setTitle(index<0?"Add website":"Edit website").setView(form).setNegativeButton("Cancel",null).setPositiveButton("Save",null).create();
        dialog.setOnShowListener(v->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(button->{
            String address=url.getText().toString().trim();if(!address.contains("://"))address="https://"+address;
            Uri parsed=Uri.parse(address);if(!("https".equals(parsed.getScheme())||"http".equals(parsed.getScheme()))||parsed.getHost()==null||parsed.getHost().isEmpty()){url.setError("Enter a valid website address");return;}
            String label=name.getText().toString().trim();if(label.isEmpty())label=parsed.getHost();
            try{JSONObject item=new JSONObject().put("name",label).put("url",address).put("color",old==null?COLORS[0]:old.optInt("color",COLORS[0]));if(index<0)sites.put(item);else sites.put(index,item);save();render();dialog.dismiss();}catch(JSONException e){url.setError("Unable to save website");}
        }));dialog.show();
    }
    private void save(){getContext().getSharedPreferences("dashboard_sites",0).edit().putString("links",sites.toString()).apply();}
    private void open(String url){try{getContext().startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(url)));}catch(ActivityNotFoundException e){Toast.makeText(getContext(),"Install a browser to open websites",Toast.LENGTH_SHORT).show();}}
}
