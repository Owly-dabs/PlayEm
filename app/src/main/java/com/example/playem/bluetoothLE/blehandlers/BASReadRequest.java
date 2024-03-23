package com.example.playem.bluetoothLE.blehandlers;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;

import com.example.playem.bluetoothLE.blehandlers.interfaces.BLECharacteristicsReadRequest;

public class BASReadRequest implements BLECharacteristicsReadRequest {
    @Override
    public Runnable onCharacteristicReadRequest(BluetoothGattServer gattServer, BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, int offset) {
        return new Runnable() {
            @Override
            @SuppressLint("MissingPermission")
            public void run() {
                gattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, new byte[]{0x60});
            }
        };
    }
}