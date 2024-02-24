package com.example.playem.btmanager;

import java.util.UUID;

public class UUIDUtil {
    //Characteristics UUID 2A4A onwards
    public static final String BTBaseNumber = "-0000-1000-8000-00805F9B34FB";
    public static final UUID SERVICE_HID = UUID.fromString("00001812" + BTBaseNumber);
    public static final UUID CHAR_REPORT = UUID.fromString("00002A4D" + BTBaseNumber);
    public static final UUID CHAR_REPORT_MAP = UUID.fromString("00002A4B" + BTBaseNumber);
    public static final UUID CHAR_HID_INFORMATION = UUID.fromString("00002A4A" + BTBaseNumber);
    public static final UUID CHAR_HID_CONTROL_POINT = UUID.fromString("00002A4C" + BTBaseNumber);
    public static final UUID DESC_REPORT_REFERENCE = UUID.fromString("00002908" + BTBaseNumber);
    public static final UUID DESC_CCC = UUID.fromString("00002902" + BTBaseNumber);

    // DIS related UUIDs
    public static final UUID SERVICE_DIS = UUID.fromString("0000180A" + BTBaseNumber);
    public static final UUID CHAR_PNP_ID = UUID.fromString("00002A50" + BTBaseNumber);
    public static final UUID CHAR_HARD_STR = UUID.fromString("00002A27"+ BTBaseNumber);
    public static final UUID CHAR_SOFT_STR = UUID.fromString("00002A28"+ BTBaseNumber);
    public static final UUID CHAR_MANU_STR = UUID.fromString("00002A29"+ BTBaseNumber);

    // BAS related UUIDs
    public static final UUID SERVICE_BAS = UUID.fromString("0000180F" + BTBaseNumber);
    public static final UUID CHAR_BATTERY_LEVEL = UUID.fromString("00002A19" + BTBaseNumber); //Implement the only mandatory field for bat
}
