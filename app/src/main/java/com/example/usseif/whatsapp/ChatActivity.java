package com.example.usseif.whatsapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;

    private TextView userName, userLastSeen;
    private CircleImageView userImage;
    private Toolbar chatToolbar;
    private ImageButton sendMessageButton;
    private EditText inputMessage;
    private RecyclerView userMessagesList;
    private ImageView sendImage, sendAttachment;

    private ProgressDialog loadingBar;

    private String messageReceiverID, messageReceiverName, messageReceiverImage, messageSenderID;
    private String saveCurrentTime, saveCurrentDate;
    private String checker = "", myURL = "";
    private Uri fileURI;
    private StorageTask uploadTask;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessagesAdapter messagesAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        messageSenderID = mAuth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();

        messageReceiverID = getIntent().getExtras().get("visit_user_id").toString();
        messageReceiverName = getIntent().getExtras().get("visit_user_name").toString();
        messageReceiverImage = getIntent().getExtras().get("visit_user_image").toString();

        Toast.makeText(this, messageReceiverID, Toast.LENGTH_SHORT).show();
        Toast.makeText(this, messageReceiverName, Toast.LENGTH_SHORT).show();

        InitializeFields();

        userName.setText(messageReceiverName);
        Picasso.get().load(messageReceiverImage).placeholder(R.drawable.profile_image).into(userImage);

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendMessage();
            }
        });

        DisplayLastSeen();

        sendImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checker = "image";

                // Send User To Mobile Phone Gallery
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent.createChooser(intent, "Select Image"), 438);

            }
        });

        sendAttachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence options[] = new CharSequence[]
                {
                    "PDF Files",
                    "Word Files"
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                builder.setTitle("Select Attachment Type");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        if (i == 0){
                            checker = "pdf";
                        }
                        if (i == 1){
                            checker = "docx";
                        }
                    }
                });
                builder.show();
            }
        });

    }

    private void InitializeFields(){

        userName = findViewById(R.id.name);
        userLastSeen = findViewById(R.id.last_seen);
        userImage = findViewById(R.id.image);
        sendMessageButton = findViewById(R.id.send_message_chat_button);
        inputMessage = findViewById(R.id.input_message);
        sendImage = findViewById(R.id.send_image_btn);
        sendAttachment = findViewById(R.id.send_attachment_btn);

        messagesAdapter = new MessagesAdapter(messagesList);
        userMessagesList = findViewById(R.id.private_messages_list);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messagesAdapter);

        loadingBar = new ProgressDialog(this);

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("dd MMM, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());
    }


    // Save Images & Files In Database
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 438 && resultCode == RESULT_OK && data != null && data.getData() != null){
            loadingBar.setTitle("Sending a File");
            loadingBar.setMessage("Uploading The File..");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            fileURI = data.getData();

            if (! checker.equals("image")){

            } else if (checker.equals("image")){

                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");
                final String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
                final String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

                DatabaseReference userMessageKeyRef = rootRef.child("Messages").child(messageSenderID).child(messageReceiverID).push();
                final String messagePushID =  userMessageKeyRef.getKey();

                final StorageReference filePath = storageReference.child(messagePushID + "." + "jpg");

                uploadTask = filePath.putFile(fileURI);
                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if (! task.isSuccessful()){
                            return task.getException();
                        }
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()){
                            Uri downloadURL = task.getResult();
                            myURL = downloadURL.toString();

                            Map messageTextBody = new HashMap();
                            messageTextBody.put("message", myURL);
                            messageTextBody.put("name", fileURI.getLastPathSegment());
                            messageTextBody.put("type", checker);
                            messageTextBody.put("from", messageSenderID);
                            messageTextBody.put("to", messageReceiverID);
                            messageTextBody.put("messageID", messagePushID);
                            messageTextBody.put("time", saveCurrentTime);
                            messageTextBody.put("date", saveCurrentDate);



                            Map messageBodyDetails = new HashMap();
                            messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
                            messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messageTextBody);

                            rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful()){
                                        Toast.makeText(ChatActivity.this, "Message Sent Successfully..", Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    } else {
                                        String message = task.getException().toString();
                                        Toast.makeText(ChatActivity.this, "Error " + message, Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    }
                                    inputMessage.setText("");
                                }
                            });
                        }
                    }
                });

            } else {
                Toast.makeText(this, "Nothing Selected !!", Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }



        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        rootRef.child("Messages").child(messageSenderID).child(messageReceiverID).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Messages messages = dataSnapshot.getValue(Messages.class);
                messagesList.add(messages);
                messagesAdapter.notifyDataSetChanged();
                userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void DisplayLastSeen(){
        rootRef.child("Users").child(messageSenderID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("userState").hasChild("state")){
                    String state = dataSnapshot.child("userState").child("state").getValue().toString();
                    String date = dataSnapshot.child("userState").child("date").getValue().toString();
                    String time  = dataSnapshot.child("userState").child("time").getValue().toString();

                    if (state.equals("online")){
                        userLastSeen.setText("Online");
                    } else if (state.equals("offline")){
                        userLastSeen.setText("Last Seen: " + date + " " + time);
                    }

                } else {
                    userLastSeen.setText("Offline");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void SendMessage(){
        String messageText =  inputMessage.getText().toString();

        if (TextUtils.isEmpty(messageText)){
            Toast.makeText(this, "Write Your Message First..", Toast.LENGTH_SHORT).show();
        } else {
            String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
            String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

            DatabaseReference userMessageKeyRef = rootRef.child("Messages").child(messageSenderID).child(messageReceiverID).push();
            String messagePushID =  userMessageKeyRef.getKey();

            Map messageTextBody = new HashMap();
            messageTextBody.put("message", messageText);
            messageTextBody.put("type", "text");
            messageTextBody.put("from", messageSenderID);
            messageTextBody.put("to", messageReceiverID);
            messageTextBody.put("messageID", messagePushID);
            messageTextBody.put("time", saveCurrentTime);
            messageTextBody.put("date", saveCurrentDate);



            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
            messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messageTextBody);

            rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()){
                        Toast.makeText(ChatActivity.this, "Message Sent Successfully..", Toast.LENGTH_SHORT).show();
                    } else {
                        String message = task.getException().toString();
                        Toast.makeText(ChatActivity.this, "Error " + message, Toast.LENGTH_SHORT).show();
                    }
                    inputMessage.setText("");
                }
            });
        }
    }

}
