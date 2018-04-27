package com.example.aashish.connect;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.icu.text.DateFormat;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class UsersProfileActivity extends AppCompatActivity {

    private ImageView mProfileImage;
    private TextView mProfileName, mProfileStatus, mProfileFriendsCount;
    private Button mProfileSendReqBtn, mDeclineBtn;

    //Firebase Database Reference
    private DatabaseReference mRootRef;
    private DatabaseReference mUsersDatabase;
    //private DatabaseReference mUserRef;//This reference is only to set active to the current user

    private DatabaseReference mFriendReqDatabase;

    private DatabaseReference mFriendDatabase;
    private DatabaseReference mNotificationDatabase;
    private FirebaseUser mCurrentUser;

    //Progress dialog
    ProgressDialog mProgressDialog;

    private String mCurrent_state;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_profile);

        //Layout
        mProfileImage = (ImageView)findViewById(R.id.users_profile_image);
        mProfileName = (TextView)findViewById(R.id.users_DisplayName);
        mProfileStatus = (TextView)findViewById(R.id.users_status);
        mProfileFriendsCount = (TextView)findViewById(R.id.users_friends_number);
        mProfileSendReqBtn = (Button)findViewById(R.id.users_request_btn);
        mDeclineBtn = (Button)findViewById(R.id.users_decline_btn);

        //Progress Dailog to Load User Data
        mProgressDialog = new ProgressDialog(UsersProfileActivity.this);
        mProgressDialog.setTitle("Loading User Details");
        mProgressDialog.setMessage("Please Wait...");
        mProgressDialog.setCanceledOnTouchOutside(false);


        final String user_id = getIntent().getStringExtra("user_id");

        //Referencing firebase database to the given user id
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mUsersDatabase.keepSynced(true);//For offline capability

        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");

        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");

        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("notifications");
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        //mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid());

        mProgressDialog.show();

        mDeclineBtn.setVisibility(View.INVISIBLE);
        mDeclineBtn.setEnabled(false);

        mCurrent_state = "not_friends";


        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //Getting values from the firebase database
                String display_name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();

                mProfileName.setText(display_name);
                mProfileStatus.setText(status);

                if (!image.equals("default")){
                    //Picasso.with(UsersProfileActivity.this).load(image).placeholder(R.drawable.default_avatar2).into(mProfileImage); //Picasso is the library which gets pictures from given url
                    //to use this line of code first you have to add dependency == compile 'com.squareup.picasso:picasso:2.5.2'

                    //Below code is necessory for storing images offline
                    //Picasso is the library which gets pictures from given url
                    //to use this line of code first you have to add dependency == compile 'com.squareup.picasso:picasso:2.5.2'
                    Picasso.with(UsersProfileActivity.this).load(image).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.default_avatar).into(mProfileImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Picasso.with(UsersProfileActivity.this).load(image).placeholder(R.drawable.default_avatar2).into(mProfileImage);
                        }
                    });


                }

                //---------------------FRIEND LIST/REQUEST FEATURE--------------
                mFriendReqDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(user_id)){

                            String req_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();
                            if (req_type.equals("received")){

                                mCurrent_state = "req_received";
                                mProfileSendReqBtn.setText("Accept Friend Request");

                                mDeclineBtn.setVisibility(View.VISIBLE);
                                mDeclineBtn.setEnabled(true);
                            }else if (req_type.equals("sent")){

                                mCurrent_state = "req_sent";
                                mProfileSendReqBtn.setText("Cancel Friend Request");

                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);
                            }
                            mProgressDialog.dismiss();
                        } else {
                            mFriendDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(user_id)){
                                        mCurrent_state = "friends";
                                        mProfileSendReqBtn.setText("Unfriend");

                                        mDeclineBtn.setVisibility(View.INVISIBLE);
                                        mDeclineBtn.setEnabled(false);
                                    }
                                    mProgressDialog.dismiss();
                                }
                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    mProgressDialog.dismiss();
                                }
                            });
                        }

                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
                mProgressDialog.dismiss();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        mProfileSendReqBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {

                mProfileSendReqBtn.setEnabled(false);

                //-------------------- NOT FRIEND STATE------------------

                if (mCurrent_state.equals("not_friends")){

                    DatabaseReference newNotificationRef = mRootRef.child("notifications").child(user_id).push();
                    String newNotificationId = newNotificationRef.getKey();

                    HashMap<String, String> notificationData = new HashMap<>();
                    notificationData.put("from", mCurrentUser.getUid());
                    notificationData.put("type", "request");

                    Map requestMap = new HashMap();
                    requestMap.put("Friend_req/" + mCurrentUser.getUid() + "/" + user_id + "/request_type", "sent");
                    requestMap.put("Friend_req/" + user_id + "/" + mCurrentUser.getUid() + "/request_type", "received");
                    requestMap.put("notifications/" + user_id + "/" + newNotificationId, notificationData);

                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError != null){
                                String error = databaseError.getMessage();
                                Toast.makeText(UsersProfileActivity.this, "Error :" +error, Toast.LENGTH_LONG).show();

                            }
                            mProfileSendReqBtn.setEnabled(true);
                            mCurrent_state = "req_sent";
                            mProfileSendReqBtn.setText("Cancel Friend Request");
                        }
                    });
                }

                //-------------------- CANCEL FRIEND STATE-----------------

                if (mCurrent_state.equals("req_sent")){

                    mFriendReqDatabase.child(mCurrentUser.getUid()).child(user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){

                                mFriendReqDatabase.child(user_id).child(mCurrentUser.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if (task.isSuccessful()){

                                            mProfileSendReqBtn.setEnabled(true);
                                            mCurrent_state = "not_friends";
                                            mProfileSendReqBtn.setText("Send Friend Request");

                                            mDeclineBtn.setVisibility(View.INVISIBLE);
                                            mDeclineBtn.setEnabled(false);
                                        }else {
                                            Toast.makeText(UsersProfileActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                            else {
                                Toast.makeText(UsersProfileActivity.this, "Error", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

                //--------------REQUEST RECEIVED STATE---------------(Accepting Friend Request)
                if (mCurrent_state.equals("req_received")){
                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                    Map friendsMap = new HashMap();
                    friendsMap.put("Friends/" + mCurrentUser.getUid() + "/" + user_id + "/date", currentDate);
                    friendsMap.put("Friends/" + user_id + "/" + mCurrentUser.getUid() + "/date", currentDate);

                    friendsMap.put("Friend_req/" + mCurrentUser.getUid() + "/" + user_id, null);
                    friendsMap.put("Friend_req/" + user_id + "/" + mCurrentUser.getUid(), null);

                    mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError == null){
                                mProfileSendReqBtn.setEnabled(true);
                                mCurrent_state = "friends";
                                mProfileSendReqBtn.setText("Unfriend");

                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);

                            }else {
                                String error = databaseError.getMessage();
                                Toast.makeText(UsersProfileActivity.this, "Error :" +error, Toast.LENGTH_LONG).show();
                            }
                        }
                    });

                }

                //---------------UNFRIENDING USERS STATE---------------------
                if (mCurrent_state.equals("friends")){

                    //This is alert dialog for confirming unfriend
                    new AlertDialog.Builder(UsersProfileActivity.this)
                            .setMessage("Do you really want to UNFRIEND")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int whichButton) {
                                    Map unFriendsMap = new HashMap();
                                    //Removing user from the friendlist
                                    unFriendsMap.put("Friends/" + mCurrentUser.getUid() + "/" + user_id, null);
                                    unFriendsMap.put("Friends/" + user_id + "/" + mCurrentUser.getUid(), null);
                                    //Deleting user history of chatting
                                    unFriendsMap.put("Chat/" + mCurrentUser.getUid() + "/" + user_id, null);
                                    unFriendsMap.put("Chat/" + user_id + "/" + mCurrentUser.getUid(), null);
                                    //Deleting All messages from the database
                                    //unFriendsMap.put("messages/" + mCurrentUser.getUid() + "/" + user_id, null);
                                    //unFriendsMap.put("messages/" + user_id + "/" + mCurrentUser.getUid(), null);


                                    mRootRef.updateChildren(unFriendsMap, new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                            if (databaseError == null){

                                                mCurrent_state = "not_friends";
                                                mProfileSendReqBtn.setText("SEND FRIEND REQUEST");

                                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                                mDeclineBtn.setEnabled(false);

                                            }else {
                                                String error = databaseError.getMessage();
                                                Toast.makeText(UsersProfileActivity.this, "Error :" +error, Toast.LENGTH_LONG).show();
                                            }
                                            mProfileSendReqBtn.setEnabled(true);

                                        }
                                    });
                                }})
                            .setNegativeButton(android.R.string.no, null).show();


                }

                //---------------DECLINING FRIEND REQUEST---------------------
                if (mCurrent_state.equals("req_received")){

                    //This is alert dialog for declinig request
                    new AlertDialog.Builder(UsersProfileActivity.this)
                            .setMessage("Are you sure want to Decline Request?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int whichButton) {

                                    Map decliningFriendsMap = new HashMap();
                                    decliningFriendsMap.put("Friend_req/" + mCurrentUser.getUid() + "/" + user_id, null);
                                    decliningFriendsMap.put("Friend_req/" + user_id + "/" + mCurrentUser.getUid(), null);

                                    mRootRef.updateChildren(decliningFriendsMap, new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                            if (databaseError == null){

                                                Toast.makeText(UsersProfileActivity.this, "Request Declined!!!", Toast.LENGTH_SHORT).show();

                                                mCurrent_state = "not_friends";
                                                mProfileSendReqBtn.setText("SEND FRIEND REQUEST");

                                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                                mDeclineBtn.setEnabled(false);
                                            }else {
                                                String error = databaseError.getMessage();
                                                Toast.makeText(UsersProfileActivity.this, "Error :" +error, Toast.LENGTH_LONG).show();
                                            }
                                            mProfileSendReqBtn.setEnabled(true);
                                        }
                                    });

                                }})
                            .setNegativeButton(android.R.string.no, null).show();

                }

            }
        });

    }
}
