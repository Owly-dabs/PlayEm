package com.example.playem.bluetoothLE.blehandlers;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;

import com.example.playem.bluetoothLE.blehandlers.interfaces.GattResponse;
import com.example.playem.bluetoothLE.blehandlers.interfaces.BLECharacteristicsReadRequest;

public class DISModelNoReadRequest implements BLECharacteristicsReadRequest {
    private final byte[] ModelNoString = "Arbitrary Model ID".getBytes();
    @Override
    public Runnable onCharacteristicReadRequest(BluetoothGattServer gattServer, BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, int offset) {
        return new Runnable() {
            @SuppressLint("MissingPermission")
            @Override
            public void run() {
                if(offset>=ModelNoString.length){
                    gattServer.sendResponse(device,requestId, BluetoothGatt.GATT_SUCCESS,0,new byte[]{0});
                }else{
                    gattServer.sendResponse(device,requestId, BluetoothGatt.GATT_SUCCESS,offset, GattResponse.Slice(ModelNoString,offset));
                }
            }
        };
    }
}
