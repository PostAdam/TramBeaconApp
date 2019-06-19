package com.pp.services;

import android.content.ContentValues;

public class BluetoothService {
    public static void sendFile(String fileUri, String deviceAddress) {
        ContentValues values = new ContentValues();
        values.put(BluetoothShare.URI, "content://" + fileUri);
        values.put(BluetoothShare.DESTINATION, deviceAddress);
        values.put(BluetoothShare.DIRECTION, BluetoothShare.DIRECTION_OUTBOUND);
        Long timeStamp = System.currentTimeMillis();
        values.put(BluetoothShare.TIMESTAMP, timeStamp);
    }
}
