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
import java.util.Date;
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

        Button button = (Button) findViewById(R.id.button_continue);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                //send a list of manifests to download
                List<String> manifests = adapter.getManifests();
                buildManifests(manifests);

                Intent myIntent = new Intent(DownloadManifestsActivity.this, BarcodeListActivity.class);
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
    //may have to make this asynchronous, don't want to crash main thread!!!
    private void buildManifests( List<String> manifests)
    {

        //build database !!!!temp useing main thread need to change!!!!!!!!!!
        AppDatabase db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "sessionDatabase").allowMainThreadQueries().build();

        db.clearAllTables();

        //temp session ID will be static!!!!
        String sessionID = "7681250419";


        Date d = new Date();

        Session newSession = new Session();
        newSession.setSessionID(sessionID);
        newSession.setUserID("7681");
        newSession.setDate(d);
        newSession.setStatus("Incomplete");
        db.myDao().createSession(newSession);

        int numrounds = 0;
        int numjobs = 0;
        int numparcels = 0;
        //for each manifest lets 'download'
        for (String manifest : manifests) {

            //this is where we make a new round
            Round newRound = new Round();
            String roundId = manifest.replace(".json", "");

            newRound.setRoundID(roundId);
            newRound.setStatus("Incomplete");
            newRound.setSessionID(sessionID);

            //add new round to the database
            numrounds++;
            Log.d("Number of Rounds", String.valueOf(numrounds));
            db.myDao().createRound(newRound);

            //--------------------------------
            try {
                //this is for the debug app on live we would download here
                JSONObject manifestObj = new JSONObject(readJSONFromAsset(manifest));

                //Log.d("Get each address name", String.valueOf(manifestObj.names()));
                int size1 = manifestObj.names().length();
                //get each key
                for (int i=0; i<size1; i++)
                {
                    //this is where we make new job
                    Job newJob = new Job();
                    String jobName = (String) manifestObj.names().get(i);

                    //get each job
                    JSONArray manifestObj2 = manifestObj.getJSONArray(jobName);
                    JSONObject currentJob = manifestObj2.getJSONObject(0);

                    String jobID = jobName;
                    String jobType = (String) currentJob.get("Parcel type");
                    String name = (String) currentJob.get("Name");
                    String postcode = (String) currentJob.get("Postcode");
                    String address = jobName;
                    String phoneNumber = (String) currentJob.get("Phonenumber");
                    String status = "Incomplete";
                    String client = (String) currentJob.get("Client");

                    newJob.setJobID(jobID);
                    newJob.setJobType(jobType);
                    newJob.setName(name);
                    newJob.setPostcode(postcode);
                    newJob.setAddress(address);
                    newJob.setPhoneNumber(phoneNumber);
                    newJob.setStatus(status);
                    newJob.setClient(client);
                    newJob.setRoundID(roundId);

                    //add job to database
                    numjobs++;
                    Log.d("Number of jobs", String.valueOf(numjobs));
                    db.myDao().createJob(newJob);

                    //-----------------------------

                    //get each list of parcels
                    JSONArray jsonData = manifestObj.getJSONArray((String) manifestObj.names().get(i));
                    int size2 = jsonData.length();
                    for (int y=0; y<size2; y++)
                    {
                        //this is each parcel we add

                        Parcel newParcel = new Parcel();
                        JSONObject currentParcel = manifestObj2.getJSONObject(y);

                        String parcelBarcode = (String) currentParcel.get("Barcode");
                        String parcelType = (String) currentParcel.get("Parcel type");
                        String parcelStatus = "Incomplete";
                        String parcelJobID = jobName;

                        newParcel.setParcelBarcode(parcelBarcode);
                        newParcel.setParcelType(parcelType);
                        newParcel.setStatus(parcelStatus);
                        newParcel.setJobID(parcelJobID);

                        numparcels++;
                        Log.d("Number of parcels", String.valueOf(numparcels));
                        db.myDao().createParcel(newParcel);

                        //--------------------------
                        //address
                        //Log.d("test", String.valueOf(manifestObj.names().get(i)));
                        //details
                       // Log.d("test", String.valueOf(jsonData.get(y)));
                    }
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
