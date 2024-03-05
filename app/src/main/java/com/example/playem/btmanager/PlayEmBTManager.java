package com.example.playem.btmanager;

import static android.bluetooth.BluetoothDevice.BOND_BONDED;
import static android.bluetooth.BluetoothDevice.BOND_BONDING;
import static android.bluetooth.BluetoothDevice.BOND_NONE;
import static android.bluetooth.BluetoothDevice.ERROR;
import static android.bluetooth.BluetoothDevice.EXTRA_BOND_STATE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ServiceCompat;

import com.example.playem.MainActivity;
import com.example.playem.PlayEmGATTService;
import com.example.playem.ViewCallbacks.GattServiceCallbacks;
import com.example.playem.btmanager.blehandlers.BASReadRequest;
import com.example.playem.btmanager.blehandlers.DISManuIDCReadRequest;
import com.example.playem.btmanager.blehandlers.DISModelNoReadRequest;
import com.example.playem.btmanager.blehandlers.DISPnpIDReadRequest;
import com.example.playem.btmanager.blehandlers.DISSoftIDCReadRequest;
import com.example.playem.btmanager.blehandlers.HIDInformationCReadRequest;
import com.example.playem.btmanager.blehandlers.HIDProtoModeReadRequest;
import com.example.playem.btmanager.blehandlers.HIDReportCCCDReadRequest;
import com.example.playem.btmanager.blehandlers.HIDReportCReadRequest;
import com.example.playem.btmanager.blehandlers.HIDReportMapCReadRequest;
import com.example.playem.btmanager.blehandlers.HIDReportRRDReadRequest;
import com.example.playem.btmanager.blehandlers.interfaces.GattServerCbRouter;
import com.example.playem.btmanager.services.BLE_HIDServiceBuilder;
import com.example.playem.btmanager.services.HIDReportNotifier;
import com.example.playem.btmanager.services.UUIDUtil;
import com.example.playem.pipes.PlayEmDataPipe;

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

public class PlayEmBTManager extends BluetoothGattServerCallback implements GattServerCbRouter {
    //Dependency Injection
    //TODO: Remove debug cases
    public PlayEmBTManager(@NonNull MainActivity context, @NonNull Executor executor, @NonNull GattServiceCallbacks callbacks) {
        this.promises = callbacks;
        this.executor = executor; //DI
        this.parentActivity = context;
        this.serviceContext = serviceContext;
        checkHardware();
        checkBTEnabled();
    }
    private ServiceCompat serviceContext;
    private GattServiceCallbacks promises;
    private Function<PlayEmBTManager,Void> StopAdvertiseHID;

    //private MainActivity parentMainActivity;
    public ConcurrentLinkedQueue<BluetoothDevice> bondedDevices = new ConcurrentLinkedQueue<>();
    //public ConcurrentLinkedQueue<BluetoothDevice> availDevices = new ConcurrentLinkedQueue<>();
    public ConcurrentLinkedQueue<BluetoothDevice> connectedDevice = new ConcurrentLinkedQueue<>();

    @SuppressLint("MissingPermission")
    public void AdvertiseHID(){
        if(!advertStartReq) {
            advertStartReq = true;
            StopAdvertiseHID = null;
            BluetoothLeAdvertiser bleAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
            AdvertiseCallback advertiseCallback = new AdvertiseCallback() {
                @Override
                public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                    Log.i("BLEADVERT", String.format("onStartSuccess BLE advertisement : %s", settingsInEffect.toString()));
                    promises.onAdvertisementStateChanged(PlayEmGATTService.SERVICE_STATES.ADVERT_ENABLED);
                }
                @Override
                public void onStartFailure(int errorCode) {
                    advertStartReq = false;
                    Log.e("BLEADVERT", String.format("Failed To Start Advertiser : %d", errorCode));
                    promises.onAdvertisementStateChanged(PlayEmGATTService.SERVICE_STATES.ADVERT_ENABLED);
                }
            };
            bleAdvertiser.startAdvertising(advertiseSettings, advertiseData, advertiseScanResponse, advertiseCallback);
            StopAdvertiseHID = (PlayEmBTManager btManager)-> {
                btManager.advertStartReq = false;
                bleAdvertiser.stopAdvertising(advertiseCallback);
                StopAdvertiseHID = null;
                return null;
            };
        }
    }
    private AdvertiseData advertiseData;
    private AdvertiseData advertiseScanResponse;
    private AdvertiseSettings advertiseSettings;
    public void StopHIDAdvertisement(){
        if(StopAdvertiseHID!=null){
            StopAdvertiseHID.apply(this);
        }
    }
    private boolean advertStartReq = false;
    @SuppressLint("MissingPermission")
    public void Disconnect(){
        detachNotifier();
        for(BluetoothDevice d:ConnectedHost.values()){
            gattServer.cancelConnection(d);
        }
    }
    @SuppressLint("MissingPermission")
    public void Close(){
        if(this.gattServer!=null){
            detachNotifier();
            for(BluetoothDevice d:ConnectedHost.values()){
                gattServer.cancelConnection(d);
            }
            this.gattServer.close();
        }
        try{
            this.executor.wait(1000);
        }catch (Exception e){
            Log.w("BLETHREAD","BLE Closed called - Interrupted Thread timeout forcibly closing");
        }
    }
    private final Executor executor;
    //private final Executor UIexecutor;

    private PlayEmDataPipe dataPipe;
    @SuppressLint("MissingPermission")
    //TODO: Make it a service dependency
    public void GattServerInit(byte[] HID_ReportMap, byte[] emptyResponse, PlayEmDataPipe dataPipe) {
        if(dataPipe==null){
            Log.e("PIPE","DataPipe is Null!");
        }
        this.dataPipe = dataPipe;

        Log.i("GATTSERVER","Gatt Server is initializing");
        gattServer = bluetoothManager.openGattServer(parentActivity, this);
        BLE_HIDServiceBuilder.Build(serviceQueue,advertSettings,advertData);
        //Start Adding Services
        this.onServiceAdded(INITIAL_SERVICE_ADD,serviceQueue.poll());
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
            //parentActivity.registerReceiver(new PlayEmBondStateBroadcastReceiver(this.gattServer), new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
            //BluetoothManager bm = (BluetoothManager) parentActivity.getSystemService(Context.BLUETOOTH_SERVICE);

        for(Object d: bluetoothAdapter.getBondedDevices().toArray()){
            //Log.w("MATCHING","   "+((BluetoothDevice)d).getAddress());
            bondedDevices.add((BluetoothDevice)d);
        }
        promises.onBondedDevicesChange(bondedDevices);
        //UIexecutor.execute(parentMainActivity.UpdateBondedLists());
        //availDevices.addAll(bm.getConnectedDevices(BluetoothProfile.GATT_SERVER));
        advertiseSettings = advertSettings.poll();
        advertiseData = advertData.poll();
        advertiseScanResponse = advertData.poll();
    }

    //Will block
    private void checkBTEnabled() {
        if (hasHW && !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(parentActivity, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                parentActivity.startActivityForResult(enableBtIntent, -1);//change api if time allows
            }
        }
    }
    @SuppressLint("MissingPermission")
    private void checkHardware() {
        this.bluetoothManager = (BluetoothManager) parentActivity.getSystemService(Context.BLUETOOTH_SERVICE);
        if(bluetoothManager==null){
            hasHW = false;
            return;
        }
        bluetoothAdapter = bluetoothManager.getAdapter();
        hasHW = bluetoothAdapter != null;

    }

    private void detachNotifier(){
        NotificationTimer.cancel();
        NotificationTimer = null;
        promises.onNotifierChanged(PlayEmGATTService.SERVICE_STATES.NOTIFY_DETACHED);
    }
    public void attachNotifier(String dAddress) {

        try{
            attachNotifier(ConnectedHost.get(dAddress));
            promises.onNotifierChanged(PlayEmGATTService.SERVICE_STATES.NOTIFY_ATTACHED);
        }catch (Exception e){
            promises.onNotifierChanged(PlayEmGATTService.SERVICE_STATES.ACTION_FAIL);
        }
    }
    public BroadcastReceiver bondStateReceiver() {
        return new BroadcastReceiver() {
            @SuppressLint("MissingPermission")
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                // Ignore updates for other devices
                if (device == null)
                    return;
                // Check if action is valid
                if (action == null) {
                    Log.e("BOND", "Invalid action on broadcast receiver BOND STATE");
                    return;
                }
                // Take action depending on new bond state
                if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                    final int bondState = intent.getIntExtra(EXTRA_BOND_STATE, ERROR);
                    switch (bondState) {
                        case BluetoothDevice.BOND_BONDING:
                            Log.i("BOND", "Bonding In progress.... Nothing done here");
                            break;
                        case BOND_BONDED:
                            Log.i("BOND", String.format("%s %s has been paired (BONDED)", device.getName(), device.getAddress()));
                            if (gattServer.connect(device, true)) {
                                promises.onBondedDevicesChange(bondedDevices);
                                Log.i("BOND", "GattServer will attempting to connect");
                            } else {
                                Log.e("BOND", "GattServer cannot initialize connection attempt!");
                            }
                            break;
                        case BluetoothDevice.BOND_NONE:
                            Log.w("BOND", String.format("%s was UN-BONDED for an unknown reason", device.getName()));
                            break;
                    }
                }
            }
        };
    }

    private void attachNotifier(BluetoothDevice device){
        connectedDevice.add(device);
        if(NotificationTimer==null){
            NotificationTimer = new Timer("BLE Notifier");
        }
        TimerTask t = new HIDReportNotifier().onTimedNotifyCharacteristics(
                        gattServer,
                        device,
                        Objects.requireNonNull(activeServices.get(UUIDUtil.SERVICE_HID.toString())).getCharacteristic(UUIDUtil.CHAR_REPORT),
                        dataPipe
        );
        NotificationTimer.scheduleAtFixedRate(t,1000,12);//Wait 2 second before firing first then 12ms after
    }
    private Timer NotificationTimer;
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGattServer gattServer;
    private final MainActivity parentActivity;

    private final static int INITIAL_SERVICE_ADD = -99;

    private final Queue<BluetoothGattService> serviceQueue = new ConcurrentLinkedQueue<>();
    private final Queue<AdvertiseSettings> advertSettings= new ConcurrentLinkedQueue<>();
    private final Queue<AdvertiseData> advertData = new ConcurrentLinkedQueue<>();
    private final HashMap<String,BluetoothGattService> activeServices = new HashMap<>();

    private final ConcurrentHashMap<String,BluetoothDevice> ConnectedHost = new ConcurrentHashMap<>();
    @SuppressLint("MissingPermission")
    @Override
    public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
        super.onConnectionStateChange(device,status,newState);

        if(newState == BluetoothProfile.STATE_DISCONNECTED){
            //TODO Ensure Services Stop Here?
            if(!ConnectedHost.containsKey(device.getAddress())){
                Log.w("CONN","Unknown device flagged as DISCONNECTED ?? returning");
                return;
            }
            device = ConnectedHost.get(device.getAddress());
            if(device!=null){
                promises.onConnectionStateChanged(device.getAddress(), device.getName(), PlayEmGATTService.SERVICE_STATES.BLUETOOTH_DEVICE_DISCONNECT);
            }
            return;
        }
        if(newState == BluetoothProfile.STATE_CONNECTED){
            //Log.i("SERVICE","Attaching Notifier");
            ConnectedHost.put(device.getAddress(),device);
            if(device.getBondState()==BOND_NONE||device.getBondState()==BOND_BONDING){
                gattServer.cancelConnection(device);
                Log.w("CONN", "%s device is CONNECTED but unpaired - attempting to disconnect and force a bond");
                device.createBond();
            }
            if(device.getBondState()==BOND_BONDED){
                promises.onConnectionStateChanged(device.getAddress(), device.getName(), PlayEmGATTService.SERVICE_STATES.BLUETOOTH_DEVICE_CONNECTED);
                Log.i("CONNECT",String.format("New Device Connection State CONNECTED %d - %s",status, device.getAddress()));
            }

            //StopHIDAdvertisement();
            //SettingsWriter.WriteToFile(parentActivity,SettingsConst.P_BONDED,device.getAddress()).run();

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
            Log.i("GATTSERVICE",String.format("Service was successfully added %s", service));
            BluetoothGattService next = serviceQueue.poll();
            if(next != null){
                gattServer.addService(next);
            }
            /*else{
                //String pConnect = "";
                try {
                    //pConnect = SettingsWriter.ReadFromFile(parentActivity, SettingsConst.P_BONDED).call();
                    //pConnect = pConnect.substring(0,17);
                  //  Log.w("FILER",String.format("Read from file: %s",pConnect));
                } catch (Exception e) {
                    Log.e("FILE",e.toString());
                }
                //StopHIDAdvertisement();
                //AdvertiseHID();
            }*/
            else{
                promises.onGattStatusChanged(PlayEmGATTService.SERVICE_STATES.GATT_READY);
            }
        }else{
            if(status!=INITIAL_SERVICE_ADD)
                Log.e("GATTSERVICE",String.format("Could not add service: %s",service.toString()));
            gattServer.addService(service);
        }
    }

    @Override
    public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
        UUID charUUID = characteristic.getUuid();
        super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
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
            //Log.i("READ",String.format("Host is Writing: %s",characteristic.getUuid().toString()));
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
            //Log.i("READ",String.format("Host is reading Descriptor: %s",descriptor.getUuid().toString()));
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

}

