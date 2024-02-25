package com.example.playem.btmanager.services;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.os.ParcelUuid;
import android.util.Log;
import com.example.playem.btmanager.UUIDUtil;

import java.util.Queue;

public class BLE_HIDServiceBuilder {
    @SuppressLint("MissingPermission")
    //TODO @RequiresPermission(Manifest.....)
    //AdvertisementData is set in the order of Data followed by Scan results
    public static boolean Build(Queue<Object> toAddServices,Queue<Object> toAddAdvertisementSetting,Queue<Object> toAddAdvertisementData, byte[] ReportMap){
        try{
            BluetoothGattService HID_Service = new BluetoothGattService(UUIDUtil.SERVICE_HID,BluetoothGattService.SERVICE_TYPE_PRIMARY);
            BluetoothGattService BAT_Service = new BluetoothGattService(UUIDUtil.SERVICE_BAS,BluetoothGattService.SERVICE_TYPE_PRIMARY);
            BluetoothGattService DIS_Service = new BluetoothGattService(UUIDUtil.SERVICE_DIS,BluetoothGattService.SERVICE_TYPE_PRIMARY);

            ////////////////
            ///DIS Section//
            ////////////////
            BluetoothGattCharacteristic DIS_PnPID = new BluetoothGattCharacteristic(
                    UUIDUtil.CHAR_PNP_ID,
                    BluetoothGattCharacteristic.PROPERTY_READ,
                    BluetoothGattCharacteristic.PERMISSION_READ
            );

            final byte[] VendorID = {0x02,0,0,0,0,0,0};
            DIS_PnPID.setValue(VendorID);
            DIS_Service.addCharacteristic(DIS_PnPID);

            BluetoothGattCharacteristic DIS_SoftID = new BluetoothGattCharacteristic(
                    UUIDUtil.CHAR_SOFT_STR,
                    BluetoothGattCharacteristic.PROPERTY_READ,
                    BluetoothGattCharacteristic.PERMISSION_READ
            );
            DIS_SoftID.setValue("0.0.0.1");
            DIS_Service.addCharacteristic(DIS_SoftID);

            BluetoothGattCharacteristic DIS_ManuID = new BluetoothGattCharacteristic(
                    UUIDUtil.CHAR_MANU_STR,
                    BluetoothGattCharacteristic.PROPERTY_READ,
                    BluetoothGattCharacteristic.PERMISSION_READ
            );
            DIS_ManuID.setValue("PlayEm SUTD");
            DIS_Service.addCharacteristic(DIS_ManuID);

            ////////////////
            ///BAS Section//
            ////////////////
            BluetoothGattCharacteristic BAT_BatLvl = new BluetoothGattCharacteristic(
                    UUIDUtil.CHAR_BATTERY_LEVEL,
                    BluetoothGattCharacteristic.PROPERTY_READ,
                    BluetoothGattCharacteristic.PERMISSION_READ
            );
            BAT_BatLvl.setValue(new byte[]{(byte)0x32}); //TODO Hook to battery Manager and even set flags
            BAT_Service.addCharacteristic(BAT_BatLvl);

            ////////////////
            ///HID Section//
            ////////////////

            //ReportMap
            BluetoothGattCharacteristic ReportMap_C = new BluetoothGattCharacteristic(
                    UUIDUtil.CHAR_REPORT_MAP,
                    BluetoothGattCharacteristic.PROPERTY_READ,
                    BluetoothGattCharacteristic.PERMISSION_READ
            );
            ReportMap_C.setValue(ReportMap);
            HID_Service.addCharacteristic(ReportMap_C);

            //Report
            BluetoothGattCharacteristic Report_C = new BluetoothGattCharacteristic(
                    UUIDUtil.CHAR_REPORT,
                    BluetoothGattCharacteristic.PROPERTY_READ|BluetoothGattCharacteristic.PROPERTY_WRITE|BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                    BluetoothGattCharacteristic.PERMISSION_READ|BluetoothGattCharacteristic.PERMISSION_WRITE
            );
            //Report_C.setValue([]); No values to set yet
                //Report Characteristics Report Reference Desc
                BluetoothGattDescriptor C_Report_RRD = new BluetoothGattDescriptor(
                        UUIDUtil.DESC_REPORT_REFERENCE,
                        BluetoothGattDescriptor.PERMISSION_READ|BluetoothGattDescriptor.PERMISSION_WRITE
                );
                C_Report_RRD.setValue(new byte[]{(byte) 0x02,0x01}); //ID 2 Consumer CTRL,1 for KB Type:Input
                //Report Characteristics ClientCCDesc
                BluetoothGattDescriptor C_Report_CCCD = new BluetoothGattDescriptor(
                        UUIDUtil.DESC_CCC,
                        BluetoothGattDescriptor.PERMISSION_READ|BluetoothGattDescriptor.PERMISSION_WRITE
                );
                C_Report_CCCD.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            Report_C.addDescriptor(C_Report_CCCD);
            Report_C.addDescriptor(C_Report_RRD);
            HID_Service.addCharacteristic(Report_C);

            //Protocol Mode
            BluetoothGattCharacteristic ProtocolMode = new BluetoothGattCharacteristic(
                    UUIDUtil.CHAR_PROTO_MODE,
                    BluetoothGattCharacteristic.PROPERTY_READ|BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
                    BluetoothGattCharacteristic.PERMISSION_READ|BluetoothGattCharacteristic.PERMISSION_WRITE
            );
            ProtocolMode.setValue(new byte[]{0x01});
            //Protocol mode optional in report mode;
            HID_Service.addCharacteristic(ProtocolMode);

            //HID Information
            BluetoothGattCharacteristic HIDInfo = new BluetoothGattCharacteristic(
                    UUIDUtil.CHAR_HID_INFORMATION,
                    BluetoothGattCharacteristic.PROPERTY_READ,
                    BluetoothGattCharacteristic.PERMISSION_READ
                );
            HIDInfo.setValue(new byte[]{0x11,0x01,0x00,0x02}); //HID LSB first pg 19 USB HID1.11 6.2.1 pg 22 Normally connectable

            //HID ControlPt
            BluetoothGattCharacteristic HIDControlPoint = new BluetoothGattCharacteristic( //Core pg 1407
                    UUIDUtil.CHAR_HID_CONTROL_POINT,
                    BluetoothGattCharacteristic.PROPERTY_WRITE,
                    BluetoothGattCharacteristic.PERMISSION_WRITE
            );
            HIDControlPoint.setValue(new byte[]{0x00}); //Enable to get signal of sleep state from host
            HID_Service.addCharacteristic(HIDControlPoint);

            //Setup Advertisements HOGP 3.1.3 pg13+
            AdvertiseSettings advertiseSettings = new AdvertiseSettings.Builder()
                    .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                    .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                    .setConnectable(true)
                    .setTimeout(0).build(); //Should set to some finite value after testing
            AdvertiseData advertiseData = new AdvertiseData.Builder()
                    .addServiceData(ParcelUuid.fromString(UUIDUtil.ADVERT_APPEARANCE.toString()),new byte[]{0x03,(byte)0xC4}) //Either in Advertise or Scan result GamePad Icon
                    .setIncludeDeviceName(true) //Not sure if LOCAL Name refers to UUID Advert_LOCAL_Name or Datatype 0x09 setting to device name first
                    .addServiceUuid(ParcelUuid.fromString(UUIDUtil.SERVICE_HID.toString()))
                    .addServiceUuid(ParcelUuid.fromString(UUIDUtil.SERVICE_DIS.toString()))
                    .addServiceUuid(ParcelUuid.fromString(UUIDUtil.SERVICE_BAS.toString())).build();

            AdvertiseData scanResult = new AdvertiseData.Builder()
                    .addServiceUuid(ParcelUuid.fromString(UUIDUtil.SERVICE_HID.toString()))
                    .addServiceUuid(ParcelUuid.fromString(UUIDUtil.SERVICE_DIS.toString()))
                    .addServiceUuid(ParcelUuid.fromString(UUIDUtil.SERVICE_BAS.toString())).build();

            toAddServices.add(DIS_Service);
            toAddServices.add(HID_Service);
            toAddServices.add(BAT_Service); //Caller must start service add function;

            toAddAdvertisementSetting.add(advertiseSettings);
            toAddAdvertisementData.add(advertiseData);
            toAddAdvertisementData.add(scanResult);
            return true;
        }
        catch(Exception e){
            Log.e("BTBUILD",e.toString());
            }
        return false;
    }
}
