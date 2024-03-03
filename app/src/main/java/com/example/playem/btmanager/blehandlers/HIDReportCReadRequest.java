package com.example.playem.btmanager.blehandlers;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;

import com.example.playem.btmanager.GattResponse;
import com.example.playem.btmanager.blehandlers.interfaces.BLECharacteristicsReadRequest;
import com.example.playem.pipes.PlayEmDataPipe;

public class HIDReportCReadRequest implements BLECharacteristicsReadRequest {
    public HIDReportCReadRequest(byte[] emptyResponse, PlayEmDataPipe hidReportRef){
        EmptyResponse = emptyResponse;
        this.HidReportRef = hidReportRef;
    }
    private final byte[] EmptyResponse;
    public final PlayEmDataPipe HidReportRef;
    @Override
    public Runnable onCharacteristicReadRequest(BluetoothGattServer gattServer, BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, int offset) {
        return new Runnable(){
            @SuppressLint("MissingPermission")
            @Override
            public void run(){
                synchronized (HidReportRef){
                    if(offset>=HidReportRef.getTsize()){
                        gattServer.sendResponse(device,requestId, BluetoothGatt.GATT_SUCCESS,0,EmptyResponse);
                    }else{
                        gattServer.sendResponse(device,requestId, BluetoothGatt.GATT_SUCCESS,offset, HidReportRef.GetReport() );//GattResponse.Slice(HidReportRef.GetReport(),offset));
                    }
                }
            }
        };
    }
}
