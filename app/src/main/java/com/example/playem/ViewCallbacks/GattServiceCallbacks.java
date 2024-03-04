package com.example.playem.ViewCallbacks;

import android.bluetooth.BluetoothDevice;

import com.example.playem.PlayEmGATTService;

import java.util.concurrent.ConcurrentLinkedQueue;

public interface GattServiceCallbacks {
    Runnable onBondedDevicesChange(ConcurrentLinkedQueue<BluetoothDevice> bondedDevices);
    Runnable onConnectionStateChanged(String d_address, String d_name, PlayEmGATTService.SERVICE_STATES state);
    Runnable onAdvertisementStateChanged(PlayEmGATTService.SERVICE_STATES state);
    Runnable onNotifierChanged(PlayEmGATTService.SERVICE_STATES state);
    Runnable onServicesAddComplete(PlayEmGATTService.SERVICE_STATES state);


}

