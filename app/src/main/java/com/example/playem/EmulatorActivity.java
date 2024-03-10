package com.example.playem;

import android.os.Bundle;
import android.util.Log;
import android.view.DisplayCutout;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.DisplayCutoutCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.playem.ControllerEmulatorSurfaceView.ControllerEmulator;

public class EmulatorActivity extends AppCompatActivity {
    private ControllerEmulator controllerEmulator;
    private WindowInsetsControllerCompat windowInsetsControllerCompat;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);

        controllerEmulator = new ControllerEmulator(this);
        //setContentView(R.layout.activity_main);
        windowInsetsControllerCompat =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        // Configure the behavior of the hidden system bars.
        windowInsetsControllerCompat.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
        getWindow().getDecorView().setOnApplyWindowInsetsListener((view, windowInsets) -> {
            Log.i("UI","Hidding it");
            Log.e("INSET",String.valueOf(windowInsets.getDisplayCutout().getSafeInsetTop()));
            Log.e("INSET",String.valueOf(windowInsets.getDisplayCutout().getSafeInsetBottom()));
            Log.e("INSET",String.valueOf(windowInsets.getDisplayCutout().getSafeInsetRight()));
            this.windowInsetsControllerCompat.hide(WindowInsetsCompat.Type.systemBars());
            return view.onApplyWindowInsets(windowInsets);
        });


        setContentView(controllerEmulator);
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_thumbstick);
    }
    @Override
    protected void onStart() {
        super.onStart();
    }
    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
