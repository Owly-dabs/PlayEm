package com.example.playem.bluetoothLE.utils;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.playem.generics.PermissionsHandle;

import java.util.Arrays;

public class PermissionHandle implements PermissionsHandle {
    public PermissionHandle(String permission){
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