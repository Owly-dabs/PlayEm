package com.example.playem.btmanager;

import android.Manifest;
import android.bluetooth.*;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.playem.MainActivity;
import com.example.playem.PermissionHandlerDelegate;
import com.example.playem.PermissionsHandle;

import java.util.ArrayList;

public class PlayEmBTManager {

    @RequiresApi(api = Build.VERSION_CODES.S)
    public PlayEmBTManager(MainActivity context) {
        this.parentActivity = context;
        checkHardware();
        checkBTAdapterPermissions();
        checkBTEnabled();
    }

    private void checkBTEnabled() {
        if (hasHW && !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(parentActivity, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                parentActivity.startActivityForResult(enableBtIntent, -1);
            }else{
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    checkBTAdapterPermissions();
                }
            }
        }
    }
    private void checkHardware() {
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        hasHW = bluetoothAdapter != null;
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private void checkBTAdapterPermissions() {
        if (hasHW) {
            final String[] rt_permissions = {
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_ADVERTISE,
            };
            ArrayList<String> requires = new ArrayList<>();
            for(String s:rt_permissions)
            {
                if(ActivityCompat.checkSelfPermission(parentActivity, s)!= PackageManager.PERMISSION_GRANTED){
                    requires.add(s);
                    Log.w("PERM","Requiring:"+s);
                    ((PermissionHandlerDelegate)parentActivity).RegisterRequests(new BTPermissionHandle(s));
                }
            }
            if(requires.size()>0){
                String[] requiredPerm = new String[requires.size()];
                requiredPerm= requires.toArray(requiredPerm);
                ActivityCompat.requestPermissions(parentActivity, requiredPerm,2);
            }
        }
    }
    private BluetoothAdapter bluetoothAdapter;
    private AppCompatActivity parentActivity;
    private boolean hasHW = false;
    protected class BTPermissionHandle implements PermissionsHandle{
        protected BTPermissionHandle(String permission){
            Permission = permission;
        }
        public String Permission;
        @Override
        public void NotGranted() {
            Log.e("PERM",Permission +" Not Granted!");
        }
        @Override
        public void Granted() {

        }
        @Override
        public String Rationale() {
            return null;
        }
    }
}

