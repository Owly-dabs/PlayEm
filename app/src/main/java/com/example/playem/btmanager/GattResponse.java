package com.example.playem.btmanager;

import android.bluetooth.BluetoothDevice;

public class GattResponse {
    public BluetoothDevice device;
    public int requestId;
    public int ReponseStatus;
    public int offset;
    public byte[] data;

    public static byte[] Slice(byte[] data,int offset){
        if(offset>=data.length){
            return new byte[]{}; //Empty
        }
        int remainder = data.length-offset;
        byte[] retarr = new byte[remainder];
        System.arraycopy(data,offset,retarr,0,remainder);
        return retarr;
    }
}
