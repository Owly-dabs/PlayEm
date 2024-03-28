package com.example.playem.appsettings;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import com.example.playem.appcontroller.ControlComponent;
import com.example.playem.appcontroller.interfaces.Buildable;

import java.util.Objects;

public class ControlsData {
    public ControlsData(int idxX,int idxY,int widthSteps,int heightSteps,int pixelsPerStep,int pipeId,String virtualControlType,String extras){
        this.idxX = idxX;
        this.idxY = idxY;
        this.pixelsPerStep = pixelsPerStep;
        this.heightSteps = heightSteps;
        this.widthSteps = widthSteps;
        this.virtualControlType = virtualControlType;
        this.extras = extras;
        this.pipeId = pipeId;
    }
    public ControlsData(Buildable buildable){
        ControlComponent cc = buildable.GetComponent();
        this.idxX = cc.positionX;
        this.idxY = cc.positionY;
        this.pixelsPerStep = cc.pixelsPerStep;
        this.pipeId = cc.pipeID;
        this.widthSteps = cc.widthSteps;
        this.heightSteps = cc.heightSteps;
        this.virtualControlType = buildable.GetVirtualControlType().toString();
        this.extras="Empty;";
    }
    public int idxX;
    public int idxY;
    public int widthSteps;
    public int heightSteps;
    public int pixelsPerStep;
    public int pipeId;
    public String virtualControlType;
    public String extras;
    @NonNull
    @SuppressLint("DefaultLocale")
    @Override
    public String toString(){
        return String.format("%s px: %d py: %d pid:%d extras:%b",virtualControlType,idxX,idxY,pipeId,(!Objects.equals(extras, "Empty;")));
    }
}
