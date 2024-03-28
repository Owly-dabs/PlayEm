package com.example.playem;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ServiceInfo;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.playem.ViewCallbacks.GattServiceCallbacks;
import com.example.playem.bluetoothLE.BtManager;
import com.example.playem.bluetoothLE.utils.PermissionHandler;
import com.example.playem.generics.ServiceHandler;
import com.example.playem.hid.HIDProfileBuilder;
import com.example.playem.pipes.HidBleDataPipe;
import com.example.playem.viewmodels.GattServiceState;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppGattService extends Service{
    public AppGattService(){
    }
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;

    @SuppressLint("MissingPermission")
    public void DeferredConstructor(Activity appActivity){ //Get States here
        if(!_constructed){
            Log.i("CONSTRUCT","INIT Constructor");
            if(executorPool==null)
                executorPool = Executors.newSingleThreadExecutor();
            executorPool.execute(()->{
                bluetoothManager = getBluetoothManager();
                bluetoothAdapter =  bluetoothManager.getAdapter();
                if(btManager == null){
                    btManager = new BtManager(this,executorPool,mediator,bluetoothManager);
                }
                _constructed = true;
                if(PermissionHandler.Valid)
                    currentState.status = GattServiceState.SERVICE_STATUS.IDLE_NODATA;
                else
                    Log.e("PERM","Showing as no permissions granted");


                String CHANNEL_ID = "playEM_channelID";
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                        "PlayEM Channel",
                        NotificationManager.IMPORTANCE_DEFAULT);

                ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);
                Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setOngoing(true)
                        .setContentTitle("PlayEm BLE")
                        .setContentText("Bluetooth is Running in the Background").build();


                int notificationID = 1;
                this.startForeground(notificationID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE);
                PermissionHandler.CheckOrRequestBtEnable(appActivity,bluetoothAdapter);
                mediator.onServiceStatusChanged(null);
            });
        }
        this.focusedActivity = appActivity;
    }
    private GattServiceState currentState = new GattServiceState ("DISCONNECT","--:--:--:--:--","INVALID","", GattServiceState.SERVICE_STATUS.NOT_INIT,false);
    public GattServiceState getLastValidState(){
        return currentState.copy(currentState.Objectdirty);
    }
    private final ServiceHandler<Activity,GattServiceCallbacks> activityGattServiceCallback = new ServiceHandler<>(5);
    private boolean _constructed = false;
    private ExecutorService executorPool;
    private final IBinder mbinder = new mBinder();
    private BtManager btManager;
    public HidBleDataPipe dataPipe;
    private Activity focusedActivity;
    private BroadcastReceiver bondStateReceiver;

    public void BuildPipe(HIDProfileBuilder builder){
        builder.Build();
        BuildPipe(builder,new HidBleDataPipe(builder.GetChunks()));
    }
    public void BuildPipe(HIDProfileBuilder builder, HidBleDataPipe dataPipe){
        if(_constructed){
            executorPool.execute(()->{
                this.dataPipe = dataPipe;
                btManager.GattServerInit(builder.GetReportMap(),new byte[]{},dataPipe);
                registerReceiver(bondStateReceiver = btManager.bondStateReceiver(),new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
            });
        }
    }
    private BluetoothManager getBluetoothManager() {
        return (BluetoothManager) this.getSystemService(Context.BLUETOOTH_SERVICE);
    }
    public void StartAdvertisement(){
        if(_constructed){
            executorPool.execute(()-> btManager.AdvertiseHID());
        }
    }
    protected void StartInput(String address){
        final int MAX_MAC_LEN = 17;
        if(address.length()> MAX_MAC_LEN){
            Log.e("INPUT","Invalid address given");
            return;
        }
        if(_constructed){
            executorPool.execute(()-> btManager.attachNotifier(address));
        }
    }
    protected void Disconnect(){
        this.Disconnect(false);
    }
    @SuppressLint("MissingPermission")
    protected void Disconnect(boolean forceBondRemoval){
        executorPool.execute(()->{
            if(_constructed){
                unregisterReceiver(bondStateReceiver);
                btManager.StopAdvertisement();
                btManager.Disconnect(forceBondRemoval);
                _constructed = false;
                dataPipe = null;
                mediator.onGattStatusChanged(GattServiceState.SERVICE_STATUS.NOT_INIT);
                btManager = null;
            }
        });
        Timer t = new Timer();//TODO hook with GUI to sync
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                DeferredConstructor(AppGattService.this.focusedActivity);
            }
        },20000);
    }

    protected void SubscribeToEventBus(Activity activity,GattServiceCallbacks callbacks){
        activityGattServiceCallback.PutPair(activity,callbacks);
        focusedActivity = activity;
    }

    protected void UnsubscribeFromEventBus(Activity activity){
        activityGattServiceCallback.Remove(activity);
        focusedActivity = null;
    }

    private final GattServiceCallbacks mediator = new GattServiceCallbacks() {
        @Override
        public Runnable onBondedDevicesChange(ConcurrentLinkedQueue<BluetoothDevice> bondedDevices) {
            for(int i =0; i<activityGattServiceCallback.size();i++) {
                Activity a = activityGattServiceCallback.GetK(i);
                Runnable activityAction = activityGattServiceCallback.Get(i).onBondedDevicesChange(bondedDevices);
                if(activityAction!=null && a!=null)
                    a.runOnUiThread(activityAction);
            }
            return null;
        }
        @SuppressLint("MissingPermission")
        @Override
        public Runnable onConnectionStateChanged(String d_address, String d_name, SERVICE_STATES state) {
            if(state == SERVICE_STATES.BLUETOOTH_DEVICE_CONNECTED){
                BluetoothDevice btd= bluetoothAdapter.getRemoteDevice(d_address);
                if(btd.getBondState() == BluetoothDevice.BOND_BONDED){
                   if(currentState.status.ordinal() < GattServiceState.SERVICE_STATUS.BUILT_READY_BROADCAST.ordinal()){
                       currentState.status = GattServiceState.SERVICE_STATUS.DONT_TOUCHME;
                   }else{
                       currentState.status = GattServiceState.SERVICE_STATUS.CONNECTED_IDLE;
                   }
                }else{
                    currentState.status = GattServiceState.SERVICE_STATUS.CONNECTING;
                }
                currentState.address = d_address;
                currentState.name = d_name==null?"Unknown":d_name;
                currentState.bondstate = btd.getBondState()==BluetoothDevice.BOND_BONDED?"BONDED":"NO BOND";
            }
            else{
                if(currentState.status.ordinal()>= GattServiceState.SERVICE_STATUS.ADVERT.ordinal()){
                    currentState.status = GattServiceState.SERVICE_STATUS.ADVERT;
                }
            }
            this.onServiceStatusChanged(null);
            for(int i =0; i<activityGattServiceCallback.size();i++) {
                Activity a = activityGattServiceCallback.GetK(i);
                Runnable activityAction = activityGattServiceCallback.Get(i).onConnectionStateChanged(d_address, d_name, state);
                if(activityAction!=null)
                    a.runOnUiThread(activityAction);
            }
            return null;
        }
        @Override
        public Runnable onAdvertisementStateChanged(SERVICE_STATES state) {
            if(state == SERVICE_STATES.ADVERT_ENABLED)
                currentState.status = GattServiceState.SERVICE_STATUS.ADVERT;
            else
                currentState.status = GattServiceState.SERVICE_STATUS.CONNECTED_IDLE;
            this.onServiceStatusChanged(null);
            for(int i =0; i<activityGattServiceCallback.size();i++) {
                Activity a = activityGattServiceCallback.GetK(i);
                Runnable activityAction = activityGattServiceCallback.Get(i).onAdvertisementStateChanged(state);
                if(activityAction!=null)
                    a.runOnUiThread(activityAction);
            }
            return null;
        }

        @Override
        public Runnable onNotifierChanged(SERVICE_STATES state) {
            if(state==SERVICE_STATES.NOTIFY_ATTACHED){
                currentState.status = GattServiceState.SERVICE_STATUS.NOTIFY;
            }else{
                currentState.status = GattServiceState.SERVICE_STATUS.BUILT_READY_BROADCAST;
            }
            this.onServiceStatusChanged(null);
            for(int i =0; i<activityGattServiceCallback.size();i++) {
                Activity a = activityGattServiceCallback.GetK(i);
                Runnable activityAction = activityGattServiceCallback.Get(i).onNotifierChanged(state);
                if(activityAction!=null)
                    a.runOnUiThread(activityAction);
            }
            return null;
        }

        @Override
        public Runnable onServicesAddComplete(SERVICE_STATES state) {
            if(state == SERVICE_STATES.ACTION_SUCCESS){
                if(currentState.status== GattServiceState.SERVICE_STATUS.DONT_TOUCHME){
                    currentState.status = GattServiceState.SERVICE_STATUS.CONNECTED_IDLE;
                }else{
                    currentState.status = GattServiceState.SERVICE_STATUS.BUILT_READY_BROADCAST;
                }
            }
            else{
                currentState.status = GattServiceState.SERVICE_STATUS.IDLE_NODATA;
            }
            this.onServiceStatusChanged(null);
            for(int i =0; i<activityGattServiceCallback.size();i++) {
                Activity a = activityGattServiceCallback.GetK(i);
                Runnable activityAction = activityGattServiceCallback.Get(i).onServicesAddComplete(state);
                if(activityAction!=null)
                    a.runOnUiThread(activityAction);
            }
            return null;
        }

        @Override
        public Runnable onServiceReady(SERVICE_STATES state) {
            if(currentState.status.ordinal()< GattServiceState.SERVICE_STATUS.IDLE_NODATA.ordinal())
                currentState.status = GattServiceState.SERVICE_STATUS.PERMISSIONS_WAIT;
            this.onServiceStatusChanged(null);
            for(int i =0; i<activityGattServiceCallback.size();i++) {
                Activity a = activityGattServiceCallback.GetK(i);
                Runnable activityAction = activityGattServiceCallback.Get(i).onServiceReady(state);//Todo ensure all calls are nullable
                if (activityAction != null)
                    a.runOnUiThread(activityAction);
            }
            return null;
        }

        @Override
        public Runnable onGattStatusChanged(GattServiceState.SERVICE_STATUS state) {
            if(state== GattServiceState.SERVICE_STATUS.NOT_INIT)
                currentState = new GattServiceState("DISCONNECTED","(INVALID)","S_CLOSE","NO_LIST", GattServiceState.SERVICE_STATUS.NOT_INIT,true);
            for(int i =0; i<activityGattServiceCallback.size();i++) {
                Activity a = activityGattServiceCallback.GetK(i);
                Runnable activityAction = activityGattServiceCallback.Get(i).onGattStatusChanged(state);
                if(activityAction!=null)
                    a.runOnUiThread(activityAction);
            }
            return onServiceStatusChanged(null);
        }

        @Override
        public Runnable onServiceStatusChanged(GattServiceState newServiceState){
            for(int i =0; i<activityGattServiceCallback.size();i++) {
                Activity a = activityGattServiceCallback.GetK(i);
                Runnable activityAction = activityGattServiceCallback.Get(i).onServiceStatusChanged(currentState.copy(currentState.Objectdirty));
                if(activityAction!=null)
                    a.runOnUiThread(activityAction);
            }
            return null;
        }
    };

    @Override
    public void onCreate(){
        super.onCreate();

        //TODO Add buttons to disable or bring APP to foreground

    }

    @Override
    public int onStartCommand(Intent intent,int flags,int startID){
        return super.onStartCommand(intent,flags,startID);
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mbinder;
    }

    @Override
    public boolean onUnbind(Intent intent){
        Log.w("SERV","Application Unbound...");
        return super.onUnbind(intent);
    }
    @Override
    public void onRebind(Intent intent){
        super.onRebind(intent);
    }

    @Override
    public void onDestroy(){
        if(bondStateReceiver!=null)
            unregisterReceiver(bondStateReceiver);
        if(btManager!=null)
            btManager.Close();
        if(executorPool!=null)
            executorPool.shutdown();
        stopForeground(STOP_FOREGROUND_REMOVE);
        super.onDestroy();
    }

public class mBinder extends Binder{
    AppGattService getService(){
        return AppGattService.this;
    }
}
public enum SERVICE_STATES {
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
