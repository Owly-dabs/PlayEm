package com.example.playem.InteractableElements;

//import android.graphics.Bitmap;
import android.graphics.Canvas;


public abstract class SpriteBaseClass {
    protected double CentreX;
    protected double CentreY;
    public boolean ShouldRedraw;

    //add in bitmap for sprite image later


    public SpriteBaseClass(double CentreX, double CentreY, boolean ShouldRedraw) {
        // (X, Y) coordinate of sprite
        this.CentreX = CentreX;
        this.CentreY = CentreY;
        this.ShouldRedraw = ShouldRedraw;
    }

    public double distanceToCentreSquared(double touchX, double touchY){
        return Math.pow(CentreX-touchX, 2) + Math.pow(CentreY-touchY, 2);
    }


    protected void draw(Canvas canvas) {}

}
