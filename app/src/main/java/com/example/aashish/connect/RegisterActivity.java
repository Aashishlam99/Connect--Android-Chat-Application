package com.example.aashish.connect;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "AashishLog";

    private TextInputLayout mDisplayName;
    private TextInputLayout mEmail;
    private TextInputLayout mPassword;
    private TextInputLayout mConfirmPassword;
    private Button mCreateBtn;

    //Toolbar
    private Toolbar mToolbar;

    //Progress Dialog
    private ProgressDialog mRegProgressBar;


    //Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabseRef;

    //ConnectionDetector is a user defined class which returns true if network is connected else returns false
    ConnectionDetector connectionDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Initializing activity components
        mDisplayName = (TextInputLayout) findViewById(R.id.reg_DisplayName);
        mEmail = (TextInputLayout) findViewById(R.id.reg_Email);
        mPassword = (TextInputLayout) findViewById(R.id.reg_Password);
        mConfirmPassword = (TextInputLayout) findViewById(R.id.reg_ConfirmPassword);
        mCreateBtn = (Button) findViewById(R.id.reg_Create_btn);

        //Toolbar
        mToolbar = (Toolbar) findViewById(R.id.register_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Progress Dialog
        mRegProgressBar = new ProgressDialog(this);

        //Firebase
        mAuth = FirebaseAuth.getInstance();

        //Passing context of MainActivity to the ConnectionDetector
        connectionDetector = new ConnectionDetector(this);

        mCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String display_name = mDisplayName.getEditText().getText().toString();
                String email = mEmail.getEditText().getText().toString();
                String password = mPassword.getEditText().getText().toString();
                String confirm_password = mConfirmPassword.getEditText().getText().toString();

                if (connectionDetector.isConnected()){
                    if (!TextUtils.isEmpty(display_name) || !TextUtils.isEmpty(email) || !TextUtils.isEmpty(password) || !TextUtils.isEmpty(confirm_password)){
                        if(password.equals(confirm_password)){

                            //Progress Dialog
                            mRegProgressBar.setTitle("Registering Account");
                            mRegProgressBar.setMessage("Please wait...");
                            mRegProgressBar.setCanceledOnTouchOutside(false);
                            mRegProgressBar.show();

                            register_user(display_name, email, password);

                        }else {
                            Toast.makeText(RegisterActivity.this, "Password is not matched !", Toast.LENGTH_LONG).show();
                            mPassword.getEditText().setText("");
                            mConfirmPassword.getEditText().setText("");
                            mPassword.getEditText().requestFocus();
                        }
                    }else {

                        Toast.makeText(RegisterActivity.this, "Please fill form properly", Toast.LENGTH_LONG).show();
                        mDisplayName.requestFocus();
                    }
                }
                else {
                    Toast.makeText(RegisterActivity.this, "No Internet Connection is available !", Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    private void register_user(final String display_name, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
                            String uid = current_user.getUid();

                            String deviceToken = FirebaseInstanceId.getInstance().getToken(); //getting tokenID for the user

                            mDatabseRef = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

                            //Hashmap is required to add complex values
                            HashMap<String, String> userMap = new HashMap<>();

                            userMap.put("name", display_name);
                            userMap.put("status", "Hi there! I am using Connect.");
                            userMap.put("image", "default");
                            userMap.put("thumb_image", "default");
                            userMap.put("device_token", deviceToken);

                            mDatabseRef.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        //Dissmissing Progress Dialog
                                        mRegProgressBar.dismiss();

                                        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);

                                        //Preventing returning to the Register Activity After Signing Up
                                        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(mainIntent);
                                        finish();

                                    }
                                }
                            });

                        } else {
                            //Hiding progress bar
                            mRegProgressBar.hide();
                            Toast.makeText(RegisterActivity.this, "Something went wrong!!!", Toast.LENGTH_LONG).show();
                        }

                    }
                });
    }
}
