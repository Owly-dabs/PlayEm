package com.example.playem.bluetoothLE.blehandlers;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;

import com.example.playem.bluetoothLE.blehandlers.interfaces.GattResponse;
import com.example.playem.bluetoothLE.blehandlers.interfaces.BLECharacteristicsReadRequest;

public class DISManuIDCReadRequest implements BLECharacteristicsReadRequest {
    private final byte[] MANU_ID = "PlayEm SUTD".getBytes();
    @Override
    public Runnable onCharacteristicReadRequest(BluetoothGattServer gattServer, BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, int offset) {
        return new Runnable() {
            @SuppressLint("MissingPermission")
            @Override
            public void run() {
                int o = offset>=MANU_ID.length?0:offset;
                gattServer.sendResponse(device,requestId , BluetoothGatt.GATT_SUCCESS,offset, GattResponse.Slice(MANU_ID,offset));
            }
        };
    }
}
