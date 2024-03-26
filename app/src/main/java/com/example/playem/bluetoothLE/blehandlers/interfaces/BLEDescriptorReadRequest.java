package com.example.playem.bluetoothLE.blehandlers.interfaces;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;

public interface BLEDescriptorReadRequest {
    public Runnable onDescriptorReadRequest(BluetoothGattServer server, BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor);
}
