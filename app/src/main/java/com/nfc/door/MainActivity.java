package com.nfc.door;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

    private NfcAdapter nfcAdapter;
    private TextView statusText;
    private Button toggleButton;
    private boolean isActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.statusText);
        toggleButton = findViewById(R.id.toggleButton);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        // NFC verfügbar?
        if (nfcAdapter == null) {
            statusText.setText("❌ NFC nicht verfügbar");
            toggleButton.setEnabled(false);
            return;
        }

        if (!nfcAdapter.isEnabled()) {
            statusText.setText("⚠️ NFC deaktiviert");
        } else {
            statusText.setText("✅ Bereit — NFC aktiv");
        }

        toggleButton.setOnClickListener(v -> {
            if (!isActive) {
                // HCE Service starten
                Intent intent = new Intent(this, HCEService.class);
                startService(intent);
                isActive = true;
                toggleButton.setText("Deaktivieren");
                statusText.setText("📡 Emulation aktiv\nUID: EC 4A 5C 14\nAn Lesegerät halten!");
            } else {
                // HCE Service stoppen
                Intent intent = new Intent(this, HCEService.class);
                stopService(intent);
                isActive = false;
                toggleButton.setText("Tür öffnen");
                statusText.setText("✅ Bereit — NFC aktiv");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nfcAdapter != null && !nfcAdapter.isEnabled()) {
            statusText.setText("⚠️ Bitte NFC aktivieren");
        }
    }
}
