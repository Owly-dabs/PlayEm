package com.example.playem.bluetoothLE.blehandlers;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;

import com.example.playem.bluetoothLE.blehandlers.interfaces.GattResponse;
import com.example.playem.bluetoothLE.blehandlers.interfaces.BLECharacteristicsReadRequest;

public class HIDInformationCReadRequest implements BLECharacteristicsReadRequest {
    final byte[] HIDINFO = {0x11,0x01,0x00,0x03}; //LSB b1-b2 1.11 || localization cc || Normally Connected && See pg 77 HID11 + HIDS_SPEC
    @Override
    public Runnable onCharacteristicReadRequest(BluetoothGattServer gattServer, BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, int offset) {
        return new Runnable() {
            @Override
            @SuppressLint("MissingPermission")
            public void run() {
                int o = offset;
                if(offset>3){
                    o =0;
                    gattServer.sendResponse(device,requestId, BluetoothGatt.GATT_SUCCESS,o, new byte[]{0});
                    return;
                }
                gattServer.sendResponse(device,requestId, BluetoothGatt.GATT_SUCCESS,o, GattResponse.Slice(HIDINFO,o));
            }
        };
    }
}
