package com.example.playem;

import androidx.appcompat.app.AppCompatActivity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import com.example.playem.btmanager.PlayEmBTManager;

public class MainActivity extends AppCompatActivity implements PermissionHandlerDelegate {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PlayEmBTManager mBTManager = new PlayEmBTManager(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                              int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for(int i =0;i<permissions.length;i++){
            PermissionsHandle ph =this.pending.get(permissions[i]);
            if(ph==null){
                Log.e("PERM","Empty PermissionsHandle Object");
            }
            if(grantResults[i]== PackageManager.PERMISSION_GRANTED){
                ph.Granted();
            }
            else{
                ph.NotGranted();
            }
            DeregisterRequests(ph.getPermission());
        }
    }
    @Override
    public void RegisterRequests(PermissionsHandle resolution) {
        if(resolution.getPermission()!="PlayEm") {
            this.pending.put(resolution.getPermission(), resolution);
            //Put Stuff here to block UI call if needed
            if (this.shouldShowRequestPermissionRationale(resolution.getPermission())) {
                Log.w("PERM", resolution.Rationale()); //This should be the hook to an activity to show message
            }
        }
    }
    @Override
    public void DeregisterRequests(String permissions) {
        this.pending.remove(permissions);
    }
}

