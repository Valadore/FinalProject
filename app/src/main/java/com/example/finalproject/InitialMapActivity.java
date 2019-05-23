package com.example.finalproject;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.arch.persistence.room.Room;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.provider.Telephony;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.text.Layout;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.gms.plus.model.people.Person;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class InitialMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private LatLng myLocation = null; //currentLocation
    private GPSTracker gpsTracker;
    private GoogleMap mMap;
    private AppDatabase db;
    private Button btnOptimise;
    private LatLng startLocation;
    private LatLng endLocation;

    private List<String> addresses = new ArrayList<>();
    private List<LatLng> latLngs = new ArrayList<>();
    private List<String> optimisedAddresses = new ArrayList<>();
    private List<LatLng> optimisedLatLngs = new ArrayList<>();
    private List<MarkerOptions> markers = new ArrayList<>();
    private List<String> jobID = new ArrayList<>();
    private List<String> optimisedJobID = new ArrayList<>();

    String txtJson;
    ProgressDialog pd;

    @Override
    public void onBackPressed(){
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            Log.i("MainActivity", "popping backstack");
            fm.popBackStackImmediate();
        } else {
            Log.i("MainActivity", "nothing on backstack, calling super");
            super.onBackPressed();
        }
    }

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
                SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
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


                addresses = pair.first;
                latLngs = pair.second;

                addresses.add(0, "Start");
                addresses.add("end");
                latLngs.add(0, startLocation);
                latLngs.add(endLocation);

                String latlngString = latLngs.toString();
                latlngString = latlngString.replaceAll("\\), lat/lng: \\(", ":");
                latlngString = latlngString.replaceAll("\\[lat/lng: \\(", "");
                latlngString = latlngString.replaceAll("\\)]", "");


                Log.d("TESTING!!", "onPostExecute: " + addresses);
                Log.d("TESTING!!", "onPostExecute: " + latlngString);
                //TO DO:
                //now we need to get optimal order and route using the tom tom api
                //we have a list of addressses and corisponding latlngs

                //for live use this should be obfuscated
                String key = "5jamNFMokhWdAqpWIxGjNh388PHjJP69";

                String url = "https://api.tomtom.com/routing/1/calculateRoute/" + latlngString +
                        "/json?computeBestOrder=true&routeRepresentation=polyline&" +
                        "computeTravelTimeFor=none&routeType=fastest&traffic=false&" +
                        "avoid=unpavedRoads&travelMode=car&key=" + key;

                new JsonTask().execute(url);
            }
        });
    }

    private void getOptimisedRoute()
    {
        JSONObject obj = null;
        try {
            obj = new JSONObject(txtJson);
            JSONArray optimizedWaypoints = obj.getJSONArray("optimizedWaypoints");
            Log.d("TESTING!!", "optimizedWaypoints: " + optimizedWaypoints);

            for(int i=0;i<optimizedWaypoints.length(); i++)
            {
                String temp = optimizedWaypoints.getString(i);
                JSONObject obj2 = new JSONObject(temp);
                int optimised = Integer.parseInt(obj2.getString("optimizedIndex")) + 1;
                optimisedAddresses.add(addresses.get(optimised));
                optimisedLatLngs.add(latLngs.get(optimised));
                optimisedJobID.add(jobID.get(optimised - 1));
                db.myDao().updateJobLatlng(latLngs.get(optimised - 1).toString(), jobID.get(optimised - 1));
                db.myDao().updateJobOrder(optimised  - 1, jobID.get(optimised  - 1));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("TESTING!!", "optimizedWaypoints: " + optimisedAddresses);
    }

    private void drawPollyLines()
    {
        for (ArrayList<String> line : pollyLines) {
            PolylineOptions rectOptions = new PolylineOptions();
            for (String subline : line)
            {
                String[] latlong =  subline.split(",");
                double latitude = Double.parseDouble(latlong[0]);
                double longitude = Double.parseDouble(latlong[1]);
                rectOptions.add(new LatLng(latitude,longitude));
            }

            rectOptions.color(Color.argb(100,0,0,255));
            rectOptions.width(25);
            Polyline polyline = mMap.addPolyline(rectOptions);
        }
    }

    private ArrayList<ArrayList<String>> pollyLines = new ArrayList<>();
    private void getPollyLines() {
        JSONObject obj = null;
        try {
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

    private class JsonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(InitialMapActivity.this);
            pd.setMessage("Please wait");
            pd.setCancelable(false);
            pd.show();
        }

        protected String doInBackground(String... params) {

            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line+"\n");
                    Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)
                }
                return buffer.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (pd.isShowing()){
                pd.dismiss();
            }
            txtJson = result;
            getOptimisedRoute();
            getPollyLines();
            drawPollyLines();

            //going about this wrong, need to start new activity with order then start new map i think0
            android.support.v4.app.FragmentManager manager = getSupportFragmentManager();

            FragmentTransaction transaction = manager.beginTransaction();
            transaction.add(R.id.address_list_fragment, AddressFragment.newInstance(optimisedAddresses)).addToBackStack("tag");
            transaction.hide(manager.findFragmentById(R.id.map));
            transaction.commit();


        }
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

        // If request is cancelled, the result arrays are empty.
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //Permission Granted
            gpsTracker = new GPSTracker(this);
            if (gpsTracker.canGetLocation) {
                mMap.setMyLocationEnabled(true);
                myLocation = new LatLng(gpsTracker.getLatitude(), gpsTracker.getLongitude());
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //get locations on seperate thread
        new AsyncShowLocations().execute(pair);
    }


    private void getEndLocation()
    {
        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(InitialMapActivity.this);
        View promptsView = li.inflate(R.layout.prompt_getstart, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                InitialMapActivity.this);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText userInput = promptsView
                .findViewById(R.id.editTextDialogUserInput);

        TextView alertTxt = promptsView.findViewById(R.id.alertTxtView);
        alertTxt.setText("Use custom end location?");

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
                                    endLocation = newlatlng;
                                    MarkerOptions marker = new MarkerOptions()
                                            .position(endLocation)
                                            .title("End")
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
                                endLocation = myLocation;
                                MarkerOptions marker = new MarkerOptions()
                                        .position(endLocation)
                                        .title("End")
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

    private void getStartLocation()
    {
        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(InitialMapActivity.this);
        View promptsView = li.inflate(R.layout.prompt_getstart, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                InitialMapActivity.this);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText userInput = promptsView
                .findViewById(R.id.editTextDialogUserInput);

        TextView alertTxt = promptsView.findViewById(R.id.alertTxtView);
        alertTxt.setText("Use custom start location?");

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
                                    getEndLocation();
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
                                getEndLocation();
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

        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(InitialMapActivity.this);
            pd.setMessage("Please wait");
            pd.setCancelable(false);
            pd.show();
        }

        @Override
        protected Pair doInBackground(Pair<List<String>, List<LatLng> >... pair) {

            addresses = getAddresses();
            latLngs = getLocationFromAddress(addresses);
            while (latLngs.contains(null)) {
                latLngs.clear();
                latLngs = getLocationFromAddress(addresses);
            }
            return Pair.create(addresses, latLngs);
        }

        @Override
        protected void onPostExecute(Pair<List<String>, List<LatLng> > result) {
            super.onPostExecute(result);
            showLocations(addresses, latLngs);
            pair.first.addAll(result.first);
            pair.second.addAll(result.second);
            btnOptimise.setVisibility(View.VISIBLE);

            if (pd.isShowing()){
                pd.dismiss();
            }

            //get prompt for start location and add to map
            getStartLocation();
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

        Job[] jobs = db.myDao().getAllJobs();
        for (Job job : jobs) {
            Addresses.add(job.getAddress() + " " + job.getPostcode());
            jobID.add(job.getJobID());
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