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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.playem.ViewCallbacks.GattServiceCallbacks;
import com.example.playem.viewmodels.GattServiceState;

import java.util.concurrent.ConcurrentLinkedQueue;

public class MainActivity extends AppCompatActivity{
    /*private ExecutorService BLEManagerExecutorPool;
    PlayEmBTManager mBTManager;
    private double a = 0;
    private boolean aswing = true;
    private int b = 0;
    private boolean testB = true;
    public PlayEmDataPipe dataPipe;
    private Timer genericTimer = new Timer("Report Test");
    private boolean gTimerActive = false;*/
    PlayEmGATTService gattService;
    boolean isBound = false;
    Button bleAdvertButton,testButton,disconnectButton,buildButton;
    TextView hostName,bondState,bondList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //TODO: Move this to after surface view is started
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        bleAdvertButton = findViewById(R.id.bAdvertise);
        testButton = findViewById(R.id.bTest);
        disconnectButton = findViewById(R.id.bDisconnect);
        buildButton = findViewById(R.id.bBuild);

        hostName = findViewById(R.id.tHostName);
        bondState = findViewById(R.id.tBond);
        bondList = findViewById(R.id.tBondedList);

        setUpClickies();

    }

    protected void setUpClickies(){
        bleAdvertButton.setOnClickListener(v -> gattService.StartAdvertisement());
        testButton.setOnClickListener(v -> gattService.StartInput(bondState.getText().toString()));
        disconnectButton.setOnClickListener(v -> gattService.Disconnect());
        buildButton.setOnClickListener(v->gattService.BuildPipe());
    }
    protected void UpdateViewFromGattState(){
        if(isBound){
            UpdateViewFromGattState(gattService.getLastValidState());
        }
    }
    protected void UpdateViewFromGattState(GattServiceState state){
        Log.i("UI","Callback from GattState");
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

        sconnHost = statusLvl>= GattServiceState.SERVICE_STATUS.CONNECTED_IDLE.ordinal()?state.name:"DISCONNECTED";
        sbondState = statusLvl>= GattServiceState.SERVICE_STATUS.CONNECTED_IDLE.ordinal()?state.address:state.status.toString();
        sbondables = statusLvl> GattServiceState.SERVICE_STATUS.IDLE_NODATA.ordinal()?bondList.getText().toString():"BT Manager Not Init";

        hostName.setText(sconnHost);
        bondState.setText(sbondState);
        bondList.setText(sbondables);

    }

    @Override
    protected void onStart() {
        super.onStart();
        bindService();
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
        bindService();
        //gattService.SubscribeToEventBus(this,gattServiceCallbacks);
        Log.i("APP","onResume entered");
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(isBound)
            gattService.UnsubscribeFromEventBus(this);
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
        Intent intent = new Intent(this, PlayEmGATTService.class);
        startService(intent);
        if(!bindService(intent, gattServiceConnection,BIND_AUTO_CREATE)) {
            Log.e("BINDER","Error in binding");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                              @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        gattService.onRequestPermissionsResult(requestCode,permissions,grantResults);
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
            PlayEmGATTService.mBinder binderLink = (PlayEmGATTService.mBinder) service;
            gattService = binderLink.getService();
            isBound = true;
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

