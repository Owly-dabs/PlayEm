package com.example.playem.btmanager.blehandlers;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;

import com.example.playem.btmanager.blehandlers.interfaces.BLECharacteristicsReadRequest;
public class HIDReportMapCReadRequest implements BLECharacteristicsReadRequest {
    public HIDReportMapCReadRequest (byte[] reportMap){
        this.ReportMap = reportMap; //this is same as byte[] ->report map but only works to read
    }
    private final byte[] ReportMap; //Support write operations if req
    @Override
    public Runnable onCharacteristicReadRequest(BluetoothGattServer gattServer, BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, int offset) {
        return new Runnable() {
            @SuppressLint("MissingPermission")
            @Override
            public void run() {
                if(offset>= ReportMap.length){
                    gattServer.sendResponse(device,requestId, BluetoothGatt.GATT_SUCCESS,0,new byte[]{0});
                }else{
                    int remainder = ReportMap.length-offset;
                    byte[] _destarr = new byte[remainder];
                    System.arraycopy(ReportMap,offset,_destarr,0,remainder);//Really slow op should mess with Mtus to speed things up
                    gattServer.sendResponse(device,requestId, BluetoothGatt.GATT_SUCCESS,offset,_destarr);
                }

            }
        };
    }
}
