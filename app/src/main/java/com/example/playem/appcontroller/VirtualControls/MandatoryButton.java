package com.example.playem.appcontroller.VirtualControls;

import android.graphics.Color;

import com.example.playem.appcontroller.interfaces.BuildableViewCallbacks;

public class MandatoryButton extends SimpleButton{
    public MandatoryButton(){
        super();
    }
    private BuildableViewCallbacks buildviewCallback;
    public MandatoryButton(int idxX, int idxY, int pixelsPerStep, BuildableViewCallbacks buildableViewCallbacks){
        super(idxX, idxY,pixelsPerStep,-1);
        this.buildviewCallback = buildableViewCallbacks;
        this.widthSteps = 2;
        this.heightSteps = 2;
        outerRingPobj.setColor(Color.WHITE);
        innerPobj.setColor(Color.WHITE);
        innerPressedPobj.setColor(Color.GRAY);
        innerPressedPobj.setAlpha(70);
        this.handler = this;
    }
    @Override
    public VirtualControlTemplates GetVirtualControlType(){
        return VirtualControlTemplates.MANDATORY;
    }
    @Override
    public ValuePack[] onEnter(float x, float y, int pointerId) {
        lastValuePack[0].x = 1;
        buildviewCallback.ShowMenuOptions();
        return this.lastValuePack;
    }

    @Override
    public ValuePack[] onExit(float x, float y, int pointerId) {
        lastValuePack[0].x=0;
        return lastValuePack;
    }

    @Override
    public ValuePack[] onMove(float x, float y, int pointerId) {
        lastValuePack[0].x = 1;
        return lastValuePack;
    }
}
