package com.example.playem.bluetoothLE;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.playem.bluetoothLE.utils.PermissionHandle;
import com.example.playem.bluetoothLE.utils.PermissionHandler;
import com.example.playem.generics.PermissionHandlerDelegate;
import com.example.playem.generics.PermissionsHandle;

import java.util.Objects;

public class BluetoothPermissionsHandler implements PermissionHandlerDelegate {
    private Activity focusedActivity;

    public boolean CheckBTPermissions(){
        if(focusedActivity!=null){
            PermissionHandler permissionHandler = new PermissionHandler(focusedActivity,"", btPermissionsHandles,this);
            if(!permissionHandler.CheckAllPermissions(focusedActivity)) {
                permissionHandler.RequestMissingPermissions(focusedActivity);
            }
            return true;
        }
        return false;
    }
    private final PermissionsHandle[] btPermissionsHandles = new PermissionsHandle[]{
            new PermissionHandle(android.Manifest.permission.BLUETOOTH_CONNECT),
            new PermissionHandle(android.Manifest.permission.BLUETOOTH_SCAN),
            new PermissionHandle(android.Manifest.permission.BLUETOOTH_ADVERTISE),
            new PermissionHandle(android.Manifest.permission.ACCESS_COARSE_LOCATION),
            new PermissionHandle(Manifest.permission.ACCESS_FINE_LOCATION),
    };

    public void RegisterRequests(PermissionsHandle resolution) {
        if(!Objects.equals(resolution.getPermission(), "PlayEm")) {
            this.pending.put(resolution.getPermission(), resolution);
            if(!(focusedActivity.isDestroyed()||focusedActivity.isFinishing())){
                if (focusedActivity.shouldShowRequestPermissionRationale(resolution.getPermission())) {
                    Log.w("PERM", resolution.Rationale()); //This should be the hook to an activity to show message
                }
            }
        }
    }

    public boolean onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        //focusedActivity.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for(int i =0;i<permissions.length;i++){
            PermissionsHandle ph =this.pending.get(permissions[i]);
            if(ph==null){
                Log.e("PERM","Empty PermissionsHandle Object");
                continue;
            }
            if(grantResults[i]== PackageManager.PERMISSION_GRANTED)
                ph.Granted();
            else
                ph.NotGranted();

            DeregisterRequests(ph.getPermission());
        }
        return pending.isEmpty();
    }

    @Override
    public void DeregisterRequests(String permissions) {
        this.pending.remove(permissions);
    }
    @Override
    public void SetFocusedActivity(Activity activity) {
        focusedActivity = activity;
    }


}
