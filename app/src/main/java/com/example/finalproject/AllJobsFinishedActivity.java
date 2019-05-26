package com.example.finalproject;

import android.arch.persistence.room.Room;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AllJobsFinishedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_jobs_finished);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ArrayList<Job> jobList;
        jobList = getJobList();
        final FinalListAdapter adapter = new FinalListAdapter(jobList, this);
        //handle listview and assign adapter
        ListView lView = findViewById(R.id.finalList);
        lView.setAdapter(adapter);

        TextView txtCompleted = findViewById(R.id.txt_complected);
        int numberCompleted = 0;
        for (Job job: jobList)
        {
            if (job.getStatus().equals("Complete"))
            {
                numberCompleted++;
            }
        }
        txtCompleted.setText("Delivered: " + numberCompleted + "/" + jobList.size());

        Button btn_finish = findViewById(R.id.btn_sign_out);
        btn_finish.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int pid = android.os.Process.myPid();
                android.os.Process.killProcess(pid);
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                startActivity(intent);
            }
        });
    }

    private ArrayList<Job> getJobList()
    {
        AppDatabase db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "sessionDatabase").allowMainThreadQueries().build();

        List<Job> jobs;
        jobs = Arrays.asList(db.myDao().getAllJobs());

        return new ArrayList<>(jobs);
    }
}
