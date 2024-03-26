package com.example.playem.appsettings;

import com.example.playem.ControllerActivity;
import com.example.playem.appcontroller.ControlComponent;
import com.example.playem.appcontroller.interfaces.Buildable;
import com.example.playem.hid.interfaces.ChunkType;

public class ControlsData {
    public ControlsData(int idxX,int idxY,int pixelsPerStep,int pipeId,String chunkType,String extras){
        this.idxX = idxX;
        this.idxY = idxY;
        this.pixelsPerStep = pixelsPerStep;
        this.chunkType = chunkType;
        this.extras = extras;
        this.pipeId = pipeId;
    }
    public ControlsData(Buildable buildable){
        ControlComponent cc = buildable.GetComponent();
        this.idxX = cc.positionX;
        this.idxY = cc.positionY;
        this.pixelsPerStep = cc.pixelsPerStep;
        this.pipeId = cc.pipeID;
        this.chunkType = buildable.GetChunkType().toString();
        this.extras="Empty;";
    }
    int idxX;
    int idxY;
    int pixelsPerStep;
    int pipeId;
    String chunkType;
    String extras;
}
