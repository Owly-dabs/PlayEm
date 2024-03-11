package com.example.playem.pipes;

import android.util.Log;

import com.example.playem.btmanager.blehandlers.interfaces.ConcurrentTransferBuffer;
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
            tsize += bChunk != null ? bChunk.size : 0;
            ButtonsData.add(bChunk); //Initialized to 0x00
        }
        if(chunks.containsKey(HIDChunk.CalHash(ChunkType.AXES,0))){
            aChunk = chunks.get(HIDChunk.CalHash(ChunkType.AXES,0));
            tsize += aChunk != null ? aChunk.size : 0;
            AxesData.add(aChunk); //Initialized to 0x00
        }
        CurrentTruth.add((new byte[tsize]));
    }
    // Button No. is 0 indexed
    public void UpdateButtonNumber(int bNo, boolean activeTrue){
        //Log.i("PIPE",String.format("Update Button called %d %s",bNo,activeTrue?"true":false));
        int byteIndex = bNo/8; //Buttons are always start at 0
        synchronized (this){
            byte[] bArray = CurrentTruth.get(0);
            if(activeTrue){
                bArray[byteIndex] = (byte) (bArray[byteIndex]|(1<<bNo));
            }else{
                bArray[byteIndex] = (byte) ~((~bArray[byteIndex])|1<<bNo);
            }
            //signalDirty = true;
            //Log.i("PIPE",String.format("Update Button called %d %8s",bNo,Integer.toBinaryString(CurrentTruth.get(0)[byteIndex]).replace(" ","0")));
        }
    }

    //Axes No are 0 indexed
    public void UpdateAxis(int aNo, int value){
        //value = value<0? (value + 0x0FFFF): value; take raw
        synchronized (this) {
            HIDChunk axesData = AxesData.get(0);
            if (aNo >= axesData.size / 2) {
                Log.e("PIPE", "Invalid Axis Number Called");
                return;
            }
            byte[] aArray = CurrentTruth.get(0);
            aArray[aNo*2+axesData.bIndex] = (byte)(value); //LSB first
            aArray[aNo*2+axesData.bIndex+1] = (byte)(value>>8);
            //signalDirty = true;
        }
    }

    public void PushFrame(){
            byte[] newFrame = new byte[tsize];
            System.arraycopy(CurrentTruth.get(0),0,newFrame,0,tsize);
            dataPipeRef.enqueue(newFrame);
    }
    public byte[] GetReport(){
        //Log.i("PIPE","GetReport Called");
            byte[] newFrame = new byte[tsize];//+1];
            //newFrame[0] = 1;
            System.arraycopy(CurrentTruth.get(0),0,newFrame,0,tsize);
            return newFrame;
    }
    public void NotifyDataReady(){
        signalDirty = true;
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
    public final ConcurrentTransferBuffer dataPipeRef = new ConcurrentTransferBuffer();
}
