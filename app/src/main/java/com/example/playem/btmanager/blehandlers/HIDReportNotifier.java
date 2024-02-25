package com.example.playem.btmanager.blehandlers;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;

import com.example.playem.btmanager.blehandlers.interfaces.BLETimedNotification;
import com.example.playem.btmanager.blehandlers.interfaces.ConcurrentTransferQueue;

import java.util.TimerTask;

import android.os.Build;
import android.os.Handler;
public class HIDReportNotifier implements BLETimedNotification {
    @Override
    public TimerTask onTimedNotifyCharacteristics(Handler handler, BluetoothGattServer gattServer, BluetoothDevice device, BluetoothGattCharacteristic characteristic, ConcurrentTransferQueue dataqueue) {
        return new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void run() {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            gattServer.notifyCharacteristicChanged(device,characteristic,false, dataqueue.dequeue());
                        }else{
                            characteristic.setValue(dataqueue.dequeue());
                            gattServer.notifyCharacteristicChanged(device,characteristic,false);
                        }
                    }
                });
            }
        };
    }
}
