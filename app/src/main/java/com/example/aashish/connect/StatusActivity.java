package com.example.aashish.connect;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity {

    //Toolbar
    private Toolbar mToolbar;

    //Layout
    private TextInputLayout mStatus;
    private Button mSaveBtn;

    //Firebase
    private DatabaseReference mStatusDatabse;
    private FirebaseUser mcurrentUser;

    //Progress Dialog
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        //Layout
        mStatus = (TextInputLayout)findViewById(R.id.status_input);
        mSaveBtn = (Button)findViewById(R.id.status_Save_btn);

        //Toolbar
        mToolbar = (Toolbar)findViewById(R.id.status_Toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Firebase
        mcurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = mcurrentUser.getUid();

        //getting passed value from profile activity
        String status_value = getIntent().getStringExtra("status_value");
        mStatus.getEditText().setText(status_value);

        mStatusDatabse = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);


        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Progress Dailog
                mProgressDialog = new  ProgressDialog(StatusActivity.this);

                mProgressDialog.setTitle("Saving Changes");
                mProgressDialog.setMessage("Please wait...");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();

                final String status = mStatus.getEditText().getText().toString();

                mStatusDatabse.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            mProgressDialog.dismiss();

                            Intent profileIntent = new Intent(StatusActivity.this, MyProfileActivity.class);
                            //profileIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(profileIntent);
                        }
                        else {
                            Toast.makeText(StatusActivity.this, "Error in saving changes!", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });


    }

    /*@Override
    protected void onStart() {
        super.onStart();
        mStatusDatabse.child("online").setValue(true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mStatusDatabse.child("online").setValue(false);
    }*/
}