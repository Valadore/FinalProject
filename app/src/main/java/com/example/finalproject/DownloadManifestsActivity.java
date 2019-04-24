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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
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

                //send a list of manifests to download
                List<String> manifests = adapter.getManifests();
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
            e.printStackTrace();
        }
        return f;
    }

    //This is where we build the manifest database
    private void buildManifests( List<String> manifests)
    {
        //for each manifest lets 'download'
        for (String manifest : manifests) {

            try {
                JSONObject manifestObj = new JSONObject(readJSONFromAsset(manifest));
                Log.d("test", String.valueOf(manifestObj.names()));
                int size1 = manifestObj.names().length();
                //get each key
                for (int i=0; i<size1; i++)
                {
                    //get each list of parcels
                    JSONArray jsonData = manifestObj.getJSONArray((String) manifestObj.names().get(i));
                    int size2 = jsonData.length();
                    for (int y=0; y<size2; y++)
                    {
                        //address
                        Log.d("test", String.valueOf(manifestObj.names().get(i)));
                        //details
                        Log.d("test", String.valueOf(jsonData.get(y)));
                    }
                    //address
                    //Log.d("test", String.valueOf(manifestObj.names().get(i)));
                    //details
                   // Log.d("test", String.valueOf(jsonData));

                    //manifestObj.names().get(i);

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
    }

    //For the debug app only, we read the json from a file
    //If this was a live version this is where we would download
    public String readJSONFromAsset(String manifest) {
        String json = null;
        try {
            InputStream is = getAssets().open("manifests/" + manifest);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }
}
