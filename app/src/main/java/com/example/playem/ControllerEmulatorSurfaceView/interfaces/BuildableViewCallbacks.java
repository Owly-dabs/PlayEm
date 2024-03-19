package com.example.playem.ControllerEmulatorSurfaceView.interfaces;

public interface BuildableViewCallbacks {
    void HideComponentAdd();
    void ShowComponentAdd();
    void ShowToastError(String message,int duration);
    void ShowControlOptions();
    void HideControlOptions();
    void ShowMenuOptions();
    void HideMenuOptions();
    void ShowControlEditOptions();
    void HideControlEditOptions();
}
