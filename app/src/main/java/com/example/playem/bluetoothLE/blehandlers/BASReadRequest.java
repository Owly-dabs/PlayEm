package com.example.playem.bluetoothLE.blehandlers;

import static android.content.Context.BATTERY_SERVICE;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.content.Context;
import android.os.BatteryManager;

import com.example.playem.bluetoothLE.blehandlers.interfaces.BLECharacteristicsReadRequest;

public class BASReadRequest implements BLECharacteristicsReadRequest {
    public BASReadRequest(Context context){
        this.context = context;
    }
    @Override
    public Runnable onCharacteristicReadRequest(BluetoothGattServer gattServer, BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, int offset) {
        return new Runnable() {
            @Override
            @SuppressLint("MissingPermission")
            public void run() {
                if(context!=null){
                    BatteryManager bm = (BatteryManager) context.getSystemService(BATTERY_SERVICE);
                    int batLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
                    gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, new byte[]{(byte)batLevel});
                }else{
                    gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, new byte[]{(byte)50});
                }
            }
        };
    }
    public void SetContext(Context context){
        this.context = context;
    }
    private Context context;
}