package com.example.finalproject;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.arch.persistence.room.Room;
import android.content.Context;
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
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.ads.internal.request.LargeParcelTeleporter;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
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

    private View mProgressView;
    private View mMapView;

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

        mMapView = findViewById(R.id.map);
        mProgressView = findViewById(R.id.login_progress);
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
        mMap.addMarker(new MarkerOptions().position(myLocation).title("My Location"));

        //get locations on seperate thread
        new AsyncShowLocations().execute();
    }


    class AsyncShowLocations extends AsyncTask<Void, Void, Void>
    {
        List<String> addresses;
        List<LatLng> latLngs;

        @Override
        protected Void doInBackground(Void... voids) {
            addresses = getAddresses();
            latLngs = getLocationFromAddress(addresses);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            showLocations(addresses, latLngs);
        }
    }

    private void showLocations(List<String> addresses, List<LatLng> latLngs)
    {
        List<MarkerOptions> markers = new ArrayList<>();
        for(int i = 0; i < latLngs.size(); i++)
        {
            String address = addresses.get(i);
            LatLng latLng = latLngs.get(i);

            MarkerOptions marker = new MarkerOptions().position(latLng).title(address);
            markers.add(marker);
            //Put marker on map on that LatLng
            mMap.addMarker(marker);
        }

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
