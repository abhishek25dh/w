package com.example.waketransition;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.service.wallpaper.WallpaperService;
import android.view.SurfaceHolder;
import java.io.IOException;

public class TestWallpaper extends WallpaperService {
    @Override public Engine onCreateEngine(){return new VideoEngine();}
    final class VideoEngine extends Engine {
        private final Handler handler=new Handler(Looper.getMainLooper());
        private final SharedPreferences prefs=getSharedPreferences(MainActivity.PREFS,MODE_PRIVATE);
        private final KeyguardManager keyguard=(KeyguardManager)getSystemService(KEYGUARD_SERVICE);
        private MediaPlayer player; private boolean visible,surfaceReady,needsWake=true,registered,destroyed,playingWake=false;
        private int generation=0;private long lastWake=0;
        private final BroadcastReceiver events=new BroadcastReceiver(){@Override public void onReceive(Context c,Intent i){String a=i.getAction();
            if(Intent.ACTION_SCREEN_OFF.equals(a)){needsWake=true;stopPlayer();}
            else if(Intent.ACTION_SCREEN_ON.equals(a)){needsWake=true;handler.postDelayed(()->maybeWake(),100);}
            else if(Intent.ACTION_USER_PRESENT.equals(a)){needsWake=false;playVideo(MainActivity.UNLOCK,false);}
        }};
        private final SharedPreferences.OnSharedPreferenceChangeListener changes=(p,k)->{if(visible&&surfaceReady){needsWake=true;maybeWake();}};
        @Override public void onCreate(SurfaceHolder holder){super.onCreate(holder);IntentFilter f=new IntentFilter();f.addAction(Intent.ACTION_SCREEN_OFF);f.addAction(Intent.ACTION_SCREEN_ON);f.addAction(Intent.ACTION_USER_PRESENT);if(Build.VERSION.SDK_INT>=33)registerReceiver(events,f,Context.RECEIVER_NOT_EXPORTED);else registerReceiver(events,f);registered=true;prefs.registerOnSharedPreferenceChangeListener(changes);}
        @Override public void onSurfaceCreated(SurfaceHolder h){super.onSurfaceCreated(h);surfaceReady=true;if(visible)maybeWake();}
        @Override public void onSurfaceDestroyed(SurfaceHolder h){surfaceReady=false;stopPlayer();super.onSurfaceDestroyed(h);}
        @Override public void onVisibilityChanged(boolean v){visible=v;if(v){if(needsWake)maybeWake();else if(player==null)playVideo(keyguard.isKeyguardLocked()?MainActivity.WAKE:MainActivity.UNLOCK,false);else try{player.start();}catch(IllegalStateException ignored){}}else{if(player!=null)try{if(player.isPlaying())player.pause();}catch(IllegalStateException ignored){}}}
        private void maybeWake(){if(!visible||!surfaceReady||destroyed)return;if(!needsWake&&SystemClock.uptimeMillis()-lastWake<600)return;needsWake=false;lastWake=SystemClock.uptimeMillis();if(keyguard.isKeyguardLocked())playVideo(MainActivity.WAKE,true);else playVideo(MainActivity.UNLOCK,false);}
        private void playVideo(String key,boolean isWake){if(!visible||!surfaceReady||destroyed)return;String value=prefs.getString(key,null);if(value==null)value=prefs.getString(MainActivity.WAKE,null);stopPlayer();if(value==null){drawEmpty();return;} final int token=++generation;playingWake=isWake;MediaPlayer p=new MediaPlayer();player=p;
            try{p.setDataSource(TestWallpaper.this,Uri.parse(value));p.setSurface(getSurfaceHolder().getSurface());p.setLooping(false);p.setVolume(0f,0f);p.setOnPreparedListener(mp->{if(token!=generation||!visible||!surfaceReady){if(token==generation)stopPlayer();return;}try{mp.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);mp.start();}catch(IllegalStateException ignored){}});p.setOnErrorListener((mp,what,extra)->{if(token==generation){stopPlayer();drawEmpty();}return true;});p.prepareAsync();}catch(IOException|SecurityException|IllegalArgumentException|IllegalStateException e){stopPlayer();drawEmpty();}}
        private void drawEmpty(){if(!surfaceReady)return;Canvas c=null;try{c=getSurfaceHolder().lockCanvas();if(c!=null)c.drawColor(Color.rgb(12,16,28));}catch(Exception ignored){}finally{if(c!=null)try{getSurfaceHolder().unlockCanvasAndPost(c);}catch(Exception ignored){}}}
        private void stopPlayer(){generation++;if(player!=null){try{player.reset();player.release();}catch(Exception ignored){}player=null;}}
        @Override public void onDestroy(){destroyed=true;handler.removeCallbacksAndMessages(null);prefs.unregisterOnSharedPreferenceChangeListener(changes);if(registered)unregisterReceiver(events);stopPlayer();super.onDestroy();}
    }
}
