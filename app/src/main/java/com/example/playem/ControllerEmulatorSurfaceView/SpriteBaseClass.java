package com.example.playem.InteractableElements;

//import android.graphics.Bitmap;
import android.graphics.Canvas;


public abstract class SpriteBaseClass {
    public double CentreX;
    public double CentreY;

    public double radius;
    public boolean ShouldRedraw;

    //add in bitmap for sprite image later

    public SpriteBaseClass(){}
    public SpriteBaseClass(double CentreX, double CentreY, boolean ShouldRedraw) {
        // (X, Y) coordinate of sprite
        this.CentreX = CentreX;
        this.CentreY = CentreY;
        this.ShouldRedraw = ShouldRedraw;
    }


    public void draw(Canvas canvas) {}

}
