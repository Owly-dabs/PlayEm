package com.example.playem.appcontroller.interfaces;

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
