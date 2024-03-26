package com.example.playem.bluetoothLE.blehandlers;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;

import com.example.playem.bluetoothLE.blehandlers.interfaces.GattResponse;
import com.example.playem.bluetoothLE.blehandlers.interfaces.BLEDescriptorReadRequest;

public class HIDReportCCCDReadRequest implements BLEDescriptorReadRequest {
    private final byte[] CCCD = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
    @Override
    public Runnable onDescriptorReadRequest(BluetoothGattServer gattServer, BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor) {
        return new Runnable() {
            @SuppressLint("MissingPermission")
            @Override
            public void run() {
                int o = offset>=CCCD.length?0:offset;
                gattServer.sendResponse(device,requestId , BluetoothGatt.GATT_SUCCESS,offset, GattResponse.Slice(CCCD,offset));
            }
        };
    }
}
