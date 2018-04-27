package com.example.aashish.connect;


import android.content.Context;
import android.content.DialogInterface;
import android.icu.text.DateFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsFragment extends Fragment {

    //For main view
    private View mMainView;

    //Views components
    private RecyclerView mRequestList;

    //Firebase References
    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mUsersDatabase;
    private static DatabaseReference mRootRef;

    private FirebaseAuth mAuth;
    private static String mCurrent_Uid;

    //Variable
    static String list_user_id;


    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView = inflater.inflate(R.layout.fragment_requests, container, false);

        mRequestList = (RecyclerView) mMainView.findViewById(R.id.friend_request_list);


        //Firebase initialization
        mAuth = FirebaseAuth.getInstance();
        mCurrent_Uid = mAuth.getCurrentUser().getUid();

        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req").child(mCurrent_Uid);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mRootRef = FirebaseDatabase.getInstance().getReference();


        mRequestList.setHasFixedSize(true);
        mRequestList.setLayoutManager(new LinearLayoutManager(getContext()));


        return mMainView;

    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Friend_req, RequestViewHolder> RequestRecyclerViewAdapter = new FirebaseRecyclerAdapter<Friend_req, RequestViewHolder>(
                Friend_req.class,
                R.layout.friend_request_layout,
                RequestViewHolder.class,
                mFriendsDatabase
        ) {

            @Override
            protected void populateViewHolder(final RequestViewHolder viewHolder, final Friend_req model, int position) {

                list_user_id = getRef(position).getKey();

                mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        final String userName = dataSnapshot.child("name").getValue().toString();
                        String user_thumb_image = dataSnapshot.child("thumb_image").getValue().toString();


                        viewHolder.seName(userName);
                        viewHolder.setImage(user_thumb_image, getContext());

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        };
        mRequestList.setAdapter(RequestRecyclerViewAdapter);

    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder {

        private Button mAcceptBtn;
        private Button mRejectBtn;

        View mView;
        public RequestViewHolder(final View itemView) {
            super(itemView);
            mView = itemView;

            mAcceptBtn = (Button) mView.findViewById(R.id.req_accept_btn);
            mRejectBtn = (Button) mView.findViewById(R.id.req_reject_btn);


            mAcceptBtn.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onClick(final View v) {

                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                    Map friendsMap = new HashMap();
                    friendsMap.put("Friends/" + mCurrent_Uid + "/" + list_user_id + "/date", currentDate);
                    friendsMap.put("Friends/" + list_user_id + "/" + mCurrent_Uid + "/date", currentDate);

                    friendsMap.put("Friend_req/" + mCurrent_Uid + "/" + list_user_id, null);
                    friendsMap.put("Friend_req/" + list_user_id + "/" + mCurrent_Uid, null);

                    mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError == null){

                                Toast.makeText(mView.getContext(), "You are now Friends", Toast.LENGTH_SHORT).show();

                            }else {
                                String error = databaseError.getMessage();
                                Toast.makeText(mView.getContext(), "Error :" +error, Toast.LENGTH_LONG).show();
                            }
                        }
                    });

                }
            });
            mRejectBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //This is alert dialog for declinig request
                    new AlertDialog.Builder(mView.getContext())
                            .setMessage("Are you sure want to Decline Request?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int whichButton) {

                                    Map decliningFriendsMap = new HashMap();
                                    decliningFriendsMap.put("Friend_req/" + mCurrent_Uid + "/" + list_user_id, null);
                                    decliningFriendsMap.put("Friend_req/" + list_user_id + "/" + mCurrent_Uid, null);

                                    mRootRef.updateChildren(decliningFriendsMap, new DatabaseReference.CompletionListener() {
                                        @Override
                                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                                            if (databaseError == null){

                                                Toast.makeText(mView.getContext(), "Request Declined!!!", Toast.LENGTH_SHORT).show();

                                            }else {
                                                String error = databaseError.getMessage();
                                                Toast.makeText(mView.getContext(), "Error :" +error, Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });

                                }})
                            .setNegativeButton(android.R.string.no, null).show();

                }
            });
        }

        public void seName(String uname) {
            TextView unameEdit = (TextView) mView.findViewById(R.id.friend_request_name);
            unameEdit.setText(uname);

        }

        public void setImage(String thumb_image, Context context) {
            CircleImageView usersImageView = (CircleImageView)mView.findViewById(R.id.friend_request_image);

            Picasso.with(context).load(thumb_image).placeholder(R.drawable.default_avatar).into(usersImageView);
        }

    }

}
