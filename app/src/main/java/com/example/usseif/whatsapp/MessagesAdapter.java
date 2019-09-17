package com.example.usseif.whatsapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

    /* Initialize The Fields */
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


    /* Relate With the Layout View custom_message_layout */
    @Override
    @NonNull
    public MessageViewHolder onCreateViewHolder (@NonNull ViewGroup viewGroup, int i){
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.custom_message_layout, viewGroup, false);

        mAuth = FirebaseAuth.getInstance();

        return new MessageViewHolder(view);
    }

    /* Bind The Data with The Layout */
    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder messageViewHolder, final int position) {
        String messageSenderID = mAuth.getCurrentUser().getUid();
        Messages messages = userMessagesList.get(position);

        if (position > 0){
            previousMessage = userMessagesList.get(position - 1);
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


        /* Send Messages */
        if (fromMessageType.equals("text")){  // TEXT Message

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
        } else if (fromMessageType.equals("image")){  // IMAGE Message
            if (fromUserID.equals(messageSenderID)){
                messageViewHolder.messageSenderImage.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).into(messageViewHolder.messageSenderImage);
            } else {
                messageViewHolder.messageReceiverImage.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).into(messageViewHolder.messageReceiverImage);
                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
            }
            messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(messageViewHolder.itemView.getContext(), ImageViewerActivity.class);
                    intent.putExtra("url", userMessagesList.get(position).getMessage());
                    messageViewHolder.itemView.getContext().startActivity(intent);
                }
            });
        } else if (fromMessageType.equals("pdf") || fromMessageType.equals("docx")) {   // DOCUMENT Message
            if (fromUserID.equals(messageSenderID)){
                messageViewHolder.messageSenderImage.setVisibility(View.VISIBLE);
                //messageViewHolder.messageSenderImage.setBackgroundResource(R.drawable.file);
                Picasso.get()
                        .load("https://firebasestorage.googleapis.com/v0/b/whatsapp-cdced.appspot.com/o/Document%20Files%2Ffile.png?alt=media&token=57f8f16f-1ecc-4515-965b-ab651270aa5d")
                        .into(messageViewHolder.messageSenderImage);

                messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                        messageViewHolder.itemView.getContext().startActivity(intent);
                    }
                });

            } else {
                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                messageViewHolder.messageReceiverImage.setVisibility(View.VISIBLE);
                //messageViewHolder.messageReceiverImage.setBackgroundResource(R.drawable.file);
                Picasso.get()
                        .load("https://firebasestorage.googleapis.com/v0/b/whatsapp-cdced.appspot.com/o/Document%20Files%2Ffile.png?alt=media&token=57f8f16f-1ecc-4515-965b-ab651270aa5d")
                        .into(messageViewHolder.messageReceiverImage);

                messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                        messageViewHolder.itemView.getContext().startActivity(intent);
                    }
                });

            }
        }

        /* Delete Messages */
        if (fromUserID.equals(messageSenderID)) {
            messageViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    CharSequence options[] = new CharSequence[]
                            {
                                    "Delete For Me",
                                    "Delete For EveryOne"
                            };
                    AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                    builder.setTitle("Delete Message ?!");
                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int i) {
                            if (i == 0){
                                deleteSentMessages(position, messageViewHolder);

                                Intent intent = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
                                messageViewHolder.itemView.getContext().startActivity(intent);
                            } else if (i == 1){
                                deleteMessagesForEveryOne(position, messageViewHolder);

                                Intent intent = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
                                messageViewHolder.itemView.getContext().startActivity(intent);
                            }
                        }
                    });
                    builder.show();
                    return false;
                }
            });
        } else {
            messageViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    CharSequence options[] = new CharSequence[]
                            {
                                    "Delete For Me"
                            };
                    AlertDialog.Builder builder = new AlertDialog.Builder(messageViewHolder.itemView.getContext());
                    builder.setTitle("Delete Message ?!");
                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int i) {
                            if (i == 0){
                                deleteReceivedMessages(position, messageViewHolder);

                                Intent intent = new Intent(messageViewHolder.itemView.getContext(), MainActivity.class);
                                messageViewHolder.itemView.getContext().startActivity(intent);
                            }
                        }
                    });
                    builder.show();
                    return false;
                }
            });
        }

    }


    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }


    private void deleteSentMessages(final int position, final MessageViewHolder messageViewHolder){
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getMessageID())
                //.child(userMessagesList.get(position).getMessage())
                //.setValue("You Deleted This Message").addOnCompleteListener(new OnCompleteListener<Void>() {
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(messageViewHolder.itemView.getContext(), "Message Deleted Successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(messageViewHolder.itemView.getContext(), "Error Occurred..", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void deleteReceivedMessages(final int position, final MessageViewHolder messageViewHolder){
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(messageViewHolder.itemView.getContext(), "Message Deleted Successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(messageViewHolder.itemView.getContext(), "Error Occurred..", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void deleteMessagesForEveryOne(final int position, final MessageViewHolder messageViewHolder){
        final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    rootRef.child("Messages")
                            .child(userMessagesList.get(position).getTo())
                            .child(userMessagesList.get(position).getFrom())
                            .child(userMessagesList.get(position).getMessageID())
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(messageViewHolder.itemView.getContext(), "Message Deleted Successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(messageViewHolder.itemView.getContext(), "Error Occurred..", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    Toast.makeText(messageViewHolder.itemView.getContext(), "Error Occurred..", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }



}
