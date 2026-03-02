package com.nfc.door;

import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.util.Log;

public class HCEService extends HostApduService {

    private static final String TAG = "HCEService";

    // UID der Zugangskarte
    private static final byte[] UID = {
        (byte)0xEC, (byte)0x4A, (byte)0x5C, (byte)0x14
    };

    // Block 04 Daten
    private static final byte[] BLOCK04 = {
        (byte)0x01, (byte)0x02, (byte)0x03, (byte)0x04,
        (byte)0x05, (byte)0x06, (byte)0x07, (byte)0x08,
        (byte)0x09, (byte)0x0A, (byte)0xFF, (byte)0x0B,
        (byte)0x0C, (byte)0x0D, (byte)0x0E, (byte)0x0F
    };

    // APDU Antworten
    private static final byte[] SELECT_OK = {(byte)0x90, (byte)0x00};
    private static final byte[] UNKNOWN_CMD = {(byte)0x00, (byte)0x00};

    @Override
    public byte[] processCommandApdu(byte[] apdu, Bundle extras) {
        Log.d(TAG, "APDU empfangen: " + bytesToHe
