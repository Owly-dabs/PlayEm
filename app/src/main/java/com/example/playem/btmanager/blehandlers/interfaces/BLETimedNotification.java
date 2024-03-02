package com.example.playem.btmanager.blehandlers.interfaces;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.os.Handler;

import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

public interface BLETimedNotification {

    TimerTask onTimedNotifyCharacteristics(Handler handler,BluetoothGattServer gattServer, BluetoothDevice device, BluetoothGattCharacteristic characteristic,ConcurrentTransferQueue dataqueue);
}
