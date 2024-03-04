package com.example.playem;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

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

    public void DeferredConstructor(MainActivity appActivity, GattServiceCallbacks callbacks){
        executorPool = Executors.newSingleThreadExecutor();
        this.appActivity = appActivity;
        dataChunks = new HashMap<>();
        if(btManager == null){
            btManager = new PlayEmBTManager(this.appActivity,executorPool,mediator);
        }
        activityGattServiceCallback = callbacks;
        _constructed = true;
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

    public void BuildAndPipeAll(){
        if(_constructed){
            executorPool.execute(()->{
                HIDProfileBuilder builder = new HIDProfileBuilder();
                builder.Build();
                dataPipe = new PlayEmDataPipe(builder.GetChunks());
                btManager.GattServerInit(builder.GetReportMap(),new byte[]{},dataPipe);
            });
        }
    }

    public void Disconnect(){
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
    };


    @Override
    public void onCreate(){
        super.onCreate();
        String CHANNEL_ID = "playEM_foreground_label";
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                "PlayEM Channel",
                NotificationManager.IMPORTANCE_DEFAULT);

        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("PlayEm BLE is still running in the background")
                .setContentText("").build();
        //TODO Add buttons to disable or bring APP to foreground
        startForeground(1, notification);
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
        _activityBind = false;
        this.appActivity = null;
        return super.onUnbind(intent);
    }
    @Override
    public void onRebind(Intent intent){
        super.onRebind(intent);
        _activityBind = true;
    }

    @Override
    public void onDestroy(){
        _activityBind = false;
        if(btManager!=null)
            btManager.Close();
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
        NOTIFY_ATTACHED

    }
}
