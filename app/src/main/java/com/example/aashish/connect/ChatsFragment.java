package com.example.aashish.connect;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment {

    private RecyclerView mChatList;

    private DatabaseReference mChatRef;
    private DatabaseReference mUsersDatabase;
    private FirebaseAuth mAuth;

    private String mCurrent_Uid;

    private View mMainView;

    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView = inflater.inflate(R.layout.fragment_chats, container, false);

        mChatList = (RecyclerView) mMainView.findViewById(R.id.chat_list);

        mAuth = FirebaseAuth.getInstance();
        mCurrent_Uid = mAuth.getCurrentUser().getUid();

        mChatRef = FirebaseDatabase.getInstance().getReference().child("Chat").child(mCurrent_Uid);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        mChatList.setHasFixedSize(true);
        mChatList.setLayoutManager(new LinearLayoutManager(getContext()));

        return mMainView;

    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<ChatList, ChatViewHolder> chatRecyclerViewAdapter = new FirebaseRecyclerAdapter<ChatList, ChatViewHolder>(
                ChatList.class,
                R.layout.users_single_layout,
                ChatViewHolder.class,
                mChatRef
        ) {
            @Override
            protected void populateViewHolder(final ChatViewHolder viewHolder, ChatList model, int position) {

                final String list_user_id = getRef(position).getKey();

                mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String name = dataSnapshot.child("name").getValue().toString();

                        String image_url = dataSnapshot.child("thumb_image").getValue().toString();
                        String status = dataSnapshot.child("status").getValue().toString();

                        if (dataSnapshot.hasChild("online")){
                            String userOnline = dataSnapshot.child("online").getValue().toString();
                            viewHolder.setUserOnline(userOnline);
                        }
                        viewHolder.setName(name);
                        viewHolder.setImage(image_url, getContext());
                        viewHolder.setStatus(status);

                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                chatIntent.putExtra("user_id", list_user_id);
                                chatIntent.putExtra("user_name", name);
                                startActivity(chatIntent);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

        };
        mChatList.setAdapter(chatRecyclerViewAdapter);
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {

        View mView;
        public ChatViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }
        public void setName(String display_name){
            TextView userName = (TextView) mView.findViewById(R.id.user_single_name);
            userName.setText(display_name);
        }
        public void setImage(String img_url, Context ctx){

            CircleImageView circleImageView = (CircleImageView) mView.findViewById(R.id.user_single_image);

            Picasso.with(ctx).load(img_url).placeholder(R.drawable.default_avatar).into(circleImageView);
        }
        public void setUserOnline(String online_status){
            ImageView userOnlineView = (ImageView) mView.findViewById(R.id.user_single_online_icon);
            if (online_status.equals("true")){
                userOnlineView.setVisibility(View.VISIBLE);
            }
            else {
                userOnlineView.setVisibility(View.INVISIBLE);
            }
        }

        public void setStatus(String s) {
            TextView statusEdit = (TextView) mView.findViewById(R.id.user_single_status);
            statusEdit.setText(s);
        }

    }
}
