package com.example.waketransition;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    static final String PREFS="wallpaper_settings", WAKE="wake_video", UNLOCK="unlock_video";
    private static final int PICK_WAKE=101, PICK_UNLOCK=102;
    private SharedPreferences prefs;
    private TextView wakeStatus, unlockStatus;
    @Override public void onCreate(Bundle b){
        super.onCreate(b); prefs=getSharedPreferences(PREFS,MODE_PRIVATE);
        ScrollView scroll=new ScrollView(this);scroll.setBackgroundColor(Color.rgb(13,17,29));
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(22),dp(38),dp(22),dp(28));scroll.addView(root);
        TextView title=text("Motion Wallpaper",30,0xfff8f9ff);title.setTypeface(null,Typeface.BOLD);root.addView(title);
        TextView subtitle=text("Create a moving lock screen and a scene for unlocking.",16,0xffb5bfd4);subtitle.setPadding(0,dp(8),0,dp(22));root.addView(subtitle);
        root.addView(card("01  SCREEN WAKES","Plays when the lock screen becomes visible.",true));
        root.addView(card("02  PHONE UNLOCKS","Plays after the phone detects an unlock. Optional: uses your wake video if empty.",false));
        Button apply=button("Preview and apply wallpaper",0xff7958eb);LinearLayout.LayoutParams a=new LinearLayout.LayoutParams(-1,dp(56));a.topMargin=dp(22);root.addView(apply,a);
        apply.setOnClickListener(v->{if(prefs.getString(WAKE,null)==null){toast("Choose a screen-wake video first");return;} Intent i=new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER); i.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,new ComponentName(this,TestWallpaper.class)); try{startActivity(i);}catch(Exception e){toast("Wallpaper picker is not available on this phone");}});
        TextView note=text("For best results choose short portrait videos with matching scenes. If your phone offers it, apply to both Home and Lock screens. The exact unlock timing varies by device.",13,0xffaebbd0);note.setPadding(0,dp(20),0,0);root.addView(note);setContentView(scroll);update();
    }
    private LinearLayout card(String heading,String description,boolean wake){
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(18),dp(18),dp(18),dp(18));GradientDrawable bg=new GradientDrawable();bg.setColor(0xff1c2335);bg.setCornerRadius(dp(18));box.setBackground(bg);LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,-2);bp.bottomMargin=dp(12);box.setLayoutParams(bp);
        TextView h=text(heading,15,0xffd4cbff);h.setTypeface(null,Typeface.BOLD);box.addView(h);TextView d=text(description,14,0xffd0d7e5);d.setPadding(0,dp(8),0,dp(10));box.addView(d);
        TextView status=text("No video selected",13,0xffa5b1c8);box.addView(status);if(wake)wakeStatus=status;else unlockStatus=status;
        LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.HORIZONTAL);LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,-2);rp.topMargin=dp(12);box.addView(row,rp);
        Button choose=button("Choose video",0xff5540a4);row.addView(choose,new LinearLayout.LayoutParams(0,dp(48),1));choose.setOnClickListener(v->choose(wake?PICK_WAKE:PICK_UNLOCK));
        Button preview=button("Play",0xff364157);LinearLayout.LayoutParams pp=new LinearLayout.LayoutParams(dp(76),dp(48));pp.leftMargin=dp(8);row.addView(preview,pp);preview.setOnClickListener(v->preview(wake?WAKE:UNLOCK));return box;
    }
    private void choose(int request){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("video/*");i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);try{startActivityForResult(i,request);}catch(Exception e){toast("No video picker is available");}}
    @Override protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);if(result!=RESULT_OK||data==null||data.getData()==null)return;Uri uri=data.getData();try{getContentResolver().takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION);}catch(SecurityException e){toast("Could not retain access to this video. Choose a local file.");return;}prefs.edit().putString(request==PICK_WAKE?WAKE:UNLOCK,uri.toString()).apply();update();}
    private void preview(String key){String value=prefs.getString(key,null);if(value==null){toast("Choose a video first");return;}Intent i=new Intent(Intent.ACTION_VIEW);i.setDataAndType(Uri.parse(value),"video/*");i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);try{startActivity(i);}catch(Exception e){toast("No video player found");}}
    private void update(){wakeStatus.setText(prefs.getString(WAKE,null)==null?"No video selected":"✓ Video selected");unlockStatus.setText(prefs.getString(UNLOCK,null)==null?"No video selected":"✓ Video selected");}
    private Button button(String s,int color){Button b=new Button(this);b.setText(s);b.setTextColor(Color.WHITE);b.setAllCaps(false);GradientDrawable bg=new GradientDrawable();bg.setColor(color);bg.setCornerRadius(dp(12));b.setBackground(bg);return b;}
    private TextView text(String s,int size,int color){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);return t;}
    private int dp(float n){return (int)(getResources().getDisplayMetrics().density*n+.5f);}private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
}
