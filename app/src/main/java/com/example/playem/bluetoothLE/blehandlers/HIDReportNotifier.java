package com.example.playem.bluetoothLE.blehandlers;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.os.Build;

import com.example.playem.bluetoothLE.blehandlers.interfaces.BLETimedNotification;
import com.example.playem.pipes.HidBleDataPipe;

import java.util.TimerTask;

public class HIDReportNotifier implements BLETimedNotification {
    @Override
    public TimerTask onTimedNotifyCharacteristics(BluetoothGattServer gattServer, BluetoothDevice device, BluetoothGattCharacteristic characteristic, HidBleDataPipe dataqueue) {
        return new TimerTask() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void run() {
                if(dataqueue.signalDirty){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        byte[] report = dataqueue.GetReport();
                        //Log.i("NOTIFY",String.format("Device: %s -- %s",device.getAddress(), HIDUtils.bytesToHex(report)));
                        gattServer.notifyCharacteristicChanged(device, characteristic, false, report);
                    } else {
                        characteristic.setValue(dataqueue.GetReport());
                        gattServer.notifyCharacteristicChanged(device, characteristic, false);
                    }
                }
                }
            };
        }
    }
