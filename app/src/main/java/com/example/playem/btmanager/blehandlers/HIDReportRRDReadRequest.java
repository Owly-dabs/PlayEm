package com.example.playem.btmanager.blehandlers;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;

import com.example.playem.btmanager.blehandlers.interfaces.GattResponse;
import com.example.playem.btmanager.blehandlers.interfaces.BLEDescriptorReadRequest;

public class HIDReportRRDReadRequest implements BLEDescriptorReadRequest {
    public HIDReportRRDReadRequest(byte nIds,byte reportType,byte maxBufferSize){
        this.RRD = new byte[]{0x00,0x01};//nIds,reportType};//Page 29 of 123
    }
    private final byte[] RRD; //= new byte[]{(byte) 0x02,0x01};
    @Override
    public Runnable onDescriptorReadRequest(BluetoothGattServer gattServer, BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor) {
        return new Runnable() {
            @SuppressLint("MissingPermission")
            @Override
            public void run() {
                int o = offset>=RRD.length?0:offset;
                gattServer.sendResponse(device,requestId ,BluetoothGatt.GATT_SUCCESS,offset, GattResponse.Slice(RRD,o));
            }
        };
    }
}
