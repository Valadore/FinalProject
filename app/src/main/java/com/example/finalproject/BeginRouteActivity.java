package com.example.finalproject;

import android.Manifest;
import android.app.ProgressDialog;
import android.arch.persistence.room.Room;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class BeginRouteActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    Intent intent = getIntent();
    String txtJson;
    private LatLng startLocation;
    private LatLng endLocation;
    private LatLng lastLocation;
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
        btnNavigate = findViewById(R.id.btn_navigate);

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
                jobNumber++;
                beginStage(jobNumber);
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

    private void beginStage(int stage)
    {
        mMap.clear();
        markers.clear();
        String address = jobs.get(stage).getAddress();

        destination.setText("Destination: " + address);
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
        mMap.addMarker(marker);

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
        int padding = 100; // offset from edges of the map in pixels
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
