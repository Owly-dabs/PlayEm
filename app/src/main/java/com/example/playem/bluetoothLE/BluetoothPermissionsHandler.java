package com.example.playem.bluetoothLE;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.NonNull;
import com.example.playem.generics.PermissionHandlerDelegate;
import com.example.playem.generics.PermissionsHandle;
import com.example.playem.generics.PermissionsHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class BluetoothPermissionsHandler implements PermissionHandlerDelegate {
    private Activity focusedActivity;

    public boolean CheckBTPermissions(){
        if(focusedActivity!=null){
            BTPermissionHandler permissionHandler = new BTPermissionHandler("PlayEm BT Manager", btPermissionsHandles);
            if(!permissionHandler.CheckAllPermissions(focusedActivity)) {
                permissionHandler.RequestMissingPermissions(focusedActivity);
            }
            return true;
        }
        return false;
    }
    private static final PermissionsHandle[] btPermissionsHandles = new PermissionsHandle[]{
            new BTPermissionHandle(android.Manifest.permission.BLUETOOTH_CONNECT),
            new BTPermissionHandle(android.Manifest.permission.BLUETOOTH_SCAN),
            new BTPermissionHandle(android.Manifest.permission.BLUETOOTH_ADVERTISE),
            new BTPermissionHandle(android.Manifest.permission.ACCESS_COARSE_LOCATION),
            new BTPermissionHandle(Manifest.permission.ACCESS_FINE_LOCATION),
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
    protected  class BTPermissionHandler extends PermissionsHandler {
        public BTPermissionHandler(String GroupName, PermissionsHandle[] Permissions_to_track) {
            super(GroupName, Permissions_to_track);
        }
        @Override
        protected void RequestMissingPermissions(Activity context) {
            ArrayList<String> requires = new ArrayList<>();
            for(PermissionsHandle s: btPermissionsHandles)
            {
                if(focusedActivity.checkSelfPermission(s.getPermission())!= PackageManager.PERMISSION_GRANTED){
                    requires.add(s.getPermission());
                    Log.w("PERM","Requiring:"+s);
                    RegisterRequests(s);
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
    protected static class BTPermissionHandle implements PermissionsHandle{
        protected BTPermissionHandle(String permission){
            Permission = permission;
        }
        protected String Permission;
        @Override
        public void NotGranted() {
            Log.e("PERM",Permission +" Not Granted!");
        }
        @Override
        public void Granted() {

        }
        @Override
        public String Rationale() {
            return "null";
        }
        @NonNull
        @Override
        public String toString(){
            return new String(Arrays.copyOf(Permission.toCharArray(),Permission.length()));
        }
        public String getPermission(){
            return this.toString();
        }
    }
}
