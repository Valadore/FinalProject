package com.example.finalproject;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class BeginRouteActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    String txtJson;
    private LatLng startLocation;
    private LatLng endLocation;
    private LatLng lastLocation;
    private LatLng myLocation = null; //currentLocation
    ProgressDialog pd;
    private AppDatabase db;
    private List<MarkerOptions> markers = new ArrayList<>();
    private ArrayList<ArrayList<String>> pollyLines = new ArrayList<>();
    private Button btnBegin;
    private Button btnFail;
    private Button btnComplete;
    private FloatingActionButton btnNavigate;
    private LinearLayout btnPanel;
    private TextView destination;
    private TextView numParcels;
    private ArrayList<Job> jobs;
    private int jobNumber;
    private String uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_begin_route);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Intent intent = getIntent();
        txtJson = Objects.requireNonNull(intent.getExtras()).getString("jsonText");
        startLocation = (LatLng) Objects.requireNonNull(intent.getExtras()).get("startLocation");
        endLocation = (LatLng) Objects.requireNonNull(intent.getExtras()).get("endLocation");
        btnBegin = findViewById(R.id.btn_Begin);
        btnFail = findViewById(R.id.btn_fail);
        btnComplete = findViewById(R.id.btn_complete);
        btnPanel = findViewById(R.id.btn_panel);
        destination = findViewById(R.id.txt_destination);
        numParcels = findViewById(R.id.txt_num_parcels);
        btnNavigate = findViewById(R.id.btn_navigate);

        LocationListener mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                myLocation = new LatLng(location.getLatitude(), location.getLongitude());
            }
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }
            @Override
            public void onProviderEnabled(String provider) {
            }
            @Override
            public void onProviderDisabled(String provider) {
            }
        };
        LocationManager mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000,
                    10, mLocationListener);
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        new AsyncGetJobs().execute();

        btnBegin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                beginRoute();
            }
        });
    }

    private void beginRoute(){
        btnBegin.setVisibility(View.GONE);
        btnPanel.setVisibility(View.VISIBLE);
        jobNumber = 0;
        lastLocation = startLocation;

        beginStage(jobNumber);
        btnFail.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                failJob(jobNumber);
            }
        });

        btnComplete.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                completeJob(jobNumber);
            }
        });

        btnNavigate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse(uri));
                startActivity(intent);
            }
        });
    }
    private CaptureSignatureView mSig;
    private LinearLayout mContent;

    private void completeJob(final int stage)
    {
        findViewById(R.id.sig_container).setVisibility(View.VISIBLE);
        btnPanel.setVisibility(View.GONE);
        mContent = findViewById(R.id.get_sig);
        mSig = new CaptureSignatureView(this, null);
        mContent.addView(mSig, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        Button btnGetSig = findViewById(R.id.btn_save_sig);
        btnGetSig.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // get prompts.xml view
                LayoutInflater li = LayoutInflater.from(BeginRouteActivity.this);
                View promptsView = li.inflate(R.layout.prompt_getstart, null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        BeginRouteActivity.this);

                // set prompts.xml to alertdialog builder
                alertDialogBuilder.setView(promptsView);

                final EditText userInput = promptsView
                        .findViewById(R.id.editTextDialogUserInput);
                userInput.setVisibility(View.GONE);

                TextView alertTxt = promptsView.findViewById(R.id.alertTxtView);
                alertTxt.setText("Accept Signature?");

                // set dialog message
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("Confirm",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {

                                        db = Room.databaseBuilder(getApplicationContext(),
                                                AppDatabase.class, "sessionDatabase").allowMainThreadQueries().build();
                                        //update job
                                        Job job = jobs.get(stage);
                                        job.setStatus("Complete");
                                        db.myDao().updateJob(job);

                                        //update parcels
                                        Parcel[] parcels = db.myDao().getParcelsFromJob(job.getJobID());

                                        //update parcels and save data
                                        for(Parcel parcel: parcels)
                                        {
                                            Bitmap signature = mSig.getBitmap();
                                            String fileName = "Sig_" + parcel.getParcelBarcode() + ".jpg";
                                            saveToInternalStorage(signature, fileName);

                                            parcel.setStatus("Complete");
                                            parcel.setSignatureFileName(fileName);
                                            parcel.setGPS(myLocation.toString());

                                            db.myDao().updateParcel(parcel);
                                        }

                                        mContent.removeAllViews();
                                        findViewById(R.id.sig_container).setVisibility(View.GONE);
                                        btnPanel.setVisibility(View.VISIBLE);
                                        jobNumber++;
                                        beginStage(jobNumber);
                                    }
                                })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        mSig.ClearCanvas();
                                        dialog.cancel();
                                    }
                                });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();
                // show it
                alertDialog.show();
            }
        });
    }

    private void saveToInternalStorage(Bitmap bitmapImage, String fileName){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(directory,fileName);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void failJob(final int stage)
    {
        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(BeginRouteActivity.this);
        View promptsView = li.inflate(R.layout.prompt_getstart, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                BeginRouteActivity.this);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText userInput = promptsView
                .findViewById(R.id.editTextDialogUserInput);
        userInput.setVisibility(View.GONE);

        TextView alertTxt = promptsView.findViewById(R.id.alertTxtView);
        alertTxt.setText("Confirm fail all parcels for job?");

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Confirm",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                jobs.get(stage).setStatus("Failed");
                                db = Room.databaseBuilder(getApplicationContext(),
                                        AppDatabase.class, "sessionDatabase").allowMainThreadQueries().build();
                                db.myDao().updateJob(jobs.get(stage));

                                Parcel[] parcels = db.myDao().getParcelsFromJob(jobs.get(stage).getJobID());
                                for (Parcel parcel : parcels)
                                {
                                    parcel.setStatus("Failed");
                                    db.myDao().updateParcel(parcel);
                                }
                                jobNumber++;
                                beginStage(jobNumber);
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show it
        alertDialog.show();
    }
    private void beginStage(int stage)
    {
        mMap.clear();
        markers.clear();
        if (stage < jobs.size())
        {
            String address = jobs.get(stage).getAddress();
            destination.setText("Destination: " + address);

            db = Room.databaseBuilder(getApplicationContext(),
                    AppDatabase.class, "sessionDatabase").allowMainThreadQueries().build();
            int numPar = db.myDao().getParcelsFromJob( jobs.get(stage).getJobID()).length;
            numParcels.setText("Number of Parcels: " + numPar);
            String temp = jobs.get(stage).getLatlng();
            temp = temp.replaceAll("lat/lng: \\(", "");
            temp = temp.replaceAll("\\)", "");

            String[] latlong =  temp.split(",");
            double latitude = Double.parseDouble(latlong[0]);
            double longitude = Double.parseDouble(latlong[1]);
            LatLng newLocation = new LatLng(latitude,longitude);

            MarkerOptions marker = new MarkerOptions().position(newLocation).title(address);
            markers.add(marker);
            //Put marker on map on that LatLng
            mMap.addMarker(marker).showInfoWindow();

            marker = new MarkerOptions()
                    .position(lastLocation)
                    .title("Start")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            markers.add(marker);
            //Put marker on map on that LatLng
            mMap.addMarker(marker);

            drawPollyLine(pollyLines.get(stage));
            lastLocation = newLocation;
            zoomMap();
            uri = "https://www.google.com/maps/dir/?api=1&destination=" + temp;
        } else
        {
            jobsFinished();
        }
    }

    private void jobsFinished()
    {
        Intent myIntent = new Intent(BeginRouteActivity.this, AllJobsFinishedActivity.class);
        startActivity(myIntent);
    }

    private class AsyncGetJobs extends AsyncTask<ArrayList<Job>, Void, ArrayList<Job>>
    {
        //ArrayList<Job> jobs;
        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(BeginRouteActivity.this);
            pd.setMessage("Please wait");
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected ArrayList<Job> doInBackground(ArrayList<Job>... arrayLists) {
            jobs = getOptimisedList();
            return jobs;
        }

        @Override
        protected void onPostExecute(ArrayList<Job> result) {
            super.onPostExecute(result);
            jobs = result;
            showLocations(result);

            getPollyLines();
            drawPollyLines();

            btnBegin.setVisibility(View.VISIBLE);
            if (pd.isShowing()){
                pd.dismiss();
            }
        }
    }

    private void drawPollyLines()
    {
        for (ArrayList<String> line : pollyLines) {
            drawPollyLine(line);
        }
    }
    private void drawPollyLine(ArrayList<String> line)
    {
        PolylineOptions rectOptions = new PolylineOptions();
        for (String subline : line)
        {
            String[] latlong =  subline.split(",");
            double latitude = Double.parseDouble(latlong[0]);
            double longitude = Double.parseDouble(latlong[1]);
            rectOptions.add(new LatLng(latitude,longitude));
            markers.add(new MarkerOptions()
                    .position(new LatLng(latitude,longitude)));
        }

        rectOptions.color(Color.argb(100,0,0,255));
        rectOptions.width(25);
        mMap.addPolyline(rectOptions);
    }

    private void getPollyLines() {
        JSONObject obj = null;
        try {
            Log.d("txtjsdon", "getPollyLines: " + txtJson);
            obj = new JSONObject(txtJson);
            JSONArray routes = obj.optJSONArray("routes");
            Log.d("TESTING!!", "routes: " + routes);
            Log.d("TESTING!!", "size of routes: " + routes.length());

            for(int i=0;i<routes.length(); i++)
            {
                String tempLegs = routes.getString(i);
                JSONObject obj2 = new JSONObject(tempLegs);
                JSONArray legs = obj2.getJSONArray("legs");
                for (int y=0;y<legs.length();y++)
                {
                    String tempPoints = legs.getString(y);
                    JSONObject obj3 = new JSONObject(tempPoints);
                    JSONArray points = obj3.getJSONArray("points");
                    ArrayList<String> legArray = new ArrayList<>();
                    for (int u = 0;u<points.length();u++)
                    {
                        String temp = points.getString(u);

                        temp = temp.replaceAll("\\{\"latitude\":", "");
                        temp = temp.replaceAll("\"longitude\":", "");
                        temp = temp.replaceAll("\\}", "");

                        legArray.add(temp);
                    }
                    pollyLines.add(legArray);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showLocations(List<Job> jobs)
    {
        for(int i = 0; i < jobs.size(); i++)
        {
            String address = jobs.get(i).getAddress();

            String temp = jobs.get(i).getLatlng();
            Log.d("STRINGCHECK", "showLocations: " + temp);
            temp = temp.replaceAll("lat/lng: \\(", "");
            temp = temp.replaceAll("\\)", "");

            Log.d("STRINGCHECK", "showLocations: " + temp);

            String[] latlong =  temp.split(",");
            double latitude = Double.parseDouble(latlong[0]);
            double longitude = Double.parseDouble(latlong[1]);

            MarkerOptions marker = new MarkerOptions().position(new LatLng(latitude,longitude)).title(address);
            markers.add(marker);
            //Put marker on map on that LatLng
            mMap.addMarker(marker);
        }

        MarkerOptions marker = new MarkerOptions()
                .position(startLocation)
                .title("Start")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        markers.add(marker);
        //Put marker on map on that LatLng
        mMap.addMarker(marker);

        marker = new MarkerOptions()
                .position(endLocation)
                .title("Start")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        markers.add(marker);
        //Put marker on map on that LatLng
        mMap.addMarker(marker);

        zoomMap();
    }

    private void zoomMap()
    {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (MarkerOptions marker : markers) {
            builder.include(marker.getPosition());
        }

        //Animate and Zoom on that map location
        LatLngBounds bounds = builder.build();
        int padding = 300; // offset from edges of the map in pixels
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        mMap.animateCamera(cu);
    }

    private ArrayList<Job> getOptimisedList(){

        //build database !!!!temp useing main thread need to change!!!!!!!!!!
        AppDatabase db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "sessionDatabase").allowMainThreadQueries().build();

        List<Job> jobs;
        jobs = Arrays.asList(db.myDao().getAllJobs());

        Collections.sort(jobs, new Comparator<Job>(){
            public int compare(Job obj1, Job obj2) {
                return Integer.compare(obj1.getOrder(), obj2.getOrder()); // To compare integer values
            }
        });

        return new ArrayList<>(jobs);
    }


}
