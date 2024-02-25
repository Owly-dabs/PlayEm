package com.example.playem.btmanager.blehandlers.interfaces;

import java.util.concurrent.ConcurrentLinkedQueue;

public class ConcurrentTransferQueue {
    public byte[] dequeue() {
        byte[] latest = transferQueue.poll();
        byte[] peek = latest;
        while (peek != null) {
            peek = transferQueue.poll();
            if (peek != null) {
                latest = peek;
            }
        }
        return latest;
    }
    public void enqueue(byte[] data) {
        transferQueue.add(data);
    }

    private ConcurrentLinkedQueue<byte[]> transferQueue = new ConcurrentLinkedQueue<>();
}
