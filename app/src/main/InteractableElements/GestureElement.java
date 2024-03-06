package com.example.playem.InteractableElements;

interface GestureElement {
    boolean isPressed(double touchX, double touchY);
    void handleActionDown(double touchX, double touchY);
    void handleActionMove(double touchX, double touchY);
    void handleActionUp();
    void update();

}
