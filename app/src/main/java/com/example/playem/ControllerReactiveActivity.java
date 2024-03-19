package com.example.playem;

import android.animation.ObjectAnimator;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowMetrics;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.playem.ControllerEmulatorSurfaceView.ControlGrid;
import com.example.playem.ControllerEmulatorSurfaceView.ControllerReactiveView;
import com.example.playem.ControllerEmulatorSurfaceView.VirtualControls.VirtualControlTemplates;
import com.example.playem.ControllerEmulatorSurfaceView.interfaces.BuildableViewCallbacks;
import com.example.playem.ViewCallbacks.GattServiceCallbacks;
import com.example.playem.hid.HIDProfileBuilder;
import com.example.playem.viewmodels.GattServiceState;


public class ControllerReactiveActivity extends AppCompatActivity implements BuildableViewCallbacks {
    private WindowInsetsControllerCompat windowInsetsControllerCompat;
    private ControllerReactiveView controlView;
    private Button AddTS,AddB,AddTB,AddGyro;
    private Button ControlOptions,RemoveControl,DownSize,UpSize;
    private Button AcceptEdit,HIDBuild,AdvertButton;
    private LinearLayout AddCom,ControlOptionLayout,MenuLayout;
    private LinearLayout EditOptionLayout;
    private float screenWidth;
    private float screenHeight;
    private float refreshRate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        windowInsetsControllerCompat =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        //DisplayMetrics displayMetrics = new DisplayMetrics();
        refreshRate = this.getDisplay().getRefreshRate();
        Rect r = getWindowManager().getCurrentWindowMetrics().getBounds();
        screenWidth = r.width();
        screenHeight = r.height();
        //int height = displayMetrics.heightPixels;
        //int width = displayMetrics.widthPixels;
        // Configure the behavior of the hidden system bars.
        windowInsetsControllerCompat.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);

        getWindow().getDecorView().setOnApplyWindowInsetsListener((view, windowInsets) -> {
            this.windowInsetsControllerCompat.hide(WindowInsetsCompat.Type.systemBars());
            return view.onApplyWindowInsets(windowInsets);
        });

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        controlView = new ControllerReactiveView(this,new ControlGrid(this,0.25f,ControlGrid.ORIENTATION_POTRAIT),r);
        setContentView(R.layout.activity_thumbstick);
        FrameLayout fl = (FrameLayout) findViewById(R.id.cGridLayout);
        fl.addView(controlView);

        AddTS = findViewById(R.id.tButtonThumbstick);
        AddB = findViewById(R.id.tButtonButton);
        AddTB = findViewById(R.id.tButtonToggle);
        ControlOptions = findViewById(R.id.bOptions);
        UpSize = findViewById(R.id.bResizeUp);
        ControlOptionLayout = findViewById(R.id.cControlSettings);
        EditOptionLayout = findViewById(R.id.lControlEdits);
        AddCom = findViewById(R.id.cSidePanel);
        MenuLayout = findViewById(R.id.cMainBuildSidePanel);
        RemoveControl = findViewById(R.id.bRemove);
        AcceptEdit = findViewById(R.id.bDoneEdits);
        DownSize = findViewById(R.id.bResizeDown);
        HIDBuild = findViewById(R.id.buildButton);
        AdvertButton = findViewById(R.id.AdvertiseButton);
        setClickiesSide();
        super.onCreate(savedInstanceState);
    }

    private void setClickiesSide() {
        AddTS.setOnClickListener((View v) -> {
            if (controlView != null) {
                controlView.AddComponent(VirtualControlTemplates.THUMSTICK);
                HideComponentAdd();
            }
        });
        ControlOptions.setOnClickListener((View v) -> {
            HideControlOptions();
            ShowControlEditOptions();
        });
        UpSize.setOnClickListener((View v) -> {

        });
        DownSize.setOnClickListener((View v) -> {

        });
        RemoveControl.setOnClickListener((View v) -> {
            if (controlView != null) {
                controlView.RemoveComponent();
                HideControlOptions();
            }
        });
        AcceptEdit.setOnClickListener((View v)->{
            controlView.FinishEdits();
            HideControlOptions();
            HideControlEditOptions();
        });
        HIDBuild.setOnClickListener((View v)->{
            HideAllBuildOptions();
            controlView.FinalizeAndBuildAll(gattService);
        });
        AdvertButton.setOnClickListener((View v)->{
            gattService.StartAdvertisement();
            controlView.SwitchToPlay();
            HideAllBuildOptions();
        });

    }
    @Override
    protected void onStart() {
        super.onStart();
        bindService();
        HideAllBuildOptions();
    }

    private void HideAllBuildOptions(){
        HideComponentAdd();
        HideControlOptions();
        HideControlEditOptions();
        HideMenuOptions();
    }
    protected void onPause(){
        super.onPause();
        Log.i("APP","onPause entered");

        if(isBound){
            Log.i("APP","Controller onPause entered");
            gattService.UnsubscribeFromEventBus(this);
            unbindService(gattServiceConnection);
            isBound = false;
        }
    }
    @Override
    protected void onResume(){
        super.onResume();
        bindService();
//        gattService.SubscribeToEventBus(this,gattServiceCallbacks);
        Log.i("APP","Controller onResume entered");
    }
    @Override
    protected void onStop() {
        super.onStop();
        if(isBound){
            gattService.UnsubscribeFromEventBus(this);
            Log.w("BINDER","Unsubscribed from gattService trying to unbind");
            try{
                unbindService(gattServiceConnection);
            }catch(Exception e){
                Log.e("BIND",e.toString());
            }
        }
    }

    @Override
    protected void onDestroy() {
        if(isBound)
            unbindService(gattServiceConnection);
        super.onDestroy();
    }
    private void bindService(){
        Intent intent = new Intent(this, PlayEmGATTService.class);
        startService(intent);
        if(!bindService(intent, gattServiceConnection,BIND_AUTO_CREATE)) {
            Log.e("BINDER","Error in binding");
        }
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
    private void UpdateViewFromGattState(GattServiceState newState){
        Log.i("STATE",String.format("New State %s",newState.status.toString()));
        if(newState.status.ordinal() == GattServiceState.SERVICE_STATUS.CONNECTED_IDLE.ordinal()){
            gattService.StartInput(newState.address);
        }
    }

    ServiceConnection gattServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.w("BINDER","Binder connected");
            PlayEmGATTService.mBinder binderLink = (PlayEmGATTService.mBinder) service;
            gattService = binderLink.getService();
            isBound = true;
            Log.i("DEBUG","Controller onServiceConnected " + Thread.currentThread().getName());
            gattService.DeferredConstructor(ControllerReactiveActivity.this);
            gattService.SubscribeToEventBus(ControllerReactiveActivity.this,gattServiceCallbacks);
            controlView.SetPipe(gattService.GetPipe());
            UpdateViewFromGattState();
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.w("BINDER","Service Disconnect was called");
            isBound = false;
            gattService= null;
        }
    };

    @Override
    public void HideComponentAdd() {
        this.runOnUiThread(()->{
            //View sPanel = findViewById(R.id.cSidePanel);
            ObjectAnimator animation = ObjectAnimator.ofFloat(AddCom, "translationX", -400f);
            animation.setDuration(1000);
            animation.start();
        });
    }

    @Override
    public void ShowComponentAdd() {
        //Log.i("CALLBACK","ShowComponentAdd Called");
        this.runOnUiThread(()->{
            ObjectAnimator animation = ObjectAnimator.ofFloat(AddCom, "translationX", 0f);
            animation.setDuration(1000);
            animation.start();
        });
    }
    @Override
    public void ShowToastError(String message,int duration){
        this.runOnUiThread(()->{
            Toast.makeText(this, message, duration).show();
        });
    }

    @Override
    public void ShowControlOptions() {
        this.runOnUiThread(()->{
            ObjectAnimator animation = ObjectAnimator.ofFloat(ControlOptionLayout, "translationY", 0f);
            animation.setDuration(1000);
            animation.start();
        });
    }

    @Override
    public void HideControlOptions() {
        this.runOnUiThread(()->{
            ObjectAnimator animation = ObjectAnimator.ofFloat(ControlOptionLayout, "translationY", -200f);
            animation.setDuration(1000);
            animation.start();
        });
    }

    @Override
    public void ShowMenuOptions() {
        this.runOnUiThread(()->{
            ObjectAnimator animation = ObjectAnimator.ofFloat(MenuLayout, "translationX", 0f);
            animation.setDuration(1000);
            animation.start();
        });
    }

    @Override
    public void HideMenuOptions() {
        this.runOnUiThread(()->{
            ObjectAnimator animation = ObjectAnimator.ofFloat(MenuLayout, "translationX", 200f);
            animation.setDuration(1000);
            animation.start();
        });
    }

    @Override
    public void ShowControlEditOptions() {
        this.runOnUiThread(()->{
            Log.i("SCREEN",String.format("%f",screenWidth));
            ObjectAnimator animation = ObjectAnimator.ofFloat(EditOptionLayout, "translationX", 0f);
            animation.setDuration(1000);
            animation.start();
        });
    }

    @Override
    public void HideControlEditOptions() {
        this.runOnUiThread(()->{
            ObjectAnimator animation = ObjectAnimator.ofFloat(EditOptionLayout, "translationX", 200f);
            animation.setDuration(1000);
            animation.start();
        });
    }
}
