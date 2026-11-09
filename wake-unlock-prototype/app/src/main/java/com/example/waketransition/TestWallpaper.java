package com.example.waketransition;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;

public class TestWallpaper extends WallpaperService {
    @Override public Engine onCreateEngine() { return new Renderer(); }
    final class Renderer extends Engine {
        private final Handler handler = new Handler(Looper.getMainLooper());
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final KeyguardManager keyguard = (KeyguardManager)getSystemService(Context.KEYGUARD_SERVICE);
        private final Runnable drawTask = this::draw;
        private boolean visible, registered, needsWake = true, home;
        private long wakeStart, unlockStart;
        private final BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override public void onReceive(Context context, Intent intent) {
                String action=intent.getAction();
                if (Intent.ACTION_SCREEN_OFF.equals(action)) { needsWake=true; home=false; unlockStart=0; }
                else if (Intent.ACTION_SCREEN_ON.equals(action)) { needsWake=true; if (visible) beginWake(); }
                else if (Intent.ACTION_USER_PRESENT.equals(action)) { home=true; unlockStart=SystemClock.uptimeMillis(); schedule(); }
            }
        };
        @Override public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);
            IntentFilter filter=new IntentFilter(); filter.addAction(Intent.ACTION_SCREEN_ON); filter.addAction(Intent.ACTION_SCREEN_OFF); filter.addAction(Intent.ACTION_USER_PRESENT);
            if (android.os.Build.VERSION.SDK_INT >= 33) registerReceiver(receiver,filter,Context.RECEIVER_NOT_EXPORTED); else registerReceiver(receiver,filter);
            registered=true;
        }
        @Override public void onVisibilityChanged(boolean v) {
            visible=v;
            if (v) {
                if (needsWake || wakeStart==0) beginWake();
                else if (!keyguard.isKeyguardLocked() && !home) {home=true;unlockStart=SystemClock.uptimeMillis();}
                schedule();
            } else handler.removeCallbacks(drawTask);
        }
        private void beginWake() { wakeStart=SystemClock.uptimeMillis(); needsWake=false; home=!keyguard.isKeyguardLocked(); unlockStart=0; schedule(); }
        private void schedule() { handler.removeCallbacks(drawTask); if(visible) handler.post(drawTask); }
        @Override public void onSurfaceChanged(SurfaceHolder holder,int format,int width,int height) { super.onSurfaceChanged(holder,format,width,height); schedule(); }
        @Override public void onDestroy() { handler.removeCallbacks(drawTask); if(registered) unregisterReceiver(receiver); super.onDestroy(); }
        private float clamp(float x) {return Math.max(0f,Math.min(1f,x));}
        private int mix(int a,int b,float t) {return (int)(a+(b-a)*t);}
        private void draw() {
            if(!visible) return;
            Canvas c=null; SurfaceHolder holder=getSurfaceHolder();
            try {
                c=holder.lockCanvas(); if(c==null) return;
                long now=SystemClock.uptimeMillis(); float w=c.getWidth(),h=c.getHeight();
                float wake=clamp((now-wakeStart)/1400f);
                float change=home ? (unlockStart==0?1f:clamp((now-unlockStart)/1200f)) : 0f;
                c.drawColor(Color.rgb(mix(22,4,change),mix(9,24,change),mix(19,53,change)));
                float x=w*.5f,y=h*.48f,r=Math.min(w,h)*(.22f+.10f*wake);
                int glow=Color.rgb(mix(255,48,change),mix(153,158,change),mix(29,255,change));
                paint.setShader(new RadialGradient(x,y,r*2.2f,new int[]{glow,Color.argb(90,Color.red(glow),Color.green(glow),Color.blue(glow)),Color.TRANSPARENT},null,Shader.TileMode.CLAMP)); c.drawCircle(x,y,r*2.2f,paint);paint.setShader(null);
                paint.setColor(Color.argb(220,255,248,220)); c.drawCircle(x,y,r*.25f,paint);
                paint.setColor(Color.argb(190,255,235,170));paint.setStrokeWidth(Math.max(2,w*.003f));
                c.save();c.rotate((now-wakeStart)*.012f,x,y);
                for(int i=0;i<28;i++){double angle=Math.PI*2*i/28;float inner=r*(.45f+.20f*wake),outer=r*(.6f+.6f*wake);c.drawLine(x+(float)Math.cos(angle)*inner,y+(float)Math.sin(angle)*inner,x+(float)Math.cos(angle)*outer,y+(float)Math.sin(angle)*outer,paint);}c.restore();
                paint.setTextAlign(Paint.Align.CENTER);paint.setTextSize(w*.038f);paint.setColor(Color.WHITE); c.drawText(home?"HOME TRANSITION":"LOCK SCREEN WAKE",w*.5f,h*.78f,paint);
            } catch (IllegalArgumentException ignored) {
            } finally { if(c!=null) holder.unlockCanvasAndPost(c); if(visible) handler.postDelayed(drawTask,33); }
        }
    }
}
