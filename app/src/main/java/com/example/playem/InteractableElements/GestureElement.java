package com.example.playem.InteractableElements;

interface GestureElement {
    boolean isPressed(double touchX, double touchY);
    void handleActionDown(double touchX, double touchY, int pointerID);
    void handleActionMove(double touchX, double touchY, int pointerID);
    void handleActionUp(int pointerID);
    void update();

}
