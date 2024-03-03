package com.example.playem.hid;

public class HIDUtils {
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }
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
