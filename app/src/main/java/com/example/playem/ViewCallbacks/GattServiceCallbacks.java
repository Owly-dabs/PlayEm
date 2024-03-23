package com.example.playem.ViewCallbacks;

import android.bluetooth.BluetoothDevice;

import com.example.playem.AppGattService;
import com.example.playem.viewmodels.GattServiceState;

import java.util.concurrent.ConcurrentLinkedQueue;

public interface GattServiceCallbacks {
    default Runnable onBondedDevicesChange(ConcurrentLinkedQueue<BluetoothDevice> bondedDevices){return null;}
    default Runnable onConnectionStateChanged(String d_address, String d_name, AppGattService.SERVICE_STATES state){return null;}
    default Runnable onAdvertisementStateChanged(AppGattService.SERVICE_STATES state){return null;}
    default Runnable onNotifierChanged(AppGattService.SERVICE_STATES state){return null;}
    default Runnable onServicesAddComplete(AppGattService.SERVICE_STATES state) {return null;}
    default Runnable onServiceReady(AppGattService.SERVICE_STATES state){return null;}
    default Runnable onGattStatusChanged(GattServiceState.SERVICE_STATUS state){return null;};

    Runnable onServiceStatusChanged(GattServiceState newServiceState);



}

