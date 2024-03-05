package com.example.playem;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ServiceInfo;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.playem.ViewCallbacks.GattServiceCallbacks;
import com.example.playem.btmanager.PlayEmBTManager;
import com.example.playem.hid.HIDProfileBuilder;
import com.example.playem.hid.interfaces.HIDChunk;
import com.example.playem.pipes.PlayEmDataPipe;

import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlayEmGATTService extends Service {
    public PlayEmGATTService(){
    }
//Blocking call to thread of caller
    public void DeferredConstructor(MainActivity appActivity, GattServiceCallbacks callbacks){
        executorPool = Executors.newSingleThreadExecutor();
        this.appActivity = appActivity;
        dataChunks = new HashMap<>();
        if(btManager == null){
            btManager = new PlayEmBTManager(this.appActivity,executorPool,mediator);
        }
        activityGattServiceCallback = callbacks;
        _constructed = true;
        mediator.onServiceReady(SERVICE_STATES.ACTION_SUCCESS); //Decouple build sequence and give responsibility to activity
    }
    private GattServiceCallbacks activityGattServiceCallback;
    private boolean _constructed = false;
    private boolean _activityBind = false;
    private ExecutorService executorPool;
    private final IBinder _binder = new mBinder();
    private PlayEmBTManager btManager;

    public PlayEmDataPipe dataPipe;

    private MainActivity appActivity;

    private HashMap<Integer, HIDChunk> dataChunks;
    private BroadcastReceiver bondstateReceiver;
    private int notificationID = 1;
    protected void BuildPipe(){
        if(_constructed){
            executorPool.execute(()->{
                HIDProfileBuilder builder = new HIDProfileBuilder();
                builder.Build();
                dataPipe = new PlayEmDataPipe(builder.GetChunks());
                dataChunks = builder.GetChunks();
                btManager.GattServerInit(builder.GetReportMap(),new byte[]{},dataPipe);
                registerReceiver(bondstateReceiver = btManager.bondStateReceiver(),new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
            });
        }
    }

    protected void StartAdvertisement(){
        if(_constructed){
            executorPool.execute(()->{
                btManager.AdvertiseHID();
            });
        }
    }

    protected void StartInput(String address){
        final int MAX_MAC_LEN = 17;
        if(address.length()> MAX_MAC_LEN){
            Log.e("INPUT","Invalid address given");
            return;
        }
        if(_constructed){
            executorPool.execute(()->{
                btManager.attachNotifier(address);
            });
        }
    }

    protected void Disconnect(){
        executorPool.execute(()->{
            if(_constructed){
                btManager.Disconnect();
            }
        });
    }

    private final GattServiceCallbacks mediator = new GattServiceCallbacks() {
        @Override
        public Runnable onBondedDevicesChange(ConcurrentLinkedQueue<BluetoothDevice> bondedDevices) {
            if(_activityBind)
                appActivity.runOnUiThread(activityGattServiceCallback.onBondedDevicesChange(bondedDevices));
            return null;
        }
        @Override
        public Runnable onConnectionStateChanged(String d_address, String d_name, SERVICE_STATES state) {
            if(_activityBind)
                appActivity.runOnUiThread(activityGattServiceCallback.onConnectionStateChanged(d_address,d_name,state));
            return null;
        }
        @Override
        public Runnable onAdvertisementStateChanged(SERVICE_STATES state) {
            if(_activityBind)
                appActivity.runOnUiThread(activityGattServiceCallback.onAdvertisementStateChanged(state));
            return null;
        }

        @Override
        public Runnable onNotifierChanged(SERVICE_STATES state) {
            if(_activityBind)
                appActivity.runOnUiThread(activityGattServiceCallback.onNotifierChanged(state));
            return null;
        }

        @Override
        public Runnable onServicesAddComplete(SERVICE_STATES state) {
            if(_activityBind)
                appActivity.runOnUiThread(activityGattServiceCallback.onServicesAddComplete(state));
            return null;
        }

        @Override
        public Runnable onServiceReady(SERVICE_STATES state) {
            Runnable activityAction = activityGattServiceCallback.onServiceReady(state);//Todo ensure all calls are nullable
            if(_activityBind && activityAction!=null)
                appActivity.runOnUiThread(activityAction);
            return null;
        }

        @Override
        public Runnable onGattStatusChanged(SERVICE_STATES state) {
            if(_activityBind)
                appActivity.runOnUiThread(activityGattServiceCallback.onGattStatusChanged(state));
            return null;
        }
    };


    @Override
    public void onCreate(){
        super.onCreate();
        String CHANNEL_ID = "playEM_channelID";
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                "PlayEM Channel",
                NotificationManager.IMPORTANCE_DEFAULT);

        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setOngoing(true)
                .setContentTitle("PlayEm BLE")
                .setContentText("Bluetooth is Running in the Background").build();
        //TODO Add buttons to disable or bring APP to foreground
        this.startForeground(notificationID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE);
    }

    @Override
    public int onStartCommand(Intent intent,int flags,int startID){
        return super.onStartCommand(intent,flags,startID);
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        _activityBind = true;
        return _binder;
    }

    @Override
    public boolean onUnbind(Intent intent){
        Log.w("SERV","Application Unbound...");
        _activityBind = false;
        this.appActivity = null;
        return super.onUnbind(intent);
    }
    @Override
    public void onRebind(Intent intent){
        Toast.makeText(appActivity,"Rebind To Service",Toast.LENGTH_LONG).show();
        super.onRebind(intent);
        _activityBind = true;
    }

    @Override
    public void onDestroy(){
        _activityBind = false;
        if(bondstateReceiver!=null)
            unregisterReceiver(bondstateReceiver);
        if(btManager!=null)
            btManager.Close();
        if(executorPool!=null)
            executorPool.shutdown();
        stopForeground(STOP_FOREGROUND_REMOVE);
        super.onDestroy();
    }

    public class mBinder extends Binder{
        PlayEmGATTService getService(){
            return PlayEmGATTService.this;
        }
    }
    public enum SERVICE_STATES{
        ADVERT_DISABLED,
        ADVERT_ENABLED,
        BLUETOOTH_DEVICE_DISCONNECT,
        BLUETOOTH_DEVICE_CONNECTED,
        CHUNK_READY,
        ACTION_SUCCESS,
        ACTION_FAIL,
        NOTIFY_DETACHED,
        NOTIFY_ATTACHED,
        GATT_READY,
        GATT_ERROR,
        GATT_RUNNING

    }
}
