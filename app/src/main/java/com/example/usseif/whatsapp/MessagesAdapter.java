package com.example.usseif.whatsapp;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder> {

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    private Messages previousMessage;

    private List<Messages> userMessagesList;

    public MessagesAdapter (List<Messages> userMessagesList){
        this.userMessagesList = userMessagesList;
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{
        public TextView senderMessageText, receiverMessageText, receiverTime, senderTime;
        public CircleImageView receiverProfileImage;
        public ImageView messageSenderImage, messageReceiverImage;
        public RelativeLayout senderLayout, receiverLayout;

        public MessageViewHolder(@NonNull View itemView){
            super(itemView);

            senderMessageText = itemView.findViewById(R.id.sender_message_text);
            receiverMessageText = itemView.findViewById(R.id.receiver_message_text);
            receiverProfileImage = itemView.findViewById(R.id.message_profile_image);
            messageSenderImage = itemView.findViewById(R.id.message_sender_image);
            messageReceiverImage = itemView.findViewById(R.id.message_receiver_image);
            senderTime = itemView.findViewById(R.id.sender_time);
            receiverTime = itemView.findViewById(R.id.receiver_time);
            senderLayout = itemView.findViewById(R.id.sender_layout);
            receiverLayout = itemView.findViewById(R.id.receiver_layout);

        }
    }


    @Override
    @NonNull
    public MessageViewHolder onCreateViewHolder (@NonNull ViewGroup viewGroup, int i){
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.custom_message_layout, viewGroup, false);

        mAuth = FirebaseAuth.getInstance();

        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder messageViewHolder, int i) {
        String messageSenderID = mAuth.getCurrentUser().getUid();
        Messages messages = userMessagesList.get(i);

        if (i > 0){
            previousMessage = userMessagesList.get(i - 1);
        }

        String fromUserID = messages.getFrom();
        String fromMessageType = messages.getType();

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()){
                   if (dataSnapshot.hasChild("image")){
                       String receiverImage = dataSnapshot.child("image").getValue().toString();
                       Picasso.get().load(receiverImage).placeholder(R.drawable.profile_image).into(messageViewHolder.receiverProfileImage);
                   }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //messageViewHolder.receiverMessageText.setVisibility(View.INVISIBLE);
        //messageViewHolder.senderMessageText.setVisibility(View.INVISIBLE);
        ///messageViewHolder.senderTime.setVisibility(View.INVISIBLE);
        //messageViewHolder.receiverTime.setVisibility(View.INVISIBLE);
        messageViewHolder.receiverProfileImage.setVisibility(View.INVISIBLE);
        messageViewHolder.senderLayout.setVisibility(View.INVISIBLE);
        messageViewHolder.receiverLayout.setVisibility(View.INVISIBLE);
        messageViewHolder.messageSenderImage.setVisibility(View.GONE);
        messageViewHolder.messageReceiverImage.setVisibility(View.GONE);


        if (fromMessageType.equals("text")){

            if (fromUserID.equals(messageSenderID)){
                //messageViewHolder.senderMessageText.setVisibility(View.VISIBLE);
                //messageViewHolder.senderTime.setVisibility(View.VISIBLE);
                messageViewHolder.senderLayout.setVisibility(View.VISIBLE);

                messageViewHolder.senderMessageText.setBackgroundResource(R.drawable.sender_message_layout);
                messageViewHolder.senderMessageText.setTextColor(Color.BLACK);
                messageViewHolder.senderMessageText.setText(messages.getMessage());
                messageViewHolder.senderTime.setText(messages.getTime());
            } else {
                //messageViewHolder.receiverMessageText.setVisibility(View.VISIBLE);
                //messageViewHolder.receiverTime.setVisibility(View.VISIBLE);
                messageViewHolder.receiverLayout.setVisibility(View.VISIBLE);
                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);


                messageViewHolder.receiverMessageText.setBackgroundResource(R.drawable.receiver_message_layout);
                messageViewHolder.receiverMessageText.setTextColor(Color.BLACK);
                messageViewHolder.receiverMessageText.setText(messages.getMessage());
                messageViewHolder.receiverTime.setText(messages.getTime());

            }
        } else if (fromMessageType.equals("image")){
            if (fromUserID.equals(messageSenderID)){
                messageViewHolder.messageSenderImage.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).into(messageViewHolder.messageSenderImage);
            } else {
                messageViewHolder.messageReceiverImage.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).into(messageViewHolder.messageReceiverImage);
                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
            }
        }

    }


    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }
}
