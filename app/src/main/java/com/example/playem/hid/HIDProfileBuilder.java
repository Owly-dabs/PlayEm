package com.example.playem.hid;

import com.example.playem.hid.usagepages.USAGE_BUTTON;
import com.example.playem.hid.usagepages.USAGE_DESKTOP_GENERIC;

import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Stack;

public class HIDProfileBuilder {
//Stacks
    public HIDProfileBuilder(){
        Top = new PriorityQueue<Byte>();
        Bottom = new Stack<Byte>();
    }
    private Queue<Byte> Top;
    private Stack<Byte> Bottom;
    private byte[] ReportMap;

    private int axes_count = 0;
    public void Init(){
        AddDesktopGenericUsagePage();
        CollectionStart(HIDDescriptor.COLLECTIONTYPES.APPLICATION);
        CollectionStart(HIDDescriptor.COLLECTIONTYPES.PHYSICAL);
        AddButtons(8, (byte) 0x02);
        AddAxes(5,(byte)2);
        CollectionEnd();
        CollectionEnd();
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

    public void AddAxes(int nAxes, byte inputParams) {
        if(nAxes>9){
            throw new RuntimeException("Too Many Axes");
        }
        Top.add(HIDDescriptor.DESCRIPTORIDS.USAGE_PAGE);
        Top.add(USAGE_DESKTOP_GENERIC.PAGE);
        Top.add(HIDDescriptor.DESCRIPTORIDS.USAGE);

        for(int i = 0;i<nAxes;i++) {
            Top.add((byte) (USAGE_DESKTOP_GENERIC.X + (byte) axes_count));
            axes_count++;
        }
        Top.add((byte) (HIDDescriptor.DESCRIPTORIDS.LOGIC_MIN + 1));
        Top.add((byte) 0x00);
        Top.add((byte) 0x80); //Default 16bits LittleEndian

        Top.add((byte) (HIDDescriptor.DESCRIPTORIDS.LOGIC_MAX + 1));
        Top.add((byte) 0xFF);
        Top.add((byte) 0x7F); //Default 16bits LittleEndian

        Top.add((byte) HIDDescriptor.DESCRIPTORIDS.REPORT_SIZE);
        Top.add((byte) 0x02);
        Top.add((byte) HIDDescriptor.DESCRIPTORIDS.REPORT_COUNT);
        Top.add((byte) 0x01);

        Top.add((byte) HIDDescriptor.DESCRIPTORIDS.INPUT);
        Top.add((byte) inputParams);

    }

    public void AddButtons(int nButtons,byte inputParams){
        Top.add(HIDDescriptor.DESCRIPTORIDS.USAGE);
        Top.add(USAGE_BUTTON.PAGE);
        Top.add(HIDDescriptor.DESCRIPTORIDS.USAGE_MIN);
        Top.add((byte)0x01);
        Top.add(HIDDescriptor.DESCRIPTORIDS.USAGE_MAX);
        Top.add((byte)nButtons);
        Top.add(HIDDescriptor.DESCRIPTORIDS.LOGIC_MIN);
        Top.add((byte)0x00);
        Top.add(HIDDescriptor.DESCRIPTORIDS.LOGIC_MAX);
        Top.add((byte)0x01);
        Top.add(HIDDescriptor.DESCRIPTORIDS.REPORT_COUNT);
        Top.add((byte)nButtons);
        Top.add(HIDDescriptor.DESCRIPTORIDS.REPORT_SIZE);
        Top.add((byte)0x01);
        Top.add(HIDDescriptor.DESCRIPTORIDS.INPUT);
        Top.add((byte)inputParams); //Default is 2
        if(nButtons%8!=0){ //Padding Bits
            Top.add(HIDDescriptor.DESCRIPTORIDS.REPORT_COUNT);
            Top.add((byte)nButtons);
            Top.add(HIDDescriptor.DESCRIPTORIDS.REPORT_SIZE);
            Top.add((byte)0x01);
            Top.add(HIDDescriptor.DESCRIPTORIDS.INPUT);
            Top.add((byte)(1|2)); //Const Var Abs
        }
    }
    private void AddDesktopGenericUsagePage(){
        Top.add(HIDDescriptor.DESCRIPTORIDS.USAGE_PAGE);
        Top.add(HIDDescriptor.USAGEPAGE.DESKTOP_GENERIC.PAGE);
        Top.add(HIDDescriptor.DESCRIPTORIDS.USAGE_PAGE);
        Top.add(HIDDescriptor.USAGEPAGE.DESKTOP_GENERIC.GAMEPAD);
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
        if(Bottom.size()>0){
            throw new RuntimeException("HID Report Map was improperly closed!");
        }
        ReportMap = new byte[Top.size()];
        for(int i = 0;i<Top.size();i++){
            ReportMap[i] = Top.remove();
        }
        return ReportMap;
    }
}
