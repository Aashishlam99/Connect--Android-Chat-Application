package com.example.aashish.connect;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

public class AboutUsActivity extends AppCompatActivity {

    //Toolbar
    private Toolbar mProfileToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        //Toolbar
        mProfileToolbar = (Toolbar)findViewById(R.id.about_us_toolbar);
        setSupportActionBar(mProfileToolbar);
        getSupportActionBar().setTitle("About Us");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);



    }
}
