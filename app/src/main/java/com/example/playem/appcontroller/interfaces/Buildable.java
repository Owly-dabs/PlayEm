package com.example.playem.appcontroller.interfaces;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.example.playem.appcontroller.ControlComponent;
import com.example.playem.appcontroller.VirtualControls.VirtualControlTemplates;
import com.example.playem.hid.interfaces.ChunkType;
import com.example.playem.pipes.InputPipeCallbacks;

public interface Buildable extends InputPipeCallbacks {
    void Resize(int step);
    void MoveAndUpdateDrawSpace(int datumX, int datumY);
    void NewBuildState(boolean newState);
    void DrawColliderBox(Canvas screen, Paint colliderColor,int stroke_width);
    ControlComponent GetComponent();
    ChunkType GetChunkType();
    VirtualControlTemplates GetVirtualControlType();
}
