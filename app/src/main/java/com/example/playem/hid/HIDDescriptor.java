package com.example.playem.hid;

import com.example.playem.hid.usagepages.*;

public class HIDDescriptor {

    public static final class DESCRIPTORIDS{
        public final byte COLLECTION_START = (byte)0xA1; //Java negative hex values work around
        public final byte COLLECTION_END = (byte)0xC0;
        public final byte USAGE_PAGE = 0x05;
        public final byte USAGE = 0x09;
        public final byte USAGE_MIN = 0x19;
        public final byte USAGE_MAX = 0x29;
        public final byte LOGIC_MIN = 0x15; //length of 1 byte
        public final byte LOGIC_MAX = 0x25; //length of 1 byte
        public final byte REPORT_COUNT = (byte)0x95;
        public final byte REPORT_SIZE = 0x75;
        public final byte INPUT = (byte)0x81;
    }
    public static final class COLLECTIONTYPES{
        public final byte APPLICATION = 0x01;
        public final byte LOGICAL = 0x02;
        public final byte PHYSICAL = 0x00;
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

