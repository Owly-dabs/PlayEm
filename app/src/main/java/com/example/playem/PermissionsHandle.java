package com.example.playem;

public interface PermissionsHandle {
    public void NotGranted();
    public void Granted();
    public String Rationale();
    public String Permission = "PlayEm";
}
