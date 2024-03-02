package com.example.playem.hid;

public class HIDUtils {
    public static byte[] intToLittleEndianByteArray16bit(int value){//only 16bit
        //Negative numbers are ones complement of its value down converting from 32bits

        byte[] retarr = new byte[2];
        if(value<0) {
            retarr[0] = (byte)(value & 0x000000FF);
            retarr[1] = (byte)((value >> 8)|0x80);
        }
        else{
            retarr[0] = (byte)(value);
            retarr[1] = (byte)((value>>8)&0x7F);
        }
        return retarr;
    }
    public static byte[] intToLittleEndianByteArray8bit(int value){//only 8bit
        //Negative numbers are ones complement of its value downconverting from 32bits

        byte[] retarr = new byte[1];
        if(value<0) {
            retarr[0] = (byte)((value)|0x80);
        }
        else{
            retarr[1] = (byte)(value);
        }
        return retarr;
    }
}
