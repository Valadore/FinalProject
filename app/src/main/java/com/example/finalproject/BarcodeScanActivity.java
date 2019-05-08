package com.example.finalproject;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.finalproject.ui.barcodescan.BarcodeScanFragment;

public class BarcodeScanActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.barcode_scan_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, BarcodeScanFragment.newInstance())
                    .commitNow();
        }
    }
}
