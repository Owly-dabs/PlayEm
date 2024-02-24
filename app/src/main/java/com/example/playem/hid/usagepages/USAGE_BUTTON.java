package com.example.playem.hid.usagepages;

public class USAGE_BUTTON {
    public static byte HIDButton(short ButtonNo) {return ButtonNo>255?0:(byte)ButtonNo;}
    public static byte NoPress = 0;
}
