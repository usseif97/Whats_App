package com.example.usseif.whatsapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private String currentUserID;

    private String register = "false";
    private String hasImage = "false";
    private String retrievedUserName;
    private String retrievedStatus;
    private String retrievedImage;

    private Toolbar mtoolbar;
    private LinearLayout profile;
    private TextView userName, userStatus;
    private CircleImageView userImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_profile);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();

        InitializeFields();

        RetrieveUserInfo();

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToSettingsActivity();
            }
        });

    }

    private void InitializeFields(){
        profile = findViewById(R.id.profile);
        userName = findViewById(R.id.user_name);
        userStatus = findViewById(R.id.user_bio);
        userImage = findViewById(R.id.profile_image);
        mtoolbar = (Toolbar) findViewById(R.id.settings_toolbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Profile Settings");
    }

    private void RetrieveUserInfo(){
        rootRef.child("Users").child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name")) && (dataSnapshot.hasChild("image"))){
                    hasImage = "true";
                    retrievedUserName = dataSnapshot.child("name").getValue().toString();
                    retrievedStatus = dataSnapshot.child("status").getValue().toString();
                    retrievedImage = dataSnapshot.child("image").getValue().toString();

                    userName.setText(retrievedUserName);
                    userStatus.setText(retrievedStatus);
                    Picasso.get().load(retrievedImage).into(userImage);



                } else if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name"))){
                    hasImage = "false";
                    retrievedUserName = dataSnapshot.child("name").getValue().toString();
                    retrievedStatus = dataSnapshot.child("status").getValue().toString();

                    userName.setText(retrievedUserName);
                    userStatus.setText(retrievedStatus);

                } else {
                    register = "true";
                    Toast.makeText(SettingsProfileActivity.this, "Please, Update Your Profile..", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void SendUserToSettingsActivity(){
        Intent settingsIntent = new Intent(SettingsProfileActivity.this, SettingsActivity.class);
        if (register.equals("false")) {
            if (hasImage.equals("true")) {
                settingsIntent.putExtra("register", register);
                settingsIntent.putExtra("hasImage", hasImage);
                settingsIntent.putExtra("userName", retrievedUserName);
                settingsIntent.putExtra("userStatus", retrievedStatus);
                settingsIntent.putExtra("userImage", retrievedImage);
            } else if (hasImage.equals("false")) {
                settingsIntent.putExtra("register", register);
                settingsIntent.putExtra("hasImage", hasImage);
                settingsIntent.putExtra("userName", retrievedUserName);
                settingsIntent.putExtra("userStatus", retrievedStatus);
            }
        }
        settingsIntent.putExtra("register", register);
        startActivity(settingsIntent);
    }
}
