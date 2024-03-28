package com.example.playem.bluetoothLE.utils;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.example.playem.generics.PermissionHandlerDelegate;
import com.example.playem.generics.PermissionsHandle;
import com.example.playem.generics.PermissionsHandler;

import java.util.ArrayList;
import java.util.List;

public class PermissionHandler {
    /*public PermissionHandler(Activity activity, String GroupName, PermissionsHandle[] Permissions_to_track, PermissionHandlerDelegate delegate) {
        super(GroupName, Permissions_to_track);
        this.activity = activity;
        this.delegate = delegate;
        tracking_permissions = Permissions_to_track;
    }
    private PermissionsHandle[] tracking_permissions;
    private Activity activity;
    private PermissionHandlerDelegate delegate;
    @Override
    public void RequestMissingPermissions(Activity context) {
        ArrayList<String> requires = new ArrayList<>();
        for(PermissionsHandle s: tracking_permissions)
        {
            if(activity.checkSelfPermission(s.getPermission())!= PackageManager.PERMISSION_GRANTED){
                requires.add(s.getPermission());
                Log.w("PERM","Requiring:"+s);
                delegate.RegisterRequests(s);
            }
        }
        if(!requires.isEmpty()){
            String[] requiredPerm = new String[requires.size()];
            requiredPerm= requires.toArray(requiredPerm);
            context.requestPermissions(requiredPerm,2); //Could use request code for a hashed event system from activity
            //this.requestPermissions(requiredPerm,11);
        }
    }*/
    public static void CheckPermissions(Activity activity){
        List<String> no_perms = new ArrayList<>();
        for(String perm : PermissionHandler.permissions_req) {
            if(activity.checkSelfPermission(perm)!=PackageManager.PERMISSION_GRANTED){
                no_perms.add(perm);
            }
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            if(activity.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!= PackageManager.PERMISSION_GRANTED) {
                no_perms.add(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
        if(!no_perms.isEmpty()){
            String[] req_perm = new String[no_perms.size()];
            int i = 0;
            for(String p:no_perms){
                req_perm[i] = p;
                i++;
            }
            activity.requestPermissions(req_perm,REQUEST_CODE);
        }else{
            PermissionHandler.Valid = true;
        }
    }
    public static boolean CheckOrRequestBtEnable(Activity activity, BluetoothAdapter btAdapter){
        if (!btAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                activity.startActivityForResult(enableBtIntent, REQUEST_BT_ENABLE);
                return false;
            }
        }
        return true;
    }
    private final static int REQUEST_CODE = 420;
    private final static int REQUEST_BT_ENABLE = 888;
    public static void checkOnPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        if(requestCode==PermissionHandler.REQUEST_CODE) {
            for (int i : grantResults) {
                if (i != PackageManager.PERMISSION_GRANTED) {
                    PermissionHandler.Valid = false;
                    return;
                }
            }
        }
        PermissionHandler.Valid=true;
    }
    public static boolean Valid = false;
    public final static String[] permissions_req = new String[]{
            android.Manifest.permission.BLUETOOTH_CONNECT,
            android.Manifest.permission.BLUETOOTH_SCAN,
            android.Manifest.permission.BLUETOOTH_ADVERTISE,
            android.Manifest.permission.BLUETOOTH_ADMIN,
            android.Manifest.permission.BLUETOOTH,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
    };
}