package com.example.aashish.connect;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class MyProfileActivity extends AppCompatActivity {

    //Toolbar
    private Toolbar mProfileToolbar;

    //Firebase
    private DatabaseReference mUserRef;
    private FirebaseUser mCurrentUser;
    //Firebase Storage refernce to store Display Picture
    private StorageReference mStorageRef;

    //xml layouts
    private CircleImageView mImage;
    private TextView mDisplayName;
    private TextView mStatus;

    private Button mChangeDPBtn;
    private Button mChangeStatusBtn;

    //Progress Dialog for Profile Activity Loading
    private ProgressDialog mProfileProgress;
    //Progress Dialog for Uploading Image
    private ProgressDialog mUploadProgress;

    //Variable for starting gallery intent
    private static final int GALLERY_PICK = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);

        //Layout
        mImage = (CircleImageView)findViewById(R.id.profileImage);
        mDisplayName = (TextView)findViewById(R.id.profile_DisplayName);
        mStatus = (TextView)findViewById(R.id.profile_Status);
        mChangeStatusBtn = (Button)findViewById(R.id.profile_ChangeStatus_btn);
        mChangeDPBtn = (Button)findViewById(R.id.profile_ChangeDP_btn);

        //Toolbar
        mProfileToolbar = (Toolbar)findViewById(R.id.profileToolbar);
        setSupportActionBar(mProfileToolbar);
        getSupportActionBar().setTitle("My Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Progress Dailog to Load User Data
        mProfileProgress = new  ProgressDialog(MyProfileActivity.this);
        mProfileProgress.setTitle("Loading...");
        mProfileProgress.setMessage("Please Wait");
        mProfileProgress.setCanceledOnTouchOutside(false);
        mProfileProgress.show();

        //Firebase
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = mCurrentUser.getUid();
        mStorageRef = FirebaseStorage.getInstance().getReference();


        mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);
        mUserRef.keepSynced(true); //Firebase Offline Capability

        mUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString(); //It gets image url from the firebase
                final String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                mDisplayName.setText(name);
                mStatus.setText(status);

                if (!image.equals("default")){

                    //Below code is necessory for storing images offline
                    //Picasso is the library which gets pictures from given url
                    //to use this line of code first you have to add dependency == compile 'com.squareup.picasso:picasso:2.5.2'
                    Picasso.with(MyProfileActivity.this).load(thumb_image).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.default_avatar).into(mImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Picasso.with(MyProfileActivity.this).load(thumb_image).placeholder(R.drawable.default_avatar).into(mImage);
                        }
                    });

                }

                mProfileProgress.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        //Calling Status Activity
        mChangeStatusBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent statusIntent = new Intent(MyProfileActivity.this, StatusActivity.class);
                String status_value = mStatus.getText().toString();
                //Passing status value to the status activity
                statusIntent.putExtra("status_value", status_value);
                startActivity(statusIntent);
            }
        });

        //Calling Gallery intent to pick images from gallery
        mChangeDPBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent galleyIntent = new Intent();
                galleyIntent.setType("image/*");
                galleyIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(galleyIntent, "SELECT IMAGE"), GALLERY_PICK);

                /*This code opens various application for image picker
                // start picker to get image for cropping and then use the image in cropping activity
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(this);
                */

            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK){

            Uri imageUri = data.getData();

            CropImage.activity(imageUri)
                    .setAspectRatio(1, 1)
                    .setMinCropWindowSize(20, 20)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);  //It sores reault to the result variable
            if (resultCode == RESULT_OK) {
                //Progress Dialog
                mUploadProgress = new ProgressDialog(MyProfileActivity.this);
                mUploadProgress.setTitle("Uploading an Image");
                mUploadProgress.setMessage("Please Wait...");
                mUploadProgress.setCanceledOnTouchOutside(false);
                mUploadProgress.show();

                Uri resultUri = result.getUri(); //gets uri of the image

                File thumb_filePath = new File(resultUri.getPath());

                String u_id = mCurrentUser.getUid();

                //Now lets compress image into bitmap images
                Bitmap thumb_bitmap = null; //Used from dependencies
                try {
                    thumb_bitmap = new Compressor(this)
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(50)
                            .compressToBitmap(thumb_filePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //Now uploading bitmap images into firebase databse storage
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                final byte[] thumb_byte = baos.toByteArray();



                StorageReference filepath = mStorageRef.child("profile_images").child(u_id + ".jpg"); //Defining path for the Image
                final StorageReference thumb_filepath = mStorageRef.child("profile_images").child("thumbs").child(u_id + ".jpg");

                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if (task.isSuccessful()){

                            @SuppressWarnings("VisibleForTests")
                            final String download_url = task.getResult().getDownloadUrl().toString();

                            UploadTask uploadTask = thumb_filepath.putBytes(thumb_byte);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {

                                    @SuppressWarnings("VisibleForTests")
                                    String thumb_download_url = thumb_task.getResult().getDownloadUrl().toString();

                                    if (thumb_task.isSuccessful()){

                                        Map update_Map = new HashMap();
                                        update_Map.put("image", download_url);
                                        update_Map.put("thumb_image", thumb_download_url);

                                        mUserRef.updateChildren(update_Map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if (task.isSuccessful()){
                                                    mUploadProgress.dismiss();
                                                    Toast.makeText(MyProfileActivity.this, "Display Picture Changed Successfully", Toast.LENGTH_LONG).show();
                                                }else {
                                                    Toast.makeText(MyProfileActivity.this, "Error", Toast.LENGTH_LONG).show();
                                                }

                                            }
                                        });

                                    }
                                    else {

                                        Toast.makeText(MyProfileActivity.this, "Error uploading thumbnail!!!", Toast.LENGTH_LONG).show();
                                        mUploadProgress.dismiss();

                                    }
                                }
                            });

                        }else {

                            Toast.makeText(MyProfileActivity.this, "Error uploading Display Picture!!!", Toast.LENGTH_LONG).show();
                            mUploadProgress.dismiss();
                        }

                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }

}
