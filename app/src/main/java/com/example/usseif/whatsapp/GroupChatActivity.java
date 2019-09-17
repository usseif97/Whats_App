package com.example.usseif.whatsapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class GroupChatActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef, groupNameRef, groupMessageKeyRef;

    private String currentGroupName, currentUserName, currentUserID, currentDate, currentTime;

    private Toolbar mtoolbar;
    private ImageButton sendMessageButton;
    private EditText userMessage;
    private ScrollView mScorll;
    private TextView dislayTextMessages;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        currentGroupName = getIntent().getExtras().get("groupName").toString();  //Receive the passed Extra
        Toast.makeText(this, currentGroupName, Toast.LENGTH_SHORT).show();

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        groupNameRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);

        InitializeFields();

        GetUserInfo();

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveMessageInfoToDatabase();
                userMessage.setText("");
                mScorll.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        groupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.exists()){
                    DisplayMessage(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.exists()){
                    DisplayMessage(dataSnapshot);
                }
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

    private void InitializeFields(){
        mtoolbar = findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle(currentGroupName);
        sendMessageButton = findViewById(R.id.send_message_button);
        userMessage = findViewById(R.id.group_chat_message);
        mScorll = findViewById(R.id.scroll);
        dislayTextMessages = findViewById(R.id.group_chat_text);
    }

    private void GetUserInfo(){
        usersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    currentUserName = dataSnapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void SaveMessageInfoToDatabase(){
        String message = userMessage.getText().toString();
        String messageKey = groupNameRef.push().getKey();  // Generate A Random Key

        if (TextUtils.isEmpty(message)){
            Toast.makeText(this, "Please Write Your Message..", Toast.LENGTH_SHORT).show();
        } else {
            Calendar date = Calendar.getInstance();
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("dd MMM, yyyy");
            currentDate = currentDateFormat.format(date.getTime());

            Calendar time = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
            currentTime = currentTimeFormat.format(time.getTime());

            HashMap<String, Object> groupMessageKey = new HashMap<>();
            groupNameRef.updateChildren(groupMessageKey);

            groupMessageKeyRef = groupNameRef.child(messageKey);  // Reference To The Key

            HashMap<String, Object> messageInfo = new HashMap<>();
            messageInfo.put("name", currentUserName);
            messageInfo.put("message", message);
            messageInfo.put("date", currentDate);
            messageInfo.put("time", currentTime);
            groupMessageKeyRef.updateChildren(messageInfo);


        }
    }

    private void DisplayMessage(DataSnapshot dataSnapshot){
        Iterator iterator = dataSnapshot.getChildren().iterator();  //Iterate on Messages ID
        while (iterator.hasNext()){ // Message ID
            // Message Info
            String chatDate = ((DataSnapshot) iterator.next()).getValue().toString();
            String chatMessage = ((DataSnapshot) iterator.next()).getValue().toString();
            String chatName = ((DataSnapshot) iterator.next()).getValue().toString();
            String chatTime = ((DataSnapshot) iterator.next()).getValue().toString();

            dislayTextMessages.append(chatName + " :\n" + chatMessage + "\n" + chatTime + "    " + chatDate + "\n\n\n");

            mScorll.fullScroll(ScrollView.FOCUS_DOWN);
        }

    }

}
