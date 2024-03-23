package com.example.playem.bluetoothLE.blehandlers;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.util.Log;

import com.example.playem.bluetoothLE.blehandlers.interfaces.GattResponse;
import com.example.playem.bluetoothLE.blehandlers.interfaces.BLECharacteristicsReadRequest;
import com.example.playem.hid.utils.HIDUtils;

//TODO: What happens if we directly change the report maps?
public class HIDReportMapCReadRequest implements BLECharacteristicsReadRequest {
    public HIDReportMapCReadRequest (byte[] reportMap){
        ReportMap = new byte[reportMap.length];
        System.arraycopy(reportMap,0,ReportMap,0,reportMap.length);

         //this is same as byte[] ->report map but only works to read
    }
    /*public void SetReportMap(byte[] reportMap){
        ReportMap = new byte[reportMap.length];
        System.arraycopy(reportMap,0,ReportMap,0,reportMap.length);
    }*/
    private final byte[] ReportMap; //Support write operations if req
    @Override
    public Runnable onCharacteristicReadRequest(BluetoothGattServer gattServer, BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, int offset) {

        return new Runnable() {
            @SuppressLint("MissingPermission")
            @Override
            public void run() {
                Log.i("REPORTMAP",String.format("ReportMap has %d bytes",ReportMap.length));
                if(offset>= ReportMap.length){
                    Log.e("REPORTMAP",String.format("offset was greater than length! %d",offset));
                    gattServer.sendResponse(device,requestId, BluetoothGatt.GATT_FAILURE,0,new byte[]{0});
                }else{
                    //TODO: Really slow op should mess with Mtus to speed things up
                    gattServer.sendResponse(device,requestId, BluetoothGatt.GATT_SUCCESS,offset, GattResponse.Slice(ReportMap,offset));
                    Log.w("REPORTMAP", HIDUtils.bytesToHex(GattResponse.Slice(ReportMap,offset)));
                }

            }
        };
    }
}
