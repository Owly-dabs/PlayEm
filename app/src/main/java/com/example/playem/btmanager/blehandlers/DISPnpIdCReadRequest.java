package com.example.playem.btmanager.blehandlers;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;

import com.example.playem.btmanager.GattResponse;
import com.example.playem.btmanager.blehandlers.interfaces.BLECharacteristicsReadRequest;

public class DISPnpIdCReadRequest implements BLECharacteristicsReadRequest {
    private byte[] DISVendorID = {0x02,0,0,0,0,0,0};
    @Override
    public Runnable onCharacteristicReadRequest(BluetoothGattServer gattServer, BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, int offset) {
        return new Runnable() {
            @SuppressLint("MissingPermission")
            @Override
            public void run() {
                if(offset>=DISVendorID.length){
                    gattServer.sendResponse(device,requestId, BluetoothGatt.GATT_SUCCESS,0,new byte[]{});
                }else{
                    gattServer.sendResponse(device,requestId, BluetoothGatt.GATT_SUCCESS,offset, GattResponse.Slice(DISVendorID,offset));
                }
            }
        };
    }
}
