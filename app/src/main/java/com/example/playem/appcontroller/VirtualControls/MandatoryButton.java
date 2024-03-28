package com.example.playem.appcontroller.VirtualControls;

import android.graphics.Color;

import com.example.playem.appcontroller.interfaces.BuildableViewCallbacks;

public class MandatoryButton extends SimpleButton{
    private final BuildableViewCallbacks buildViewCallback;
    public MandatoryButton(int idxX, int idxY, int width,int height,int pixelsPerStep, BuildableViewCallbacks buildableViewCallbacks){
        super(idxX, idxY,width<1?2:width,height<1?2:height,pixelsPerStep,-1);
        this.buildViewCallback = buildableViewCallbacks;
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
        buildViewCallback.ShowMenuOptions();
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
