package com.example.playem.btmanager;

import android.bluetooth.BluetoothDevice;

public class GattResponse {
    public BluetoothDevice device;
    public int requestId;
    public int ReponseStatus;
    public int offset;
    public byte[] data;
}
