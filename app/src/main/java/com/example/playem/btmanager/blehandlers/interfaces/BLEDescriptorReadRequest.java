package com.example.playem.btmanager.blehandlers.interfaces;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;

public interface BLEDescriptorReadRequest {
    public Runnable onDescriptorReadRequest(BluetoothGattServer server, BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor);
}
