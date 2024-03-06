package com.example.playem.ViewCallbacks;

import android.bluetooth.BluetoothDevice;

import com.example.playem.PlayEmGATTService;
import com.example.playem.viewmodels.GattServiceState;

import java.util.concurrent.ConcurrentLinkedQueue;

public interface GattServiceCallbacks {
    default Runnable onBondedDevicesChange(ConcurrentLinkedQueue<BluetoothDevice> bondedDevices){return null;}
    default Runnable onConnectionStateChanged(String d_address, String d_name, PlayEmGATTService.SERVICE_STATES state){return null;}
    default Runnable onAdvertisementStateChanged(PlayEmGATTService.SERVICE_STATES state){return null;}
    default Runnable onNotifierChanged(PlayEmGATTService.SERVICE_STATES state){return null;}
    //Internal operation callbacks are optional for UI;
    default Runnable onServicesAddComplete(PlayEmGATTService.SERVICE_STATES state) {return null;}
    default Runnable onServiceReady(PlayEmGATTService.SERVICE_STATES state){return null;}
    default Runnable onGattStatusChanged(GattServiceState.SERVICE_STATUS state){return null;};

    Runnable onServiceStatusChanged(GattServiceState newServiceState);



}

