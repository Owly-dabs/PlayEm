package com.example.playem.bluetoothLE.utils;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.List;

public class BondStateBroadcastReceiver extends BroadcastReceiver {
    private final BluetoothGattServer gattServer;
    public BondStateBroadcastReceiver(BluetoothGattServer gattServer){
        this.gattServer = gattServer;
    }
    @Override
    @SuppressLint("MissingPermission")
    public void onReceive(Context context, Intent intent) {
        int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE,BluetoothDevice.ERROR);
        //int pstate = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE,BluetoothDevice.ERROR);
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
        //Log.i("BONDREC",String.format("Bond receive received for %s : %d",device.getName(),state));
        if(state == BluetoothDevice.BOND_BONDED){

            BluetoothManager bm =  (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            List<BluetoothDevice> deviceList = bm.getDevicesMatchingConnectionStates(BluetoothProfile.GATT,new int[]{BluetoothProfile.STATE_CONNECTED,BluetoothProfile.STATE_CONNECTING});
            synchronized (gattServer){
                if(deviceList == null)
                    gattServer.connect(device,true);
                else if (!deviceList.contains(device))
                    gattServer.connect(device,true);
            }
            context.unregisterReceiver(this);
        }
    }
}
