package com.example.finalproject;

import android.Manifest;
import android.arch.persistence.room.Room;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class BarcodeListActivity extends AppCompatActivity {

    private static final int REQ_CODE_PERMISSION = 0x1111;
    private ListView lView;
    private List<String> barcodeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Button btn = (Button) findViewById(R.id.btn_sm);

        //build database !!!!temp useing main thread need to change!!!!!!!!!!
        AppDatabase db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "sessionDatabase").allowMainThreadQueries().build();

        barcodeList = db.myDao().getAllBarcodes();

        //instantiate custom adapter
        final BarcodeListAdapter adapter = new BarcodeListAdapter((ArrayList<String>) barcodeList, this);

        //handle listview and assign adapter
        lView = (ListView)findViewById(R.id.barcode_list);
        lView.setAdapter(adapter);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Open Scan Activity
                if (ContextCompat.checkSelfPermission(BarcodeListActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    // Do not have the permission of camera, request it.
                    ActivityCompat.requestPermissions(BarcodeListActivity.this, new String[]{Manifest.permission.CAMERA}, REQ_CODE_PERMISSION);
                } else {
                    // Have gotten the permission
                    Intent myIntent = new Intent(BarcodeListActivity.this, BarcodeScanActivity.class);
                    startActivity(myIntent);

                }
            }
        });

        Button button = (Button) findViewById(R.id.button_continue);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent myIntent = new Intent(BarcodeListActivity.this, InitialMapActivity.class);
                startActivity(myIntent);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQ_CODE_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // User agree the permission
                    Intent myIntent = new Intent(BarcodeListActivity.this, BarcodeScanActivity.class);
                    startActivity(myIntent);

                } else {
                    // User disagree the permission
                    Toast.makeText(this, "You must agree the camera permission request before you use the code scan function", Toast.LENGTH_LONG).show();
                }
            }
            break;
        }
    }
}