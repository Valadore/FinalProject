package com.example.finalproject;

import android.arch.persistence.room.Room;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class OptimisedListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_optimised_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        final String txtJson = Objects.requireNonNull(intent.getExtras()).getString("jsonText");
        final LatLng startLocation = (LatLng) Objects.requireNonNull(intent.getExtras()).get("startLocation");
        final LatLng endLocation = (LatLng) Objects.requireNonNull(intent.getExtras()).get("endLocation");

        ArrayList<Job> optimisedJobs = getOptimisedList();
        ArrayList<String> stringList = new ArrayList<>();

        for (Job job : optimisedJobs)
        {
            stringList.add(job.getAddress() + " " + job.getPostcode());
        }

        final AddressListAdapter adapter = new AddressListAdapter(stringList, this);

        //handle listview and assign adapter
        ListView lView = findViewById(R.id.address_list);
        lView.setAdapter(adapter);

        Button btnContinue;
        btnContinue = findViewById(R.id.button_continue);
        btnContinue.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent myIntent = new Intent(OptimisedListActivity.this, BeginRouteActivity.class);
                myIntent.putExtra("jsonText", txtJson);
                myIntent.putExtra("startLocation", startLocation);
                myIntent.putExtra("endLocation", endLocation);
                startActivity(myIntent);
            }
            });
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
