package com.example.playem.InteractableElements;

abstract public class ControllerElement extends SpriteBaseClass implements GestureElement{
    public ControllerElement(double CentreX, double CentreY, boolean ShouldRedraw) {
        super(CentreX, CentreY, ShouldRedraw);
    }
    public double distanceToCentreSquared(double touchX, double touchY){
        return Math.pow(CentreX-touchX, 2) + Math.pow(CentreY-touchY, 2);
    }
}
