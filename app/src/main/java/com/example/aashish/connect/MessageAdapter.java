package com.example.aashish.connect;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;




public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {


    private List<Messages> mMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;

    public MessageAdapter(List<Messages> mMessagesList){
        this.mMessagesList = mMessagesList;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_single_layout,parent, false);

        return new MessageViewHolder(v);
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView messageText;
        public CircleImageView profileImage;
        public RelativeLayout messageRelativeLayout;
        public TextView messageSentTime;
        public ImageView messageImage;

        public MessageViewHolder(View itemView) {
            super(itemView);

            messageText = (TextView) itemView.findViewById(R.id.message_text_layout);
            profileImage = (CircleImageView) itemView.findViewById(R.id.message_single_profile_image);
            messageRelativeLayout = (RelativeLayout) itemView.findViewById(R.id.message_single_layout);
            messageSentTime = (TextView) itemView.findViewById(R.id.message_sent_time);
            messageImage = (ImageView) itemView.findViewById(R.id.message_image_layout);

        }
    }

    @Override
    public void onBindViewHolder(final MessageAdapter.MessageViewHolder holder, int position) {

        mAuth = FirebaseAuth.getInstance();
        String current_uid = mAuth.getCurrentUser().getUid();

        Messages c = mMessagesList.get(position);

        String from_user = c.getFrom();
        Long msg_time = c.getTime();
        String message_type = c.getType();

        String time = dateFormatter(msg_time);

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(from_user);
        mUserDatabase.keepSynced(true);

        if (from_user.equals(current_uid)){

            holder.messageRelativeLayout.setGravity(Gravity.RIGHT);
            holder.profileImage.setVisibility(View.INVISIBLE);

            holder.messageText.setBackgroundResource(R.drawable.message_text_backgroud2);
            holder.messageText.setTextColor(Color.parseColor("#6c38cb"));

        }else {

            holder.messageRelativeLayout.setGravity(Gravity.LEFT);
            holder.profileImage.setVisibility(View.VISIBLE);

            holder.messageText.setBackgroundResource(R.drawable.message_text_background);
            holder.messageText.setTextColor(Color.WHITE);

            mUserDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    //Setting images
                    String image = dataSnapshot.child("thumb_image").getValue().toString();
                    Picasso.with(holder.profileImage.getContext()).load(image).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.default_avatar).into(holder.profileImage);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }
        if (message_type.equals("text")){
            holder.messageImage.setVisibility(View.INVISIBLE);
            holder.messageText.setVisibility(View.VISIBLE);
            holder.messageText.setText(c.getMessage());
            holder.messageSentTime.setText(time);
        }
        else if (message_type.equals("image")){
            holder.messageImage.setVisibility(View.VISIBLE);
            holder.messageText.setVisibility(View.INVISIBLE);
            Picasso.with(holder.messageImage.getContext()).load(c.getMessage())
                    .placeholder(R.drawable.loading_image).into(holder.messageImage);
        }

    }

    private String dateFormatter(Long msg_time) {
        SimpleDateFormat formatter = new SimpleDateFormat("h:mm a");
        String timeString = formatter.format(new Date(msg_time));
        String time = ""+timeString;
        return time;
    }

    @Override
    public int getItemCount() {
        return mMessagesList.size();
    }


}
