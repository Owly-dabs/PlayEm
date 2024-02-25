package com.example.playem.btmanager;

import com.example.playem.btmanager.blehandlers.interfaces.BLECharacteristicsReadRequest;
import com.example.playem.btmanager.blehandlers.interfaces.BLECharacteristicsWriter;

import java.util.HashMap;
import java.util.UUID;

public interface GattServerCbRouter {
    HashMap<UUID, BLECharacteristicsReadRequest> cReaders = new HashMap<>();
    HashMap<UUID, BLECharacteristicsWriter> cWriters = new HashMap<>();
    //Advertisers etc if required
}
