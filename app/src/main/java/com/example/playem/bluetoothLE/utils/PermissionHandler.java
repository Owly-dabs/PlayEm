package com.example.playem.bluetoothLE.utils;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.util.Log;

import com.example.playem.generics.PermissionHandlerDelegate;
import com.example.playem.generics.PermissionsHandle;
import com.example.playem.generics.PermissionsHandler;

import java.util.ArrayList;

public class PermissionHandler extends PermissionsHandler {
    public PermissionHandler(Activity activity, String GroupName, PermissionsHandle[] Permissions_to_track, PermissionHandlerDelegate delegate) {
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
    }
}