package com.example.aashish.connect;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class MainActivity extends AppCompatActivity {

    //Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mUserRef;

    //Toolbar
    private Toolbar mToolBar;

    //Tab and ViewPager
    private ViewPager mViewPager;
    private SectionPagerAdapter mSectionPagerAdapter; //SectionPagerAdapter This is new class file created
    private TabLayout mTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Write a message to the database
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null){
            mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
        }

        //Toolbar
        mToolBar = (Toolbar)findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle("Connect");

        //Tab and ViewPager
        mViewPager = (ViewPager) findViewById(R.id.main_tabPager);
        mSectionPagerAdapter = new SectionPagerAdapter(getSupportFragmentManager());

        mViewPager.setAdapter(mSectionPagerAdapter);
        mViewPager.setCurrentItem(1); //this is used to set default tab CHAT

        mTabLayout = (TabLayout) findViewById(R.id.main_tabs);
        mTabLayout.setupWithViewPager(mViewPager);

    }

    @Override
    public void onStart() {
        super.onStart();
        //Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        //If user is not signed in then they sent to the Start Activity(for login Login)
        if(currentUser == null){
            sendToStart();
        }else {

            mUserRef.child("online").setValue("true");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null){
            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
        }
    }


    //This sent to the Start Activity
    private void sendToStart() {
        Intent start_Intent = new Intent(MainActivity.this,SigninActivity.class);
        startActivity(start_Intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()){
            case (R.id.main_logout_btn):

                //This is alert dialog for confirming logout
                new AlertDialog.Builder(this)
                        .setMessage("Are you sure want to Log Out?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                FirebaseAuth.getInstance().signOut();
                                sendToStart();
                                //FirebaseUser currentUser = mAuth.getCurrentUser();
                                //if (currentUser != null){
                                mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
                                //}
                            }})
                        .setNegativeButton(android.R.string.no, null).show();

                break;

            case (R.id.main_MyProfile_btn):
                Intent profileIntent = new Intent(MainActivity.this, MyProfileActivity.class);
                startActivity(profileIntent);
                break;

            case (R.id.main_AllUsers_btn):
                Intent usersIntent = new Intent(MainActivity.this, UsersActivity.class);
                startActivity(usersIntent);
                break;

            case (R.id.main_aboutus_btn):
                Intent aboutIntent = new Intent(MainActivity.this, AboutUsActivity.class);
                startActivity(aboutIntent);
                break;

            default:
            //case R.id.main_aboutus_btn:

        }
        return true;
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();

        //This is alert dialog for confirming Exit
        new AlertDialog.Builder(this)
                .setMessage("Are you sure want to Exit Connect?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        finish();
                    }})
                .setNegativeButton(android.R.string.no, null)
                .show();

    }
}
