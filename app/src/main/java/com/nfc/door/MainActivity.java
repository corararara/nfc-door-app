package com.nfc.door;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private TextView statusText;
    private TextView cardInfoText;
    private Button toggleButton;
    private Button scanButton;
    private boolean isActive = false;
    private boolean scanMode = false;

    // Gespeicherte Kartendaten
    private byte[] savedUID = {
        (byte)0xEC, (byte)0x4A, (byte)0x5C, (byte)0x14
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText   = findViewById(R.id.statusText);
        cardInfoText = findViewById(R.id.cardInfoText);
        toggleButton = findViewById(R.id.toggleButton);
        scanButton   = findViewById(R.id.scanButton);
        nfcAdapter   = NfcAdapter.getDefaultAdapter(this);

        pendingIntent = PendingIntent.getActivity(
            this, 0,
            new Intent(this, getClass())
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_MUTABLE
        );

        if (nfcAdapter == null) {
            statusText.setText("❌ NFC nicht verfügbar");
            toggleButton.setEnabled(false);
            scanButton.setEnabled(false);
            return;
        }

        updateCardInfo();

        // Tür öffnen Button
        toggleButton.setOnClickListener(v -> {
            scanMode = false;
            if (!isActive) {
                Intent intent = new Intent(this, HCEService.class);
                startService(intent);
                isActive = true;
                toggleButton.setText("Deaktivieren");
                statusText.setText(
                    "📡 Emulation aktiv\n" +
                    "UID: " + bytesToHex(savedUID) + "\n" +
                    "An Lesegerät halten!"
                );
            } else {
                Intent intent = new Intent(this, HCEService.class);
                stopService(intent);
                isActive = false;
                toggleButton.setText("Tür öffnen");
                statusText.setText("✅ Bereit");
            }
        });

        // Karte scannen Button
        scanButton.setOnClickListener(v -> {
            if (isActive) {
                Intent intent = new Intent(this, HCEService.class);
                stopService(intent);
                isActive = false;
                toggleButton.setText("Tür öffnen");
            }
            scanMode = true;
            statusText.setText("📶 Scan-Modus aktiv\nZugangskarte jetzt ranhalten...");
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nfcAdapter != null) {
            IntentFilter[] filters = new IntentFilter[]{
                new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED)
            };
            nfcAdapter.enableForegroundDispatch(
                this, pendingIntent, filters, null
            );
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (!scanMode) return;

        String action = intent.getAction();
        if (!NfcAdapter.ACTION_TAG_DISCOVERED.equals(action) &&
            !NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) return;

        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (tag == null) return;

        // UID auslesen
        byte[] uid = tag.getId();
        savedUID = uid;

        // HCEService mit neuer UID aktualisieren
        HCEService.updateUID(uid);

        // Chip-Info anzeigen
        StringBuilder info = new StringBuilder();
        info.append("✅ Karte gescannt!\n\n");
        info.append("UID: ").append(bytesToHex(uid)).append("\n");
        info.append("UID (DEZ): ").append(bytesToDec(uid)).append("\n");
        info.append("Größe: ").append(uid.length).append(" Byte\n");

        // Chiptype ermitteln
        String[] techList = tag.getTechList();
        for (String tech : techList) {
            if (tech.contains("MifareClassic")) {
                MifareClassic mifare = MifareClassic.get(tag);
                info.append("Typ: MIFARE Classic\n");
                info.append("Sektoren: ").append(mifare.getSectorCount()).append("\n");
                info.append("Blöcke: ").append(mifare.getBlockCount()).append("\n");
                info.append("Größe: ").append(mifare.getSize()).append(" Byte\n");
                break;
            } else if (tech.contains("MifareUltralight")) {
                info.append("Typ: MIFARE Ultralight\n");
                break;
            } else if (tech.contains("IsoDep")) {
                info.append("Typ: ISO 14443-4\n");
                break;
            }
        }

        statusText.setText("✅ Karte gespeichert!");
        cardInfoText.setText(info.toString());
        scanMode = false;
        updateCardInfo();
    }

    private void updateCardInfo() {
        cardInfoText.setText(
            "Aktive UID: " + bytesToHex(savedUID)
        );
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }

    private String bytesToDec(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append((b & 0xFF)).append(" ");
        }
        return sb.toString().trim();
    }
}
