package com.example.playem;

import android.Manifest;
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
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.example.playem.ViewCallbacks.GattServiceCallbacks;
import com.example.playem.btmanager.BluetoothPermissionsHandler;
import com.example.playem.btmanager.PlayEmBTManager;
import com.example.playem.generics.ServiceHandler;
import com.example.playem.hid.HIDProfileBuilder;
import com.example.playem.hid.interfaces.HIDChunk;
import com.example.playem.pipes.PlayEmDataPipe;
import com.example.playem.viewmodels.GattServiceState;

import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlayEmGATTService extends Service{
    public PlayEmGATTService(){
    }
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;

    @SuppressLint("MissingPermission")
    public void DeferredConstructor(Activity appActivity){ //Get States here
        if(!_constructed){
            executorPool = Executors.newFixedThreadPool(4);
            executorPool.execute(()->{
                bluetoothManager = getBluetoothManager();
                dataChunks = new HashMap<>();
                bluetoothAdapter =  bluetoothManager.getAdapter();
                if(btManager == null){
                    btManager = new PlayEmBTManager(this,executorPool,mediator,bluetoothManager);
                }
                _constructed = true;
                btPermissionHandler = new BluetoothPermissionsHandler();
                btPermissionHandler.SetFocusedActivity(appActivity);
                if(btPermissionHandler.CheckBTPermissions())
                    currentState.status = GattServiceState.SERVICE_STATUS.IDLE_NODATA;
                else
                    Log.e("PERM","Showing as no permissions granted");
                checkBTEnabled();
                bluetoothAdapter.setName("PlayEm_DEBUG");
                mediator.onServiceStatusChanged(null);
            });
        }
        this.focusedActivity = appActivity;
    }
    private GattServiceState currentState = new GattServiceState ("DISCONNECT","--:--:--:--:--","INVALID","", GattServiceState.SERVICE_STATUS.NOT_INIT,false);
    public GattServiceState getLastValidState(){
        return currentState.copy(currentState.Objectdirty);
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,@NonNull int[] grantResults){
        executorPool.execute(()->{
            if(btPermissionHandler!=null){
                btPermissionHandler.onRequestPermissionsResult(requestCode,permissions,grantResults);
            }
        });
    }
    private final ServiceHandler<Activity,GattServiceCallbacks> activityGattServiceCallback = new ServiceHandler<>(5);
    private boolean _constructed = false;
    private ExecutorService executorPool;
    private final IBinder mbinder = new mBinder();
    private PlayEmBTManager btManager;
    public PlayEmDataPipe dataPipe;
    private Activity focusedActivity;
    private HashMap<Integer, HIDChunk> dataChunks;
    private BroadcastReceiver bondStateReceiver;
    protected void BuildPipe(HIDProfileBuilder builder){
        if(_constructed){
            executorPool.execute(()->{
                //HIDProfileBuilder builder = new HIDProfileBuilder();
                builder.Build();
                dataPipe = new PlayEmDataPipe(builder.GetChunks());
                dataChunks = builder.GetChunks();
                btManager.GattServerInit(builder.GetReportMap(),new byte[]{},dataPipe);
                registerReceiver(bondStateReceiver = btManager.bondStateReceiver(),new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
            });
        }
    }
    private void checkBTEnabled() {
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(focusedActivity, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                focusedActivity.startActivityForResult(enableBtIntent, -1);//change api if time allows
            }
        }
    }
    private BluetoothManager getBluetoothManager() {
        BluetoothManager bm = (BluetoothManager) this.getSystemService(Context.BLUETOOTH_SERVICE);
        return bm;
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
        this.Disconnect(false);
    }
    protected void Disconnect(boolean forceBondRemoval){
        executorPool.execute(()->{
            if(_constructed){
                btManager.StopAdvertisement();
                btManager.Disconnect(forceBondRemoval);
                btManager.Close();
                _constructed = false;
                dataPipe = null;
                mediator.onGattStatusChanged(GattServiceState.SERVICE_STATUS.NOT_INIT);
                btManager = null;
                DeferredConstructor(this.focusedActivity);
            }
        });
    }
    public PlayEmDataPipe GetPipe(){
        if(dataPipe!=null){
            return  dataPipe;
        }
        return null;
    }
    protected HashMap<Integer,HIDChunk> GetHidChunks(){
        return dataChunks;
    }
    protected void SubscribeToEventBus(Activity activity,GattServiceCallbacks callbacks){
        activityGattServiceCallback.PutPair(activity,callbacks);
        if(btPermissionHandler!=null)
            btPermissionHandler.SetFocusedActivity(activity);
        focusedActivity = activity;
    }

    protected void UnsubscribeFromEventBus(Activity activity){
        activityGattServiceCallback.Remove(activity);
        btPermissionHandler.SetFocusedActivity(null);
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
                    currentState.status = GattServiceState.SERVICE_STATUS.BUILT_READY_BROADCAST;}
                }
            else{
                currentState.status = GattServiceState.SERVICE_STATUS.IDLE_NODATA;}
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
        int notificationID = 1;
        this.startForeground(notificationID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE);
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

    private BluetoothPermissionsHandler btPermissionHandler;

public class mBinder extends Binder{
    PlayEmGATTService getService(){
        return PlayEmGATTService.this;
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
