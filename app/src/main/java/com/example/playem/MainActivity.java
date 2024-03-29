package com.example.playem;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.playem.ViewCallbacks.GattServiceCallbacks;
import com.example.playem.bluetoothLE.utils.PermissionHandler;
import com.example.playem.hid.HIDProfileBuilder;
import com.example.playem.viewmodels.GattServiceState;

import java.util.Timer;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MainActivity extends AppCompatActivity{
    AppGattService gattService;
    boolean isBound = false;
    ViewSwitcher viewSwitcher;
    ImageButton backButton;
    Button connectionsButton, savesButton, settingsButton, buildControllerButton;
    Button bleAdvertButton,testButton,disconnectButton;
    Button buildButton, controlViewButton;
    TextView hostName,bondState,bondList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewSwitcher = findViewById(R.id.viewSwitcher);
        settingsButton = findViewById(R.id.btnSettings);
        savesButton = findViewById(R.id.btnSaves);
        connectionsButton = findViewById(R.id.btnConnections);
        buildControllerButton = findViewById(R.id.btnBuild);
        backButton = findViewById(R.id.btnBack);


        //TODO: Move this to after surface view is started
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        bleAdvertButton = findViewById(R.id.bAdvertise);
        testButton = findViewById(R.id.bTest);
        disconnectButton = findViewById(R.id.bDisconnect);
        buildButton = findViewById(R.id.bBuild);
        controlViewButton = findViewById(R.id.bControls);


        hostName = findViewById(R.id.tHostName);
        bondState = findViewById(R.id.tBond);
        bondList = findViewById(R.id.tBondedList);
        setUpClickies();
        PermissionHandler.CheckPermissions(this);

    }

    protected void setUpClickies(){
        settingsButton.setOnClickListener(v -> {});
        savesButton.setOnClickListener(v -> {});
        buildControllerButton.setOnClickListener(v -> {
            Intent goControlsView = new Intent(getApplicationContext(), ControllerActivity.class);
            startActivity(goControlsView);
        });
        connectionsButton.setOnClickListener(v -> viewSwitcher.showNext());
        backButton.setOnClickListener(v -> viewSwitcher.showPrevious());

        bleAdvertButton.setOnClickListener(v -> gattService.StartAdvertisement());
        testButton.setOnClickListener(v -> gattService.StartInput(bondState.getText().toString()));
        disconnectButton.setOnClickListener(v -> {
           /* if(satTest!=null){
                satTest.purge();
                satTest = null;
                stest = null;
            }*/
            gattService.Disconnect();
        });
        buildButton.setOnClickListener(v->gattService.BuildPipe(new HIDProfileBuilder()));
        controlViewButton.setOnClickListener(v->{
            Intent goControlsView = new Intent(getApplicationContext(), ControllerActivity.class);
            startActivity(goControlsView);
        });
    }
    protected void UpdateViewFromGattState(){
        if(isBound){
            UpdateViewFromGattState(gattService.getLastValidState());
        }
    }
    protected void UpdateViewFromGattState(GattServiceState state){
        Log.i("UI",String.format("Callback from GattState %s",state.status.toString()));
        int statusLvl = state.status.ordinal();
        boolean dc,test,advert,hidbuild;
        String sconnHost,sbondState,sbondables;
        dc = statusLvl >= GattServiceState.SERVICE_STATUS.CONNECTED_IDLE.ordinal();
        test = statusLvl == GattServiceState.SERVICE_STATUS.CONNECTED_IDLE.ordinal();
        advert = statusLvl == GattServiceState.SERVICE_STATUS.BUILT_READY_BROADCAST.ordinal();
        hidbuild = statusLvl == GattServiceState.SERVICE_STATUS.IDLE_NODATA.ordinal();
        bleAdvertButton.setEnabled(advert);
        testButton.setEnabled(test);
        disconnectButton.setEnabled(dc);
        buildButton.setEnabled(hidbuild);

        Log.w("UI",String.format("Message Received with: %s %s %s",state.name,state.address,state.bondstate));

        sconnHost = statusLvl>= GattServiceState.SERVICE_STATUS.CONNECTED_IDLE.ordinal()?state.name:"DISCONNECTED";
        sbondState = statusLvl>= GattServiceState.SERVICE_STATUS.CONNECTED_IDLE.ordinal()?state.address:state.status.toString();
        sbondables = statusLvl> GattServiceState.SERVICE_STATUS.IDLE_NODATA.ordinal()?bondList.getText().toString():"BT Manager Not Init";

        hostName.setText(sconnHost);
        bondState.setText(sbondState);
        bondList.setText(sbondables);

        /*if(statusLvl==GattServiceState.SERVICE_STATUS.NOTIFY.ordinal() && satTest ==null){
            satTest = new Timer();
            stest = new saturationTest();
            satTest.scheduleAtFixedRate(stest.runTest(gattService.GetPipe(),1, 0.75,15,1000),1000,15);
        }*/
    }
    private Timer satTest;
    //private saturationTest stest;
    @Override
    protected void onStart() {
        super.onStart();

    }
    @Override
    protected void onPause() {
        super.onPause();
        Log.i("APP","onPause entered");

        if(isBound){
            Log.i("APP","onPause entered");
            gattService.UnsubscribeFromEventBus(this);
            unbindService(gattServiceConnection);
            isBound = false;
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        if(PermissionHandler.Valid)
            bindService();
        //gattService.SubscribeToEventBus(this,gattServiceCallbacks);
        Log.i("APP","onResume entered");
    }
    @Override
    protected void onDestroy(){
        stopService(new Intent(this,AppGattService.class));
        super.onDestroy();
        Log.i("APP","onDestroy entered");
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

    private void bindService(){
        if(PermissionHandler.Valid){
        Intent intent = new Intent(this, AppGattService.class);
        startService(intent);
        if(!bindService(intent, gattServiceConnection,BIND_AUTO_CREATE)) {
            Log.e("BINDER","Error in binding");
            }
        }else{
            Log.e("PERM","Permissions not satisfied");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,@NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionHandler.checkOnPermissionsResult(requestCode,permissions,grantResults);
        if(PermissionHandler.Valid)
            bindService();
    }
    private final GattServiceCallbacks gattServiceCallbacks = new GattServiceCallbacks() { //TODO make a factory class
        @SuppressLint("MissingPermission")
        @Override
        public Runnable onBondedDevicesChange(ConcurrentLinkedQueue<BluetoothDevice> bondedDevices) {
            return () -> {
                Object[] bondedList = bondedDevices.toArray();
                TextView tv = findViewById(R.id.tBondedList);
                String s ="";
                for(Object obt : bondedList){
                    BluetoothDevice d = (BluetoothDevice) obt;
                    s+=String.format("%s %s\n",d.getName(),d.getAddress());
                }
                tv.setText(s);
            };
        }

        @Override
        public Runnable onServiceStatusChanged (GattServiceState newState) {
            return ()->UpdateViewFromGattState(newState);
        }
    };

    ServiceConnection gattServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.w("BINDER","Binder connected");
            AppGattService.mBinder binderLink = (AppGattService.mBinder) service;
            gattService = binderLink.getService();
            isBound = true;
            Log.i("DEBUG","MainActivity onServiceConnected " + Thread.currentThread().getName());
            gattService.DeferredConstructor(MainActivity.this);
            gattService.SubscribeToEventBus(MainActivity.this,gattServiceCallbacks);
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

