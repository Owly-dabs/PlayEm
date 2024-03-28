package com.example.playem.appcontroller.interfaces;

import android.util.Log;

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
    void HideAllOptions();
    void ShowAllOptions();
}
