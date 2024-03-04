package com.example.playem.btmanager.blehandlers;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;

import com.example.playem.btmanager.blehandlers.interfaces.GattResponse;
import com.example.playem.btmanager.blehandlers.interfaces.BLECharacteristicsReadRequest;

public class HIDProtoModeReadRequest implements BLECharacteristicsReadRequest {
    private final byte[] HID_PROTO_MODE = new byte[]{0x01};
    @Override
    public Runnable onCharacteristicReadRequest(BluetoothGattServer gattServer, BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, int offset) {
        return new Runnable() {
            @Override
            @SuppressLint("MissingPermission")
            public void run() {
                int o = offset;
                if(offset>1){
                    o =0;
                }
                gattServer.sendResponse(device,requestId, BluetoothGatt.GATT_SUCCESS,o, GattResponse.Slice(HID_PROTO_MODE,o));
            }
        };
    }
}
