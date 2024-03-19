package com.example.playem.ControllerEmulatorSurfaceView.interfaces;

import com.example.playem.ControllerEmulatorSurfaceView.ControlComponent;
import com.example.playem.hid.interfaces.ChunkType;
import com.example.playem.pipes.InputPipeCallbacks;

public interface Buildable extends InputPipeCallbacks {
    void Resize(int step);
    void MoveAndUpdateDrawSpace(int datumX, int datumY);
    void NewBuildState(boolean newState);
    ControlComponent GetComponent();
    ChunkType GetChunkType();
}
