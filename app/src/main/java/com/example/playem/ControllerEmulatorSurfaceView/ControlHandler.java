package com.example.playem.ControllerEmulatorSurfaceView;

import android.view.MotionEvent;

import com.example.playem.hid.interfaces.ChunkType;

public interface ControlHandler {
    class ValuePack{
        public ChunkType type;
        public int x;
        public int y;
        public float relPixelX;
        public float relPixelY;
        public int id;
    }
    ValuePack[] onEnter(float x, float y, int pointerId);
    ValuePack[] onExit(float x, float y, int pointerId);
    ValuePack[] onMove(float x, float y, int pointerId);
    ValuePack[] onMotionEvent(MotionEvent motionEvent);
}
