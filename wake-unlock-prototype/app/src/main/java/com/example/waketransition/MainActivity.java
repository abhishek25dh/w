package com.example.waketransition;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL); root.setPadding(48,80,48,32);
        TextView title = new TextView(this); title.setText("Wake & Unlock Test"); title.setTextSize(26); root.addView(title);
        TextView instructions = new TextView(this);
        instructions.setText("A technical prototype for testing two effects.\n\n1. Apply the wallpaper to both screens if your phone offers that option.\n2. Turn the screen off, then wake it: golden rays should reveal the orb.\n3. Unlock: the wallpaper should shift toward blue.\n4. Repeat with fingerprint, face and swipe.\n\nThe animation may start late or fail on some phones. This is a diagnostic prototype, not an ad-quality character animation.");
        instructions.setTextSize(16); instructions.setPadding(0,32,0,32); root.addView(instructions);
        Button button = new Button(this); button.setText("Preview and apply wallpaper");
        button.setOnClickListener(v -> {
            Intent intent = new Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER);
            intent.putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,new ComponentName(this,TestWallpaper.class));
            try { startActivity(intent); } catch (android.content.ActivityNotFoundException e) { startActivity(new Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER)); }
        });
        root.addView(button,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
        setContentView(root);
    }
}
