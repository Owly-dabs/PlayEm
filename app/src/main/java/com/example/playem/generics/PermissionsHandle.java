package com.example.playem.generics;

public interface PermissionsHandle {
    public void NotGranted();
    public void Granted();
    public String Rationale();
    public String toString();
    public String getPermission();
}
