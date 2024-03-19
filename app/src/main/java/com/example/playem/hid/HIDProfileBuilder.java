package com.example.playem.hid;

import android.util.Log;

import com.example.playem.hid.interfaces.ChunkType;
import com.example.playem.hid.interfaces.HIDChunk;
import com.example.playem.hid.usagepages.USAGE_BUTTON;
import com.example.playem.hid.usagepages.USAGE_DESKTOP_GENERIC;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Stack;
//TODO Implement Report ID for multi virtual devices
public class HIDProfileBuilder {
//Stacks
    public HIDProfileBuilder(){
        Top = new ArrayList<>();
        Bottom = new Stack<>();
    }
    private final List<Byte> Top;
    private final Stack<Byte> Bottom;
    private boolean isBuilt = false;
    private int axes_count = 0;
    public void Build(){
        if(isBuilt)
            return;
        AddDesktopGenericUsagePage();
        CollectionStart(HIDDescriptor.COLLECTIONTYPES.APPLICATION);
        CollectionStart(HIDDescriptor.COLLECTIONTYPES.LOGICAL);
        //Top.add((byte)0x85);
        //Top.add((byte)0x01);

        AddButtons(8, (byte) 0x02);
        AddAxes(5,(byte)2);
        CollectionEnd();
        CollectionEnd();
        isBuilt = true;
    }
      /*  Input 1000 00 nn Bit 0
        {Data (0) | Constant (1)}                       1
        {Array (0) | Variable (1)}                      2
        {Absolute (0) | Relative (1)}                   3
        {No Wrap (0) | Wrap (1)}                        4
        {Linear (0) | Non Linear (1)}                   5
        {Preferred State (0) | No Preferred (1)}        6
        {No Null position (0) | Nullstate(1)}           7
        Reserved (0)
        {Bit Field (0) | Buffered Bytes (1)}
        Reserved (0)*/
    public void StartJoyStick(){
        AddDesktopGenericUsagePage();
        CollectionStart(HIDDescriptor.COLLECTIONTYPES.APPLICATION);
        CollectionStart(HIDDescriptor.COLLECTIONTYPES.LOGICAL);
    }
    public void EndJoyStick(){
        CollectionEnd();
        CollectionEnd();
        isBuilt = true;
    }
    public void AddAxes(int nAxes, byte inputParams) {
        if(nAxes==0)
            return;
        if(nAxes>9){
            throw new RuntimeException("Too Many Axes");
        }

        Top.add(HIDDescriptor.DESCRIPTORIDS.USAGE_PAGE);
        Top.add(USAGE_DESKTOP_GENERIC.PAGE);
        //Top.add(HIDDescriptor.DESCRIPTORIDS.USAGE);
        //Top.add(USAGE_DESKTOP_GENERIC.POINTER);
        //CollectionStart(HIDDescriptor.COLLECTIONTYPES.PHYSICAL);
        for(int i = 0;i<nAxes;i++) {
            Top.add(HIDDescriptor.DESCRIPTORIDS.USAGE);
            Top.add((byte) (USAGE_DESKTOP_GENERIC.X + (byte) axes_count));
            axes_count++;
        }
        Top.add(HIDDescriptor.DESCRIPTORIDS.LOGIC_MIN);
        Top.add((byte) 0x00);
        //Default 16bits LittleEndian

        Top.add((byte) (HIDDescriptor.DESCRIPTORIDS.LOGIC_MAX + 2));
        Top.add((byte) 0xFF);
        Top.add((byte) 0xFF); //Default 16bits LittleEndian
        Top.add((byte) 0x00); //Default 16bits LittleEndian
        Top.add((byte) 0x00);

        Top.add(HIDDescriptor.DESCRIPTORIDS.REPORT_SIZE);
        Top.add((byte) (0x02*8));
        Top.add(HIDDescriptor.DESCRIPTORIDS.REPORT_COUNT);
        Top.add((byte) nAxes);

        Top.add(HIDDescriptor.DESCRIPTORIDS.INPUT);
        Top.add(inputParams);
        //CollectionEnd();
        HIDChunk bChunk = new HIDChunk();
        bChunk.Type = ChunkType.AXES;
        bChunk.reportID = 0;
        int buttonChunkKey = HIDChunk.CalHash(ChunkType.BUTTONS,bChunk.reportID);
        if(Chunks.containsKey(buttonChunkKey)){
            bChunk.bIndex = Objects.requireNonNull(Chunks.get(buttonChunkKey)).size;
        }
        else{
            bChunk.bIndex = 0;
        }
        bChunk.size = nAxes*2; //Axes are always byte aligned
        Chunks.put(bChunk.GetHash(),bChunk);

    }

    public void AddButtons(int nButtons,byte inputParams){
        if(nButtons==0)
            return;
        if(nButtons>32){
            throw new RuntimeException("Too Many Buttons");
        }
        Top.add(HIDDescriptor.DESCRIPTORIDS.USAGE_PAGE);
        Top.add(USAGE_BUTTON.PAGE);
        Top.add(HIDDescriptor.DESCRIPTORIDS.USAGE_MIN);
        Top.add((byte)0x01);
        Top.add(HIDDescriptor.DESCRIPTORIDS.USAGE_MAX);
        Top.add((byte)nButtons);
        Top.add(HIDDescriptor.DESCRIPTORIDS.LOGIC_MIN);
        Top.add((byte)0x00);
        Top.add(HIDDescriptor.DESCRIPTORIDS.LOGIC_MAX);
        Top.add((byte)0x01);
        /*Top.add(HIDDescriptor.DESCRIPTORIDS.PHY_MIN);
        Top.add((byte)0x00);
        Top.add(HIDDescriptor.DESCRIPTORIDS.PHY_MAX);
        Top.add((byte)0x01);*/
        Top.add(HIDDescriptor.DESCRIPTORIDS.REPORT_COUNT);
        Top.add((byte)nButtons);
        Top.add(HIDDescriptor.DESCRIPTORIDS.REPORT_SIZE);
        Top.add((byte)0x01);
        Top.add(HIDDescriptor.DESCRIPTORIDS.INPUT);
        Top.add((byte)inputParams); //Default is 2
        if(nButtons%8!=0){ //Padding Bits
            Top.add(HIDDescriptor.DESCRIPTORIDS.REPORT_COUNT);
            Top.add((byte)0x01);
            Top.add(HIDDescriptor.DESCRIPTORIDS.REPORT_SIZE);
            Top.add((byte)(nButtons%8));
            Top.add(HIDDescriptor.DESCRIPTORIDS.INPUT);
            Top.add((byte)(1|2)); //Const Var Abs
        }
        HIDChunk bChunk= new HIDChunk();
        bChunk.Type = ChunkType.BUTTONS;
        bChunk.reportID = 0;
        bChunk.bIndex = 0;
        bChunk.size = nButtons%8==0?nButtons/8:nButtons/8+1;
        int axesChunkKey = HIDChunk.CalHash(ChunkType.AXES,bChunk.reportID);
        if(Chunks.containsKey(0)){
            HIDChunk axesChunk =Objects.requireNonNull(Chunks.get(axesChunkKey));
            axesChunk.bIndex = bChunk.size;
            Chunks.put(axesChunkKey,axesChunk);
        }
        Chunks.put(bChunk.GetHash(),bChunk);
    }
    private void AddDesktopGenericUsagePage(){
        Top.add(HIDDescriptor.DESCRIPTORIDS.USAGE_PAGE);
        Top.add(USAGE_DESKTOP_GENERIC.PAGE);
        Top.add(HIDDescriptor.DESCRIPTORIDS.USAGE);
        Top.add(USAGE_DESKTOP_GENERIC.JOYSTICK);
    }
    public void CollectionStart(byte COLLECTION_TYPE){
        Top.add(HIDDescriptor.DESCRIPTORIDS.COLLECTION_START);
        Top.add(COLLECTION_TYPE);
        Bottom.add(HIDDescriptor.DESCRIPTORIDS.COLLECTION_END);
    }
    public void CollectionEnd(){
        Top.add(Bottom.pop());
    }
    public byte[] GetReportMap(){
        if(!Bottom.isEmpty()){
            throw new RuntimeException("HID Report Map was improperly closed!");
        }
        byte[] reportMap = new byte[Top.size()];
        for(int i = 0;i<Top.size();i++){
            reportMap[i] = Top.get(i);
        }
        //DEBUG only
        Log.w("REPORTMAP",HIDUtils.bytesToHex(reportMap));
        return reportMap;
    }

    public HashMap<Integer, HIDChunk> GetChunks(){
        return Chunks;
    }
    private final HashMap<Integer,HIDChunk> Chunks = new HashMap<>();
}
