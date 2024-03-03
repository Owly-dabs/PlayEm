package com.example.playem.pipes;

import android.util.Log;

import com.example.playem.btmanager.blehandlers.interfaces.ConcurrentTransferQueue;
import com.example.playem.hid.interfaces.ChunkType;
import com.example.playem.hid.interfaces.HIDChunk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
//Always takes abs vals from calls assumes last input always
//This class acts as the de-coupler between GUI and BLE Stack
//TODO Refactor for multi virtual device
public class PlayEmDataPipe {
    public PlayEmDataPipe(HashMap<Integer,HIDChunk> chunks){
        //Assumes only report id 0 is active and not report id is not sent to host
        //Implements by copy full byte arrays
        HIDChunk bChunk;
        HIDChunk aChunk;
        if(chunks.containsKey(HIDChunk.CalHash(ChunkType.BUTTONS,0))){
            bChunk = chunks.get(HIDChunk.CalHash(ChunkType.BUTTONS,0));
            tsize += bChunk.size;
            ButtonsData.add(bChunk); //Initialized to 0x00
        }
        if(chunks.containsKey(HIDChunk.CalHash(ChunkType.AXES,0))){
            aChunk = chunks.get(HIDChunk.CalHash(ChunkType.AXES,0));
            tsize += aChunk.size;
            ButtonsData.add(aChunk); //Initialized to 0x00
        }
        CurrentTruth.add((new byte[tsize]));
    }
    // Button No. is 0 indexed
    public void UpdateButtonNumber(int bNo, byte activeTrue){
        Log.i("PIPE",String.format("Update Button called %d %d",bNo,activeTrue));
        int byteIndex = bNo/8; //Buttons are always start at 0
        activeTrue = activeTrue>0x01?0x01:activeTrue;
        synchronized (this){
            byte[] bArray = CurrentTruth.get(0);
            if(activeTrue>0){
                bArray[byteIndex] = (byte) (bArray[byteIndex]|(activeTrue<<bNo));
            }else{
                bArray[byteIndex] = (byte) ((bArray[byteIndex]|((1<<bNo)-1))&activeTrue<<bNo);
            }
            signalDirty = true;
            //Log.i("PIPE",String.format("Update Button called %d %8s",bNo,Integer.toBinaryString(CurrentTruth.get(0)[byteIndex]).replace(" ","0")));
        }
    }

    //Axes No are 0 indexed
    public void UpdateAxis(int aNo, int value, boolean isNeg){
        value = isNeg? (value + 0xFFFF): value;
        synchronized (this) {
            HIDChunk axesData = AxesData.get(0);
            if (aNo >= axesData.size / 2) {
                Log.e("PIPE", "Invalid Axis Number Called");
                return;
            }
            byte[] aArray = CurrentTruth.get(0);
            aArray[aNo*2+axesData.bIndex] = (byte)(value); //LSB first
            aArray[aNo*2+axesData.bIndex+1] = (byte)(value>>8);
            signalDirty = true;
        }
    }

    public void PushFrame(){
        synchronized (this) {
            byte[] newFrame = new byte[tsize];
            System.arraycopy(CurrentTruth.get(0),0,newFrame,0,tsize);
            dataPipeRef.enqueue(newFrame);
        }
    }
    public byte[] GetReport(){
        //Log.i("PIPE","GetReport Called");
        synchronized (this){
            byte[] newFrame = new byte[tsize];//+1];
            //newFrame[0] = 1;
            System.arraycopy(CurrentTruth.get(0),0,newFrame,0,tsize);
            return newFrame;
        }
    }

    public void NotifyComplete(){
        signalDirty = false;
    }
    public boolean signalDirty = false;
    public int getTsize(){
        return tsize;
    }
    int tsize = 0;
    final List<byte[]> CurrentTruth = new ArrayList<>();
    final List<HIDChunk> ButtonsData = new ArrayList<>();
    final List<HIDChunk> AxesData = new ArrayList<>();
    public final ConcurrentTransferQueue dataPipeRef = new ConcurrentTransferQueue();
}
