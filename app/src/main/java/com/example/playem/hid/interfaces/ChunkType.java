package com.example.playem.hid.interfaces;

public enum ChunkType {
    AXES((byte)0),
    BUTTONS((byte)1);
    public final byte b;

    ChunkType(byte b) {
        this.b = b;
    }
}
