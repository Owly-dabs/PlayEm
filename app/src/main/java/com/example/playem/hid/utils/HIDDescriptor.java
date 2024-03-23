package com.example.playem.hid.utils;

import com.example.playem.hid.utils.*;

public class HIDDescriptor {

    public static final class DESCRIPTORIDS{
        public static final byte COLLECTION_START = (byte)0xA1; //Java negative hex values work around
        public static final byte COLLECTION_END = (byte)0xC0;
        public static final byte USAGE_PAGE = 0x05;
        public static final byte USAGE = 0x09;
        public static final byte USAGE_MIN = 0x19;
        public static final byte USAGE_MAX = 0x29;
        public static final byte LOGIC_MIN = 0x15; //length of 1 byte
        public static final byte LOGIC_MAX = 0x25; //length of 1 byte
        public static final byte PHY_MIN = 0x35; //length of 1 byte
        public static final byte PHY_MAX = 0x45; //length of 1 byte
        public static final byte REPORT_COUNT = (byte)0x95;
        public static final byte REPORT_SIZE = 0x75;
        public static final byte INPUT = (byte)0x81;
    }
    public static final class COLLECTIONTYPES{
        public static final byte APPLICATION = 0x01;
        public static final byte LOGICAL = 0x02;
        public static final byte PHYSICAL = 0x00;
    }
    public static final class USAGEPAGE{
        public static final USAGE_DESKTOP_GENERIC DESKTOP_GENERIC = new USAGE_DESKTOP_GENERIC();
        public static final USAGE_BUTTON BUTTONS = new USAGE_BUTTON();
/*
* A Report descriptor may contain several Main items. A Report descriptor must
include each of the following items to describe a control’s data (all other items are
optional):
 Input (Output or Feature)
 Usage
 Usage Page
 Logical Minimum
 Logical Maximum
 Report Size
 Report Count
* */

        }
    }

