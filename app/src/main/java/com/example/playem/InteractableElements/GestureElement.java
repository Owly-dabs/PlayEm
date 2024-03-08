package com.example.playem.InteractableElements;

interface GestureElement {
    boolean isPressed(double touchX, double touchY);
    int[] handleActionDown(double touchX, double touchY, int pointerID);
    int[] handleActionMove(double touchX, double touchY, int pointerID);
    int[] handleActionUp(int pointerID);
    void update();

}
