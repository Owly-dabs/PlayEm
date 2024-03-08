package com.example.playem.InteractableElements;

abstract public class ControllerElement extends SpriteBaseClass implements GestureElement{
    public int elementID;
    public ControllerElement(int elementID, double CentreX, double CentreY, boolean ShouldRedraw) {
        super(CentreX, CentreY, ShouldRedraw);
        this.elementID = elementID;
    }
    public double distanceToCentreSquared(double touchX, double touchY){
        return Math.pow(CentreX-touchX, 2) + Math.pow(CentreY-touchY, 2);
    }
}
