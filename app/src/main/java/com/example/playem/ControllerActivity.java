package com.example.playem;

import android.animation.ObjectAnimator;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.playem.appcontroller.ControlGrid;
import com.example.playem.appcontroller.ControllerReactiveView;
import com.example.playem.appcontroller.VirtualControls.VirtualControlTemplates;
import com.example.playem.appcontroller.interfaces.BuildableViewCallbacks;
import com.example.playem.ViewCallbacks.GattServiceCallbacks;
import com.example.playem.viewmodels.GattServiceState;

import java.util.Timer;
import java.util.TimerTask;


public class ControllerActivity extends AppCompatActivity implements BuildableViewCallbacks {
    private WindowInsetsControllerCompat windowInsetsControllerCompat;
    private ControllerReactiveView controlView;
    private Button AddTS,AddB,AddTB,AddGyro;
    private Button ControlOptions,RemoveControl,DownSize,UpSize;
    private Button AcceptEdit,HIDBuild,AdvertButton;
    private Button LoadP,SaveP,DCButton,XButton;
    private LinearLayout AddCom,ControlOptionLayout,MenuLayout;
    private LinearLayout EditOptionLayout;
    private float screenWidth;
    private float screenHeight;
    private float refreshRate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);

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
        getWindow()
                .addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow()
            .getDecorView()
            .setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS);
        getWindow()
            .getDecorView()
            .setOnApplyWindowInsetsListener((view, windowInsets) -> {
                this.windowInsetsControllerCompat.hide(WindowInsetsCompat.Type.systemBars()|WindowInsetsCompat.Type.statusBars()|WindowInsetsCompat.Type.captionBar());
                return view.onApplyWindowInsets(windowInsets);
            });

        setContentView(R.layout.activity_control);

        controlView = new ControllerReactiveView(this,new ControlGrid(this,0.25f,ControlGrid.ORIENTATION_POTRAIT),r,this);
        FrameLayout fl = findViewById(R.id.cGridLayout);
        fl.addView(controlView);
        findViewById(R.id.cOverlay).setZ(1f);
        Log.e("SURFVIEW",String.format("fl %f other: %f",fl.getElevation(),findViewById(R.id.cOverlay).getElevation()));

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
        SaveP = findViewById(R.id.AdvertiseButton);
        LoadP = findViewById(R.id.loadButton);
        DCButton = findViewById(R.id.bRemoveBond);
        XButton = findViewById(R.id.exitButton);
        setClickiesSide();
    }

    private void setClickiesSide() {
        AddTS.setOnClickListener((View v) -> {
            if (controlView != null) {
                controlView.AddComponent(VirtualControlTemplates.THUMSTICK);
                HideComponentAdd();
            }
        });
        AddB.setOnClickListener((View v)->{
            if (controlView != null) {
                controlView.AddComponent(VirtualControlTemplates.SBUTTON);
                HideComponentAdd();
            }
        });
        ControlOptions.setOnClickListener((View v) -> {
            HideControlOptions();
            ShowControlEditOptions();
        });
        UpSize.setOnClickListener((View v) -> {
            controlView.ResizeComponent(1);
        });
        DownSize.setOnClickListener((View v) -> {
            controlView.ResizeComponent(-1);
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
            controlView.FinalizeAndBuildAll(gattService);
            controlView.SwitchToPlay();
        });
/*        AdvertButton.setOnClickListener((View v)->{
            gattService.StartAdvertisement();
            controlView.SwitchToPlay();
            HideAllOptions();
        });*/
        SaveP.setOnClickListener((View v)->{
            controlView.SaveProfile("autosave_cache");
        });
        LoadP.setOnClickListener((View v)->{
            runOnUiThread(()-> {
                controlView.LoadProfile("autosave_cache");
            });
        });
        DCButton.setOnClickListener((View v)->{
            disconnecting = true;
            gattService.Disconnect(true);
            this.finish();

        });
        XButton.setOnClickListener((View v)->{
            HideAllOptions();
        });

    }
    private boolean disconnecting=false;
    @Override
    protected void onStart() {
        super.onStart();
        bindService();
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
            gattService.Disconnect(true);
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
        if(isBound){
            //gattService.UnsubscribeFromEventBus(ControllerActivity.this);
            unbindService(gattServiceConnection);
        }
        super.onDestroy();
    }
    private void bindService(){
        Intent intent = new Intent(this, AppGattService.class);
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
    private AppGattService gattService;
    private void UpdateViewFromGattState(GattServiceState newState){
        Log.i("STATE",String.format("New State %s",newState.status.toString()));
        if(newState.status.ordinal()==GattServiceState.SERVICE_STATUS.BUILT_READY_BROADCAST.ordinal()&& !disconnecting)
            gattService.StartAdvertisement();
        if(newState.status.ordinal() == GattServiceState.SERVICE_STATUS.CONNECTED_IDLE.ordinal()&&!disconnecting)
            gattService.StartInput(newState.address);
        if(newState.status.ordinal()==GattServiceState.SERVICE_STATUS.NOTIFY.ordinal()){
            Timer t = new Timer();
            t.schedule(new TimerTask() {
                @Override
                public void run() {
                    HideAllOptions();
                }
            },5000);
        }
    }

    ServiceConnection gattServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.w("BINDER","Binder connected");
            AppGattService.mBinder binderLink = (AppGattService.mBinder) service;
            gattService = binderLink.getService();
            isBound = true;
            Log.i("DEBUG","Controller onServiceConnected " + Thread.currentThread().getName());
            gattService.DeferredConstructor(ControllerActivity.this);
            gattService.SubscribeToEventBus(ControllerActivity.this,gattServiceCallbacks);
            //controlView.SetPipe(gattService.GetPipe());
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
            ObjectAnimator animation = ObjectAnimator.ofFloat(AddCom, "translationX", -600f);
            animation.setDuration(500);
            animation.start();
            //Log.w("BINDER","Hide");

        });
    }
    @Override
    public void HideAllOptions(){
        Log.w("CALLBACK","Hide All Called");
        HideMenuOptions();
        HideComponentAdd();
        HideControlOptions();
        HideControlEditOptions();
    }
    @Override
    public void ShowAllOptions(){
        Log.w("CALLBACK","Show All Called");
        ShowMenuOptions();
        ShowComponentAdd();
        ShowControlOptions();
        ShowControlEditOptions();
    }

    @Override
    public void ShowComponentAdd() {
        //Log.i("CALLBACK","ShowComponentAdd Called");
        this.runOnUiThread(()->{
            ObjectAnimator animation = ObjectAnimator.ofFloat(AddCom, "translationX", 0f);
            animation.setDuration(500);
            animation.start();
            //Log.w("BINDER","Show");
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
            animation.setDuration(500);
            animation.start();
            //Log.w("BINDER","Show");
        });
    }

    @Override
    public void HideControlOptions() {
        this.runOnUiThread(()->{
            ObjectAnimator animation = ObjectAnimator.ofFloat(ControlOptionLayout, "translationY", -600f);
            animation.setDuration(500);
            animation.start();
            //Log.w("BINDER","Hide");
        });
    }

    @Override
    public void ShowMenuOptions() {
        this.runOnUiThread(()->{
            ObjectAnimator animation = ObjectAnimator.ofFloat(MenuLayout, "translationX", 0f);
            animation.setDuration(500);
            animation.start();
            Log.w("BINDER","Show");
        });
    }

    @Override
    public void HideMenuOptions() {
        this.runOnUiThread(()->{
            ObjectAnimator animation = ObjectAnimator.ofFloat(MenuLayout, "translationX", 600f);
            animation.setDuration(500);
            animation.start();
        });
    }

    @Override
    public void ShowControlEditOptions() {
        this.runOnUiThread(()->{
            Log.i("SCREEN",String.format("%f",screenWidth));
            ObjectAnimator animation = ObjectAnimator.ofFloat(EditOptionLayout, "translationX", 0f);
            animation.setDuration(500);
            animation.start();
            //Log.w("BINDER","Show");
        });
    }

    @Override
    public void HideControlEditOptions() {
        this.runOnUiThread(()->{
            ObjectAnimator animation = ObjectAnimator.ofFloat(EditOptionLayout, "translationX", 600f);
            animation.setDuration(500);
            animation.start();
            //Log.w("BINDER","Hide");
        });
    }
}
