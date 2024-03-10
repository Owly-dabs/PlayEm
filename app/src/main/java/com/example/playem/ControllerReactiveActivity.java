package com.example.playem;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.DisplayCutout;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.DisplayCutoutCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.playem.ControllerEmulatorSurfaceView.ControlGrid;
import com.example.playem.ControllerEmulatorSurfaceView.ControllerEmulator;
import com.example.playem.ControllerEmulatorSurfaceView.ControllerReactiveView;
import com.example.playem.MainActivity;
import com.example.playem.PlayEmGATTService;
import com.example.playem.R;
import com.example.playem.ViewCallbacks.GattServiceCallbackFactory;
import com.example.playem.ViewCallbacks.GattServiceCallbacks;
import com.example.playem.viewmodels.GattServiceState;

import java.util.concurrent.ConcurrentLinkedQueue;

public class ControllerReactiveActivity extends AppCompatActivity {

    private WindowInsetsControllerCompat windowInsetsControllerCompat;
    private ControllerReactiveView controlView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        EdgeToEdge.enable(this);
        windowInsetsControllerCompat =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());

        // Configure the behavior of the hidden system bars.
        windowInsetsControllerCompat.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);

        getWindow().getDecorView().setOnApplyWindowInsetsListener((view, windowInsets) -> {
            this.windowInsetsControllerCompat.hide(WindowInsetsCompat.Type.systemBars());
            return view.onApplyWindowInsets(windowInsets);
        });

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        controlView = new ControllerReactiveView(this,new ControlGrid(this,0.25f,ControlGrid.ORIENTATION_POTRAIT));
        setContentView(controlView);
        super.onCreate(savedInstanceState);
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
        unbindService(gattServiceConnection);
        super.onDestroy();
    }



    private final GattServiceCallbacks gattServiceCallbacks = new GattServiceCallbacks() { //TODO make a factory class
        @Override
        public Runnable onServiceStatusChanged (GattServiceState newState) {
            return ()->UpdateViewFromGattState(newState);
        }
    };
    protected void UpdateViewFromGattState(){
        if(isBound){
            UpdateViewFromGattState(gattService.getLastValidState());
        }
    }
    private boolean isBound=false;
    private PlayEmGATTService gattService;
    private void UpdateViewFromGattState(GattServiceState newState){}

    ServiceConnection gattServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.w("BINDER","Binder connected");
            PlayEmGATTService.mBinder binderLink = (PlayEmGATTService.mBinder) service;
            gattService = binderLink.getService();
            isBound = true;
            Log.i("DEBUG","MainActivity onServiceConnected " + Thread.currentThread().getName());
            gattService.DeferredConstructor(ControllerReactiveActivity.this);
            gattService.SubscribeToEventBus(ControllerReactiveActivity.this,gattServiceCallbacks);
            UpdateViewFromGattState();
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.w("BINDER","Service Disconnect was called");
            isBound = false;
            gattService= null;
        }
    };
}
