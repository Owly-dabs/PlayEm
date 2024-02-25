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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.Queue;
import java.util.UUID;

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
    private void GattServerInit() {
        if (permissionHandler.CheckAllPermissions(parentActivity)) {
            gattServer = bluetoothManager.openGattServer(parentActivity, GattServerCbFactory.CreateGATTServerCallback(this));
            gattServer.addService(new BluetoothGattService(UUIDUtil.SERVICE_HID,BluetoothGattService.SERVICE_TYPE_PRIMARY));
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

    private Queue<BluetoothGattService> serviceQueue;
    private Queue<AdvertiseSettings> advertSettings;
    private Queue<AdvertiseData> advertData;

    public void EnqueueServiceAdd(BluetoothGattService service){
        serviceQueue.add(service);
    }
    private HashMap<String,BluetoothGattService> activeServices = new HashMap<>();
    @Override
    public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
        super.onConnectionStateChange(device, status, newState);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onServiceAdded(int status, BluetoothGattService service) {
        super.onServiceAdded(status, service);
        if(status == BluetoothGatt.GATT_SUCCESS){
            activeServices.put(service.getUuid().toString(),service);
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
        if(this.cReaders.containsKey(charUUID)){
            Objects.requireNonNull(cWriters.get(charUUID)).onCharacteristicWriteRequest(gattServer,device, requestId, characteristic,preparedWrite,responseNeeded,offset);
        }
        else{
            Log.e("GATTSERVER","Unknown Characteristics Write Request");
        }
    }

    @Override
    public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor) {
        super.onDescriptorReadRequest(device, requestId, offset, descriptor);
    }

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
    private BTPermissionHandler permissionHandler;
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

