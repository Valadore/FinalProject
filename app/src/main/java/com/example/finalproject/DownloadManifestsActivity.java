package com.example.finalproject;

import android.arch.persistence.room.Room;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DownloadManifestsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_manifests);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String[] manifests = getManifestList();
        List<String> stringList = new ArrayList<String>(Arrays.asList(manifests));

        //instantiate custom adapter
        final ManifestListAdapter adapter = new ManifestListAdapter((ArrayList<String>) stringList, this);

        //handle listview and assign adapter
        ListView lView = (ListView)findViewById(R.id.manifest_list);
        lView.setAdapter(adapter);

        //build database
        AppDatabase db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "database").build();

        Button button = (Button) findViewById(R.id.button_continue);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                List<String> manifests = adapter.getManifests();

                //This is where we build the manifest database
                buildManifests(manifests);

                Intent myIntent = new Intent(DownloadManifestsActivity.this, BarcodeScanActivity.class);
                startActivity(myIntent);
            }
        });

    }

    //temp using local files rather than rest api
    private String[] getManifestList()
    {
        String[] f = null;
        try {
            f = getAssets().list("manifests");
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return f;
    }
}
