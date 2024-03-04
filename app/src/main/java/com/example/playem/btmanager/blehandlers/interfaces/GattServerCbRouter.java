package com.example.playem.btmanager.blehandlers.interfaces;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public interface GattServerCbRouter {
    ConcurrentHashMap<UUID, BLECharacteristicsReadRequest> cReaders = new ConcurrentHashMap<>();
    ConcurrentHashMap<UUID, BLECharacteristicsWriteRequest> cWriters = new ConcurrentHashMap<>();
    ConcurrentHashMap<UUID, BLEDescriptorReadRequest> dReaders = new ConcurrentHashMap<>();
    //Advertisers etc if required
}
