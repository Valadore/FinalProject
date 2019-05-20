package com.example.finalproject;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.internal.request.LargeParcelTeleporter;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class InitialMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private LatLng myLocation = null; //currentLocation
    private GPSTracker gpsTracker;
    private GoogleMap mMap;
    private AppDatabase db;
    private Button btnOptimise;
    private LatLng startLocation;

    private List<String> addresses = new ArrayList<>();
    private List<LatLng> latLngs = new ArrayList<>();
    List<MarkerOptions> markers = new ArrayList<>();

    //create a paired list first is adresss second is latlongs
    private Pair<List<String>, List<LatLng> > pair = Pair.create(addresses, latLngs);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial_map);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        LocationListener mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                myLocation = new LatLng(location.getLatitude(), location.getLongitude());
                SupportMapFragment supportMapFragment= (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                supportMapFragment.getMapAsync(InitialMapActivity.this);
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

        btnOptimise = findViewById(R.id.btn_Optimise);
        btnOptimise.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d("TESTING!!", "onPostExecute: " + pair.first.toString());
                Log.d("TESTING!!", "onPostExecute: " + pair.second.toString());

                //TO DO:
                //now we need to get optimal order and route using the tom tom api
                //we have a list of addressses and corisponding latlngs
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (checkLocationPermission()) {
            gpsTracker = new GPSTracker(this);
            if (gpsTracker.canGetLocation) {
                myLocation = new LatLng(gpsTracker.getLatitude(), gpsTracker.getLongitude());
            } else {
                Toast.makeText(this, "please accept permission !!!!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    public boolean checkLocationPermission() {
        String permission = "android.permission.ACCESS_FINE_LOCATION";
        int res = this.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    //Permission Granted
                    gpsTracker = new GPSTracker(this);
                    if (gpsTracker.canGetLocation) {
                        mMap.setMyLocationEnabled(true);
                        myLocation = new LatLng(gpsTracker.getLatitude(), gpsTracker.getLongitude());
                    } else {
                        // permission denied, boo! Disable the
                        // functionality that depends on this permission.
                    }
                    return;
                }
                // other 'case' lines to check for other
                // permissions this app might request
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //get locations on seperate thread
        new AsyncShowLocations().execute(pair);

        //get prompt for start location and add to map
        getStartLocation();
    }

    private void getStartLocation()
    {
        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(InitialMapActivity.this);
        View promptsView = li.inflate(R.layout.prompt_getstart, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                InitialMapActivity.this);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText userInput = (EditText) promptsView
                .findViewById(R.id.editTextDialogUserInput);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                // get user input and set it to result
                                // edit text
                                LatLng newlatlng = getLatLngFromAddress(userInput.getText().toString());
                                if (newlatlng != null) {
                                    startLocation = newlatlng;
                                    MarkerOptions marker = new MarkerOptions()
                                            .position(startLocation)
                                            .title("Start")
                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                                    markers.add(marker);
                                    mMap.addMarker(marker);
                                    zoomMap();
                                } else
                                {
                                    TextView alertTxtView = findViewById(R.id.alertTxtView);
                                    alertTxtView.setText("Could not find address");
                                }
                            }
                        })
                .setNegativeButton("Use My Location",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                                startLocation = myLocation;
                                MarkerOptions marker = new MarkerOptions()
                                        .position(startLocation)
                                        .title("Start")
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

                                markers.add(marker);
                                mMap.addMarker(marker);
                                zoomMap();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    private class AsyncShowLocations extends AsyncTask<Pair<List<String>, List<LatLng> >, Void, Pair<List<String>, List<LatLng> >>
    {
        List<String> addresses;
        List<LatLng> latLngs;

        @Override
        protected Pair doInBackground(Pair<List<String>, List<LatLng> >... pair) {

            addresses = getAddresses();
            latLngs = getLocationFromAddress(addresses);

            Pair<List<String>, List<LatLng> > result = Pair.create(addresses, latLngs);
            return result;
        }

        @Override
        protected void onPostExecute(Pair<List<String>, List<LatLng> > result) {
            super.onPostExecute(result);
            showLocations(addresses, latLngs);
            pair.first.addAll(result.first);
            pair.second.addAll(result.second);
            btnOptimise.setVisibility(View.VISIBLE);
        }
    }

    private void showLocations(List<String> addresses, List<LatLng> latLngs)
    {
        for(int i = 0; i < latLngs.size(); i++)
        {
            String address = addresses.get(i);
            LatLng latLng = latLngs.get(i);

            MarkerOptions marker = new MarkerOptions().position(latLng).title(address);
            markers.add(marker);
            //Put marker on map on that LatLng
            mMap.addMarker(marker);
        }

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

    private List<String> getAddresses(){

        List<String> Addresses = new ArrayList<>();

        db = Room.databaseBuilder(this,
                AppDatabase.class, "sessionDatabase").allowMainThreadQueries().build();

        Job jobs[] = db.myDao().getAllJobs();
        for (Job job : jobs) {
            Addresses.add(job.getAddress() + " " + job.getPostcode());
        }
        return Addresses;
    }

    private List<LatLng> getLocationFromAddress(List<String> addresses)
    {
        List<LatLng> latLngs = new ArrayList<>();
        for (String address : addresses)
        {
            LatLng latLng;
            latLng = getLatLngFromAddress(address);
            latLngs.add(latLng);
        }
        return latLngs;
    }

    private LatLng getLatLngFromAddress(String address)
    {
        Geocoder coder = new Geocoder(this);
        List<Address> possibleaddresses;
        LatLng latLng = null;
        try {
            //Get latLng from String
            possibleaddresses = coder.getFromLocationName(address,5);

            //check for null
            if (possibleaddresses == null) {
                return null;
            }

            //Lets take first possibility from the all possibilities.
            Address location = possibleaddresses.get(0);
            latLng = new LatLng(location.getLatitude(), location.getLongitude());

        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return latLng;
    }

}
