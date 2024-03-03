package com.example.playem.btmanager.blehandlers;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;

import com.example.playem.btmanager.GattResponse;
import com.example.playem.btmanager.blehandlers.interfaces.BLECharacteristicsReadRequest;

public class DISSoftIDCReadRequest implements BLECharacteristicsReadRequest {
    private final byte[] SOFT_ID = "0.0.0.1".getBytes();
    @Override
    public Runnable onCharacteristicReadRequest(BluetoothGattServer gattServer, BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, int offset) {
        return new Runnable() {
            @SuppressLint("MissingPermission")
            @Override
            public void run() {
                int o = offset>=SOFT_ID.length?0:offset;
                gattServer.sendResponse(device,requestId , BluetoothGatt.GATT_SUCCESS,offset, GattResponse.Slice(SOFT_ID,offset));
            }
        };
    }
}
