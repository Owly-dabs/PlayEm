package com.example.playem.hid.interfaces;

public class HIDChunk{
    public int bIndex;
    public int size;
    public int reportID;
    public ChunkType Type;
    public int GetHash(){
        return CalHash(Type,reportID);
    }

    public static int CalHash(ChunkType type,int reportID){
        return (int)type.b+(reportID<<8);
    }
}
