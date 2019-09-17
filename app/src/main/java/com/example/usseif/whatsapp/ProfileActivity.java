package com.example.usseif.whatsapp;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private String receiverUserID, senderUserID, currentState;

    private DatabaseReference userRef, chatRequestRef, contactsRef, notificationsRef;
    private FirebaseAuth mAuth;

    private ProgressDialog loadingBar;

    private CircleImageView userProfileImage;
    private TextView userProfileName, userProfileStatus;
    private Button sendMessageRequestButton, declineMessageRequestButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        chatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        notificationsRef = FirebaseDatabase.getInstance().getReference().child("Notifications");

        receiverUserID = getIntent().getExtras().getString("Visit_User_ID").toString();
        senderUserID = mAuth.getCurrentUser().getUid();

        InitializeFields();

        RetrieveUserInfo();

    }

    private void InitializeFields(){
        userProfileImage = findViewById(R.id.visit_profile_image);
        userProfileName = findViewById(R.id.visit_username);
        userProfileStatus = findViewById(R.id.visit_status);
        sendMessageRequestButton = findViewById(R.id.send_message_request_button);
        declineMessageRequestButton = findViewById(R.id.decline_message_request_button);
        currentState = "New";
        loadingBar = new ProgressDialog(this);
    }

    private void RetrieveUserInfo(){
        userRef.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && (dataSnapshot.hasChild("image"))){
                    String username = dataSnapshot.child("name").getValue().toString();
                    String userstatus = dataSnapshot.child("status").getValue().toString();
                    String userimage = dataSnapshot.child("image").getValue().toString();

                    Picasso.get().load(userimage).placeholder(R.drawable.profile_image).into(userProfileImage);
                    userProfileName.setText(username);
                    userProfileStatus.setText(userstatus);
                    ManageChatRequest();
                } else {
                    String username = dataSnapshot.child("name").getValue().toString();
                    String userstatus = dataSnapshot.child("status").getValue().toString();

                    userProfileName.setText(username);
                    userProfileStatus.setText(userstatus);
                    ManageChatRequest();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    //chatRequestRef
    private void ManageChatRequest(){
        chatRequestRef.child(senderUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(receiverUserID)){
                    String requestType = dataSnapshot.child(receiverUserID).child("request_type").getValue().toString();

                    if (requestType.equals("sent")){
                        currentState = "request_sent";
                        sendMessageRequestButton.setText("Cancel Chat Request");
                    } else if (requestType.equals("received")){
                        currentState = "request_received";
                        sendMessageRequestButton.setText("Accept Chat Request");

                        declineMessageRequestButton.setVisibility(View.VISIBLE);
                        declineMessageRequestButton.setEnabled(true);
                        declineMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                CancelChatRequest();
                            }
                        });
                    }
                } else {
                    contactsRef.child(senderUserID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(receiverUserID)){
                               currentState = "friends";
                               sendMessageRequestButton.setText("Remove This Contact");
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if (!senderUserID.equals(receiverUserID)){
            sendMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendMessageRequestButton.setEnabled(false);
                    if (currentState.equals("New")){
                        SendChatRequest();
                    }
                    if (currentState.equals("request_sent")){
                        CancelChatRequest();
                    }
                    if (currentState.equals("request_received")){
                        AcceptChatRequest();
                    }
                    if (currentState.equals("friends")){
                        RemoveContact();
                    }
                }
            });
        } else {
            sendMessageRequestButton.setVisibility(View.INVISIBLE);
            declineMessageRequestButton.setVisibility(View.INVISIBLE);
        }
    }

    //chatRequestRef & notificationsRef
    private void SendChatRequest(){
        loadingBar.setTitle("Sending Chat Request");
        loadingBar.setMessage("Please Wait, While Sending Your Request");
        loadingBar.setCanceledOnTouchOutside(true);
        loadingBar.show();
        chatRequestRef.child(senderUserID).child(receiverUserID).child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    chatRequestRef.child(receiverUserID).child(senderUserID).child("request_type").setValue("received")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        HashMap<String, String> chatNotificationsMap = new HashMap<>();
                                        chatNotificationsMap.put("from", senderUserID);
                                        chatNotificationsMap.put("type", "request");

                                        notificationsRef.child(receiverUserID).push().setValue(chatNotificationsMap)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()){
                                                            sendMessageRequestButton.setEnabled(true);
                                                            currentState = "request_sent";
                                                            sendMessageRequestButton.setText("Cancel Chat Request");
                                                            loadingBar.dismiss();
                                                        } else {
                                                            String message = task.getException().toString();
                                                            Toast.makeText(ProfileActivity.this, "Error " + message, Toast.LENGTH_SHORT).show();
                                                            loadingBar.dismiss();
                                                        }
                                                    }
                                                });


                                    } else {
                                        String message = task.getException().toString();
                                        Toast.makeText(ProfileActivity.this, "Error " + message, Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    }
                                }
                            });
                } else {
                    String message = task.getException().toString();
                    Toast.makeText(ProfileActivity.this, "Error " + message, Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }
            }
        });
    }

    //chatRequestRef
    private void CancelChatRequest(){
        loadingBar.setTitle("Canceling Chat Request");
        loadingBar.setMessage("Please Wait, While Cancelling Your Request");
        loadingBar.setCanceledOnTouchOutside(true);
        loadingBar.show();
        chatRequestRef.child(senderUserID).child(receiverUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    chatRequestRef.child(receiverUserID).child(senderUserID).removeValue()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                sendMessageRequestButton.setEnabled(true);
                                currentState = "New";
                                sendMessageRequestButton.setText("Send Message");

                                declineMessageRequestButton.setVisibility(View.INVISIBLE);
                                declineMessageRequestButton.setEnabled(false);
                                loadingBar.dismiss();
                            } else {
                                String message = task.getException().toString();
                                Toast.makeText(ProfileActivity.this, "Error " + message, Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
                } else {
                    String message = task.getException().toString();
                    Toast.makeText(ProfileActivity.this, "Error " + message, Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }
            }
        });
    }

    //contactsRef
    private void AcceptChatRequest(){
        loadingBar.setTitle("Accepting Chat Request");
        loadingBar.setMessage("Please Wait, While Accepting Your Request");
        loadingBar.setCanceledOnTouchOutside(true);
        loadingBar.show();
        contactsRef.child(senderUserID).child(receiverUserID).child("Contacts").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    contactsRef.child(receiverUserID).child(senderUserID).child("Contacts").setValue("Saved")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        chatRequestRef.child(senderUserID).child(receiverUserID)
                                                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()){
                                                    chatRequestRef.child(receiverUserID).child(senderUserID)
                                                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()){
                                                                sendMessageRequestButton.setEnabled(true);
                                                                currentState = "friends";
                                                                sendMessageRequestButton.setText("Remove This Contact");

                                                                declineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                                declineMessageRequestButton.setEnabled(false);
                                                                loadingBar.dismiss();
                                                            } else {
                                                                String message = task.getException().toString();
                                                                Toast.makeText(ProfileActivity.this, "Error " + message, Toast.LENGTH_SHORT).show();
                                                                loadingBar.dismiss();
                                                            }
                                                        }
                                                    });
                                                } else {
                                                    String message = task.getException().toString();
                                                    Toast.makeText(ProfileActivity.this, "Error " + message, Toast.LENGTH_SHORT).show();
                                                    loadingBar.dismiss();
                                                }
                                            }
                                        });
                                    } else {
                                        String message = task.getException().toString();
                                        Toast.makeText(ProfileActivity.this, "Error " + message, Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    }
                                }
                            });
                } else {
                    String message = task.getException().toString();
                    Toast.makeText(ProfileActivity.this, "Error " + message, Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }
            }
        });
    }

    //contactsRef
    private void RemoveContact(){
        loadingBar.setTitle("Removing The Contact");
        loadingBar.setMessage("Please Wait, While Removing The Contact");
        loadingBar.setCanceledOnTouchOutside(true);
        loadingBar.show();
        contactsRef.child(senderUserID).child(receiverUserID).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    contactsRef.child(receiverUserID).child(senderUserID).removeValue()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        sendMessageRequestButton.setEnabled(true);
                                        currentState = "New";
                                        sendMessageRequestButton.setText("Send Message");

                                        declineMessageRequestButton.setVisibility(View.INVISIBLE);
                                        declineMessageRequestButton.setEnabled(false);
                                        loadingBar.dismiss();
                                    } else {
                                        String message = task.getException().toString();
                                        Toast.makeText(ProfileActivity.this, "Error " + message, Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    }
                                }
                            });
                } else {
                    String message = task.getException().toString();
                    Toast.makeText(ProfileActivity.this, "Error " + message, Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }
            }
        });
    }

}
