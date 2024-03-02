package com.example.playem.btmanager.blehandlers;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;

import com.example.playem.btmanager.blehandlers.interfaces.BLECharacteristicsReadRequest;

public class HIDReportCReadRequest implements BLECharacteristicsReadRequest {
    public HIDReportCReadRequest(byte[] emptyResponse, final byte[] hidReportRef){
        EmptyResponse = emptyResponse;
        this.HidReportRef = hidReportRef;
    }
    private final byte[] EmptyResponse;
    public final byte[] HidReportRef;
    @Override
    public Runnable onCharacteristicReadRequest(BluetoothGattServer gattServer, BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, int offset) {
        return new Runnable(){
            @SuppressLint("MissingPermission")
            @Override
            public void run(){
                synchronized (HidReportRef){
                    if(offset>=HidReportRef.length){
                        gattServer.sendResponse(device,requestId, BluetoothGatt.GATT_SUCCESS,0,HidReportRef);
                    }else{
                        gattServer.sendResponse(device,requestId, BluetoothGatt.GATT_SUCCESS,offset,EmptyResponse);
                    }
                }
            }
        };
    }
}
