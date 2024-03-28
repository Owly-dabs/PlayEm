package com.example.playem.bluetoothLE.blehandlers.interfaces;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;

import com.example.playem.pipes.HidBleDataPipe;
import java.util.TimerTask;

public interface BLETimedNotification {

    TimerTask onTimedNotifyCharacteristics(BluetoothGattServer gattServer, BluetoothDevice device, BluetoothGattCharacteristic characteristic, HidBleDataPipe dataqueue);
}
