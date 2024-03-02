package com.example.playem.btmanager;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import com.example.playem.hid.*;

public class GattServerCbFactory {
    public static BluetoothGattServerCallback CreateGATTServerCallback(PlayEmBTManager btManager){
        BluetoothGattServerCallback btgattcb = new BluetoothGattServerCallback() {

        };
        return btgattcb;
    }
}
