package com.nfc.door;

import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.util.Log;

public class HCEService extends HostApduService {

    private static final String TAG = "HCEService";

    private static final byte[] UID = {
        (byte)0xEC, (byte)0x4A, (byte)0x5C, (byte)0x14
    };

    private static final byte[] BLOCK04 = {
        (byte)0x01, (byte)0x02, (byte)0x03, (byte)0x04,
        (byte)0x05, (byte)0x06, (byte)0x07, (byte)0x08,
        (byte)0x09, (byte)0x0A, (byte)0xFF, (byte)0x0B,
        (byte)0x0C, (byte)0x0D, (byte)0x0E, (byte)0x0F
    };

    private static final byte[] SELECT_OK = {
        (byte)0x90, (byte)0x00
    };

    private static final byte[] UNKNOWN_CMD = {
        (byte)0x00, (byte)0x00
    };

    @Override
    public byte[] processCommandApdu(byte[] apdu, Bundle extras) {
        Log.d(TAG, "APDU: " + bytesToHex(apdu));

        if (apdu == null || apdu.length < 2) {
            return UNKNOWN_CMD;
        }

        if (apdu[0] == (byte)0x00 && apdu[1] == (byte)0xA4) {
            Log.d(TAG, "SELECT");
            return buildUIDResponse();
        }

        if (apdu[0] == (byte)0x00 && apdu[1] == (byte)0xB0) {
            Log.d(TAG, "READ");
            return buildBlock04Response();
        }

        if (apdu[0] == (byte)0xFF && apdu[1] == (byte)0xCA) {
            Log.d(TAG, "GET UID");
            return buildUIDResponse();
        }

        return SELECT_OK;
    }

    private byte[] buildUIDResponse() {
        byte[] response = new byte[UID.length + 2];
        System.arraycopy(UID, 0, response, 0, UID.length);
        response[UID.length] = (byte)0x90;
        response[UID.length + 1] = (byte)0x00;
        return response;
    }

    private byte[] buildBlock04Response() {
        byte[] response = new byte[BLOCK04.length + 2];
        System.arraycopy(BLOCK04, 0, response, 0, BLOCK04.length);
        response[BLOCK04.length] = (byte)0x90;
        response[BLOCK04.length + 1] = (byte)0x00;
        return response;
    }

    private String bytesToHex(byte[] bytes) {
        if (bytes == null) return "";
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString();
    }

    @Override
    public void onDeactivated(int reason) {
        Log.d(TAG, "Deaktiviert: " + reason);
    }
}
