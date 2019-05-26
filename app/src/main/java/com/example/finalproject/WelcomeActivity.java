package com.example.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button button = (Button) findViewById(R.id.button_continue);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent myIntent = new Intent(WelcomeActivity.this, DownloadManifestsActivity.class);
                startActivity(myIntent);
            }
        });

        String welcome = getWelcomeMessage();
        TextView txtWelcome = findViewById(R.id.txt_welcome);
        txtWelcome.setText(welcome);
    }

    private String getWelcomeMessage()
    {
        //This is where we get our welcome message
        //normally we download this from server
        String msg = "Welcome to the app demonstration! This would normally be the place where we " +
                "download a message from the server. \nFor this demonstration we will just " +
                "use this text.";
        return msg;
    }

}
