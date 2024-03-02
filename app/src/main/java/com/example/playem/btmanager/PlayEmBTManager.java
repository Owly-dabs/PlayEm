package com.example.playem.btmanager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.*;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PlayEmBTManager extends BluetoothGattServerCallback implements GattServerCbRouter{

    public PlayEmBTManager(MainActivity context) {
        this.parentActivity = context;
        this.permissionHandler = new BTPermissionHandler("PlayEm BT Manager", btPermissionsHandles);
        checkHardware();
        if(!permissionHandler.CheckAllPermissions(parentActivity)) {
            permissionHandler.RequestMissingPermissions(parentActivity);
        }
        checkBTEnabled();
    }

    private final PermissionsHandle[] btPermissionsHandles = new PermissionsHandle[]{
            new BTPermissionHandle(Manifest.permission.BLUETOOTH_CONNECT),
            new BTPermissionHandle(Manifest.permission.BLUETOOTH_SCAN),
            new BTPermissionHandle(Manifest.permission.BLUETOOTH_ADVERTISE),
            new BTPermissionHandle(Manifest.permission.ACCESS_COARSE_LOCATION),
            new BTPermissionHandle(Manifest.permission.ACCESS_FINE_LOCATION),
    };
    @SuppressLint("MissingPermission")
    //TODO: Make it a service dependency
    private void GattServerInit(byte[] HID_ReportMap,byte[] emptyResponse, byte[] hidRefReport) {
        if (permissionHandler.CheckAllPermissions(parentActivity)) {
            gattServer = bluetoothManager.openGattServer(parentActivity, GattServerCbFactory.CreateGATTServerCallback(this));
            BLE_HIDServiceBuilder.Build(serviceQueue,advertSettings,advertData,HID_ReportMap);
            this.cReaders.put(UUIDUtil.CHAR_PNP_ID,(BLECharacteristicsReadRequest) new DISPnpIdCReadRequest());
            this.cReaders.put(UUIDUtil.CHAR_HID_INFORMATION,(BLECharacteristicsReadRequest) new HIDInformationCReadRequest());
            this.cReaders.put(UUIDUtil.CHAR_REPORT,(BLECharacteristicsReadRequest) new HIDReportCReadRequest(emptyResponse,hidRefReport));
            this.cReaders.put(UUIDUtil.CHAR_REPORT_MAP,(BLECharacteristicsReadRequest) new HIDReportMapCReadRequest(HID_ReportMap));
            this.dReaders.put(UUIDUtil.DESC_REPORT_REFERENCE, (BLEDescriptorReadRequest) new HIDReportRRDReadRequest());
            this.dReaders.put(UUIDUtil.DESC_CCC, (BLEDescriptorReadRequest) new HIDReportCCCDReadRequest());
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

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGattServer gattServer;
    private final AppCompatActivity parentActivity;

    private final Queue<BluetoothGattService> serviceQueue = new ConcurrentLinkedQueue<>();
    private final Queue<AdvertiseSettings> advertSettings= new ConcurrentLinkedQueue<>();
    private final Queue<AdvertiseData> advertData = new ConcurrentLinkedQueue<>();
    private final HashMap<String,BluetoothGattService> activeServices = new HashMap<>();

    private final ConcurrentHashMap<String,BluetoothDevice> ConnectedHost = new ConcurrentHashMap<>();
    @Override
    public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
        super.onConnectionStateChange(device, status, newState);
        if(ConnectedHost.containsKey(device.getAddress())){
            if(newState == BluetoothProfile.STATE_DISCONNECTED){
                //TODO Ensure Services Stop Here
                ConnectedHost.remove(device.getAddress());
            }
            return;
        }
        if(newState == BluetoothProfile.STATE_CONNECTED){
            ConnectedHost.put(device.getAddress(),device);
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onServiceAdded(int status, BluetoothGattService service) {
        super.onServiceAdded(status, service);
        if(status == BluetoothGatt.GATT_SUCCESS){
            if(activeServices.put(service.getUuid().toString(),service)!=null){
                Log.w("SERVICE",String.format("Previous Characteristics was not null! - %s",service));
            }
            BluetoothGattService next = serviceQueue.remove();
            if(next != null){
                gattServer.addService(next);
            }
        }else{
            Log.e("PERM",String.format("Could not add service: %s",service.toString()));
            gattServer.addService(service);
        }
    }

    @Override
    public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
        UUID charUUID = characteristic.getUuid();
        //super.onCharacteristicReadRequest(device, requestId, offset, characteristic);
        if(this.cReaders.containsKey(charUUID)){
            Objects.requireNonNull(cReaders.get(charUUID)).onCharacteristicReadRequest(gattServer,device, requestId, characteristic,offset);
        }
        else{
            Log.e("GATTSERVER","Unknown Characteristics Read Request");
        }
    }

    @Override
    public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
        super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
        UUID charUUID = characteristic.getUuid();
        if(this.cWriters.containsKey(charUUID)){
            Objects.requireNonNull(cWriters.get(charUUID)).onCharacteristicWriteRequest(gattServer,device, requestId, characteristic,preparedWrite,responseNeeded,offset);
        }
        else{
            Log.e("GATTSERVER","Unknown Characteristics Write Request");
        }
    }

    @Override
    public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor) {
        super.onDescriptorReadRequest(device, requestId, offset, descriptor);
        UUID desUUID = descriptor.getUuid();

        if(this.cReaders.containsKey(desUUID)){
            Objects.requireNonNull(dReaders.get(desUUID)).onDescriptorReadRequest(gattServer,device, requestId,offset,descriptor);//TODO See if runnable object can keep reference
        }
        else{
            Log.e("GATTSERVER","Unknown Characteristics Read Request");
        }
    }
    ////TODO Unknown if required/should be implemented Requirements
    @Override
    public void onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
        super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value);
    }

    @Override
    public void onExecuteWrite(BluetoothDevice device, int requestId, boolean execute) {
        super.onExecuteWrite(device, requestId, execute);
    }

    @Override
    public void onNotificationSent(BluetoothDevice device, int status) {
        super.onNotificationSent(device, status);
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
            if(requires.size()>0){
                String[] requiredPerm = new String[requires.size()];
                requiredPerm= requires.toArray(requiredPerm);
                ActivityCompat.requestPermissions(parentActivity, requiredPerm,2); //Could use request code for a hashed event system from activity
            }
        }
    }
    protected class BTPermissionHandle implements PermissionsHandle{
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
        @Override
        public String toString(){
            return new String(Arrays.copyOf(Permission.toCharArray(),Permission.length()));
        }
        public String getPermission(){
            return this.toString();
        }
    }
}

