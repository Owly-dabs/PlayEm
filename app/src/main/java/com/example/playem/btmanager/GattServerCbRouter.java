package com.example.playem.btmanager;

import com.example.playem.btmanager.blehandlers.interfaces.BLECharacteristicsReadRequest;
import com.example.playem.btmanager.blehandlers.interfaces.BLECharacteristicsWriteRequest;
import com.example.playem.btmanager.blehandlers.interfaces.BLEDescriptorReadRequest;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public interface GattServerCbRouter {
    ConcurrentHashMap<UUID, BLECharacteristicsReadRequest> cReaders = new ConcurrentHashMap<>();
    ConcurrentHashMap<UUID, BLECharacteristicsWriteRequest> cWriters = new ConcurrentHashMap<>();
    ConcurrentHashMap<UUID, BLEDescriptorReadRequest> dReaders = new ConcurrentHashMap<>();
    //Advertisers etc if required
}
