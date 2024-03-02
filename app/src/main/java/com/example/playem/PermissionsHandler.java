package com.example.playem;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public abstract class PermissionsHandler {
    public PermissionsHandler(String GroupName, PermissionsHandle[] Permissions_to_track){
        PermissionsGroup = GroupName;
        Permissions = Permissions_to_track;
    }
    public String PermissionsGroup;
    private PermissionsHandle[] Permissions;
    private int permissionsmask = 0;

    public boolean CheckAllPermissions(AppCompatActivity context){
        int m = 0;
        int mask = 0;
        boolean clean = true;
        for(PermissionsHandle p:Permissions){
            if(context.checkSelfPermission(p.toString())!= PackageManager.PERMISSION_GRANTED){
                mask|=(1<<m);
                Log.e("PERM", String.format("%s Not Granted",p));
                clean = false;
            }
            m++;
        }
        permissionsmask = mask;
        return clean;
    }
    protected abstract void RequestMissingPermissions(AppCompatActivity context);

}
