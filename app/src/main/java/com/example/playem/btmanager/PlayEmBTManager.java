package com.example.playem.btmanager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.*;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.playem.MainActivity;
import com.example.playem.PermissionHandlerDelegate;
import com.example.playem.PermissionsHandle;
import com.example.playem.PermissionsHandler;
import com.example.playem.btmanager.blehandlers.*;
import com.example.playem.btmanager.blehandlers.interfaces.BLECharacteristicsReadRequest;
import com.example.playem.btmanager.blehandlers.interfaces.BLEDescriptorReadRequest;
import com.example.playem.btmanager.services.BLE_HIDServiceBuilder;
import com.example.playem.pipes.PlayEmDataPipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.function.Function;

public class PlayEmBTManager extends BluetoothGattServerCallback implements GattServerCbRouter{
    //Dependency Injection
    public PlayEmBTManager(MainActivity context, Executor executor, Executor UI_executor) {
        this.parentActivity = context; //DI
        this.permissionHandler = new BTPermissionHandler("PlayEm BT Manager", btPermissionsHandles);
        checkHardware();
        if(!permissionHandler.CheckAllPermissions(parentActivity)) {
            permissionHandler.RequestMissingPermissions(parentActivity);
        }
        checkBTEnabled();
        this.executor = executor; //DI
        this.UIexecutor = UI_executor; //DI for future callbacks if required.
    }
    private Function<PlayEmBTManager,Void> StopAdvertiseHID;
    @SuppressLint("MissingPermission")
    public void AdvertiseHID(){
        if(!advertStartReq) {
            advertStartReq = true;
            StopAdvertiseHID = null;
            BluetoothLeAdvertiser bleAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
            AdvertiseCallback advertiseCallback = new AdvertiseCallback() {
                @Override
                public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                    Log.i("BLEADVERT", String.format("Success in Advert! : %s", settingsInEffect.toString()));
                }
                @Override
                public void onStartFailure(int errorCode) {
                    advertStartReq = false;
                    Log.e("BLEADVERT", String.format("Failed To Start Advertiser : %d", errorCode));
                }
            };
            bleAdvertiser.startAdvertising(advertSettings.poll(), advertData.poll(), advertiseCallback);
            StopAdvertiseHID = (PlayEmBTManager btManager)-> {
                btManager.advertStartReq = false;
                bleAdvertiser.stopAdvertising(advertiseCallback);
                return null;
            };
        }
    }
    public void StopHIDAdvertisement(){
        if(StopAdvertiseHID!=null){
            StopAdvertiseHID.apply(this);
        }
    }
    private boolean advertStartReq = false;
    @SuppressLint("MissingPermission")
    public void Close(){
        if(this.gattServer!=null){
            this.gattServer.close();
        }
        try{
            this.executor.wait(10);
        }catch (Exception e){
            Log.w("BLETHREAD","Interrupted Thread timeout forcibly closing");
        }
    }
    private final Executor executor;
    private final Executor UIexecutor;
    private final PermissionsHandle[] btPermissionsHandles = new PermissionsHandle[]{
            new BTPermissionHandle(Manifest.permission.BLUETOOTH_CONNECT),
            new BTPermissionHandle(Manifest.permission.BLUETOOTH_SCAN),
            new BTPermissionHandle(Manifest.permission.BLUETOOTH_ADVERTISE),
            new BTPermissionHandle(Manifest.permission.ACCESS_COARSE_LOCATION),
            new BTPermissionHandle(Manifest.permission.ACCESS_FINE_LOCATION),
    };

    private PlayEmDataPipe dataPipe;
    @SuppressLint("MissingPermission")
    //TODO: Make it a service dependency
    public void GattServerInit(byte[] HID_ReportMap, byte[] emptyResponse, PlayEmDataPipe dataPipe) {
        if(dataPipe==null){
            Log.e("PIPE","DataPipe is Null!");
        }
        this.dataPipe = dataPipe;
        if (permissionHandler.CheckAllPermissions(parentActivity)) {
            Log.i("GATTSERVER","Gatt Server is initializing");
            gattServer = bluetoothManager.openGattServer(parentActivity, this);
            BLE_HIDServiceBuilder.Build(serviceQueue,advertSettings,advertData);
            //Start Adding Services
            this.onServiceAdded(-1,serviceQueue.poll());
            this.cReaders.put(UUIDUtil.CHAR_PNP_ID,new DISPnpIDReadRequest());
            this.cReaders.put(UUIDUtil.CHAR_MODEL_NO, new DISModelNoReadRequest());
            this.cReaders.put(UUIDUtil.CHAR_MANU_STR, new DISManuIDCReadRequest());
            this.cReaders.put(UUIDUtil.CHAR_SOFT_STR, new DISSoftIDCReadRequest());
            this.cReaders.put(UUIDUtil.CHAR_HID_INFORMATION, new HIDInformationCReadRequest());
            this.cReaders.put(UUIDUtil.CHAR_REPORT, new HIDReportCReadRequest(emptyResponse,dataPipe));
            this.cReaders.put(UUIDUtil.CHAR_REPORT_MAP, new HIDReportMapCReadRequest(HID_ReportMap));
            this.cReaders.put(UUIDUtil.CHAR_PROTO_MODE, new HIDProtoModeReadRequest());
            this.dReaders.put(UUIDUtil.DESC_REPORT_REFERENCE, new HIDReportRRDReadRequest((byte) 0x01, (byte) 0x01,(byte)12));
            this.dReaders.put(UUIDUtil.DESC_CCC, new HIDReportCCCDReadRequest());
            this.cReaders.put(UUIDUtil.CHAR_BATTERY_LEVEL, new BASReadRequest());
            //TODO Check if Report descriptors need write functions from Host
            //TODO Check if Any Characteristics need Write functions from Host
        }
    }
    private void checkBTEnabled() {
        if (hasHW && !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(parentActivity, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                parentActivity.startActivityForResult(enableBtIntent, -1);//change api if time allows
            }else{
                permissionHandler.RequestMissingPermissions(parentActivity);
            }
        }
    }
    private void checkHardware() {
        this.bluetoothManager = (BluetoothManager) parentActivity.getSystemService(Context.BLUETOOTH_SERVICE);
        if(bluetoothManager==null){
            hasHW = false;
            return;
        }
        bluetoothAdapter = bluetoothManager.getAdapter();
        hasHW = bluetoothAdapter != null;
    }

    private void attachNotifier(BluetoothDevice device){
        if(NotificationTimer==null){
            NotificationTimer = new Timer("BLE Notifier");
        }
        NotificationTimer.purge();
        TimerTask t = new HIDReportNotifier().onTimedNotifyCharacteristics(
                        gattServer,device,
                        Objects.requireNonNull(activeServices.get(UUIDUtil.SERVICE_HID.toString())).getCharacteristic(UUIDUtil.CHAR_REPORT),
                        dataPipe);
        NotificationTimer.scheduleAtFixedRate(t,500,10);//Wait half a second before firing first then 15ms after
    }
    private Timer NotificationTimer;
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGattServer gattServer;
    private final AppCompatActivity parentActivity;

    private final Queue<BluetoothGattService> serviceQueue = new ConcurrentLinkedQueue<>();
    private final Queue<AdvertiseSettings> advertSettings= new ConcurrentLinkedQueue<>();
    private final Queue<AdvertiseData> advertData = new ConcurrentLinkedQueue<>();
    private final HashMap<String,BluetoothGattService> activeServices = new HashMap<>();

    private final ConcurrentHashMap<String,BluetoothDevice> ConnectedHost = new ConcurrentHashMap<>();
    @SuppressLint("MissingPermission")
    @Override
    public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
        //super.onConnectionStateChange(device, status, newState);
        Log.w("CONNECT",String.format("New Device Connection State %d - %s",status, device.getAddress()));

        if(newState == BluetoothProfile.STATE_DISCONNECTED){
            //TODO Ensure Services Stop Here
            ConnectedHost.remove(device.getAddress());
            BroadcastReceiver checkBondState = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE,BluetoothDevice.ERROR);
                    if(state == BluetoothDevice.BOND_BONDED){
                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        synchronized (gattServer){
                            gattServer.connect(device,true);
                        }
                        context.unregisterReceiver(this);
                    }
                }
            };
            parentActivity.registerReceiver(checkBondState, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
            //auto reconnect should be handling unintended disconnects
            Log.w("DISCONNECT",String.format("New Device Connection State DISCONNECTED %d - %s",status, device.getAddress()));
            return;
        }

        if(newState == BluetoothProfile.STATE_CONNECTED){
            Log.i("SERVICE","Attaching Notifier");
            ConnectedHost.put(device.getAddress(),device);
            StopHIDAdvertisement();
            attachNotifier(ConnectedHost.get(device.getAddress()));
            Log.i("CONNECT",String.format("New Device Connection State CONNECTED %d - %s",status, device.getAddress()));
        }
        /*if(device.getBondState()==BluetoothDevice.BOND_NONE){
            Log.w("CONNECT","New Device Connection State CONNECTED but BONDSTATE is NONE");
            device.createBond();
        }*/
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onServiceAdded(int status, BluetoothGattService service) {
        super.onServiceAdded(status, service);
        if(status == BluetoothGatt.GATT_SUCCESS){
            if(activeServices.put(service.getUuid().toString(),service)!=null){
                Log.w("GATTSERVICE",String.format("Previous Characteristics was not null! - %s",service));
            }
            Log.i("GATTSERVICE",String.format("Service was successfully added %s",service.toString()));
            BluetoothGattService next = serviceQueue.poll();
            if(next != null){
                gattServer.addService(next);
            }
        }else{
            Log.e("GATTSERVICE",String.format("Could not add service: %s",service.toString()));
            gattServer.addService(service);
        }
    }

    @Override
    public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
        UUID charUUID = characteristic.getUuid();
        //super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
        if(this.cReaders.containsKey(charUUID)){
            Log.i("READ",String.format("Host is reading: %s",characteristic.getUuid().toString()));
            executor.execute(Objects.requireNonNull(cReaders.get(charUUID)).onCharacteristicReadRequest(gattServer,device, requestId, characteristic,offset));
        }
        else{
            Log.e("GATTSERVER",String.format("Unknown Characteristics Read Request - %s",characteristic.getUuid().toString()));
            for(UUID k:cReaders.keySet()){
                Log.i("cReader","   " + k.toString());
            }
        }
    }

    @Override
    public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
        super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
        UUID charUUID = characteristic.getUuid();
        if(this.cWriters.containsKey(charUUID)){
            Log.i("READ",String.format("Host is Writing: %s",characteristic.getUuid().toString()));
            executor.execute(Objects.requireNonNull(cWriters.get(charUUID)).onCharacteristicWriteRequest(gattServer,device, requestId, characteristic,preparedWrite,responseNeeded,offset));
        }
        else{
            Log.e("GATTSERVER",String.format("Unknown Characteristics Write Request - %s",characteristic.getUuid().toString()));
        }
    }

    @Override
    public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor) {
        super.onDescriptorReadRequest(device, requestId, offset, descriptor);
        UUID desUUID = descriptor.getUuid();

        if(this.dReaders.containsKey(desUUID)){
            Log.i("READ",String.format("Host is reading Descriptor: %s",descriptor.getUuid().toString()));
            executor.execute(Objects.requireNonNull(dReaders.get(desUUID)).onDescriptorReadRequest(gattServer,device, requestId,offset,descriptor));
        }
        else{
            Log.e("GATTSERVER",String.format("Unknown Descriptor Read Request - %s", descriptor.getUuid().toString()));
        }
    }
    ////TODO Unknown if required/should be implemented Requirements
    @Override
    public void onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
        Log.e("GATTSERVER",String.format("Unknown Descriptor Write Request - %s", descriptor.getUuid().toString()));
        super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value);
    }

    @Override
    public void onExecuteWrite(BluetoothDevice device, int requestId, boolean execute) {
        super.onExecuteWrite(device, requestId, execute);
    }

    @Override
    public void onNotificationSent(BluetoothDevice device, int status) {
        //Log.i("BTMANAGER",String.format("Notification sent %s %d",device.getAddress(),status)); //Remove when reverting to fast poll
        super.onNotificationSent(device, status);
        synchronized (dataPipe){
            dataPipe.NotifyComplete();
        }
    }

    @Override
    public void onMtuChanged(BluetoothDevice device, int mtu) {
        super.onMtuChanged(device, mtu);
    }

    @Override
    public void onPhyUpdate(BluetoothDevice device, int txPhy, int rxPhy, int status) {
        super.onPhyUpdate(device, txPhy, rxPhy, status);
    }

    @Override
    public void onPhyRead(BluetoothDevice device, int txPhy, int rxPhy, int status) {
        super.onPhyRead(device, txPhy, rxPhy, status);
    }
    private boolean hasHW = false;
    private final BTPermissionHandler permissionHandler;
    protected  class BTPermissionHandler extends PermissionsHandler{
        public BTPermissionHandler(String GroupName, PermissionsHandle[] Permissions_to_track) {
            super(GroupName, Permissions_to_track);
        }
        @Override
        protected void RequestMissingPermissions(AppCompatActivity context) {
            ArrayList<String> requires = new ArrayList<>();
            for(PermissionsHandle s: btPermissionsHandles)
            {
                if(ActivityCompat.checkSelfPermission(parentActivity, s.getPermission())!= PackageManager.PERMISSION_GRANTED){
                    requires.add(s.getPermission());
                    Log.w("PERM","Requiring:"+s);
                    ((PermissionHandlerDelegate)parentActivity).RegisterRequests(s);
                }
            }
            if(!requires.isEmpty()){
                String[] requiredPerm = new String[requires.size()];
                requiredPerm= requires.toArray(requiredPerm);
                ActivityCompat.requestPermissions(parentActivity, requiredPerm,2); //Could use request code for a hashed event system from activity
            }
        }
    }
    protected static class BTPermissionHandle implements PermissionsHandle{
        protected BTPermissionHandle(String permission){
            Permission = permission;
        }
        protected String Permission;
        @Override
        public void NotGranted() {
            Log.e("PERM",Permission +" Not Granted!");
        }
        @Override
        public void Granted() {

        }
        @Override
        public String Rationale() {
            return null;
        }
        @NonNull
        @Override
        public String toString(){
            return new String(Arrays.copyOf(Permission.toCharArray(),Permission.length()));
        }
        public String getPermission(){
            return this.toString();
        }
    }
}

