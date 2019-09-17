package com.example.usseif.whatsapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private String currentUserID;

    private String hasImage;
    private String retrievedUserName = "";
    private String retrievedStatus = "";
    private String retrievedImage;
    private String register;

    private StorageReference userProfileImages;

    private ProgressDialog loadingBar;

    private static final int galleryPick = 1;  // requestCode for pick image from gallery

    private Button updateAccountSettings;
    private EditText username,userbio;
    private CircleImageView userImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        register = getIntent().getExtras().get("register").toString();  //Receive hasImage boolean
        if (register.equals("false")) {
            hasImage = getIntent().getExtras().get("hasImage").toString();  //Receive hasImage boolean
            if (!hasImage.equals("null")) {
                if (hasImage.equals("true")) {
                    retrievedUserName = getIntent().getExtras().get("userName").toString();  //Receive the passed Extra
                    retrievedStatus = getIntent().getExtras().get("userStatus").toString();  //Receive the passed Extra
                    retrievedImage = getIntent().getExtras().get("userImage").toString();  //Receive the passed Extra

                } else if (hasImage.equals("false")) {
                    retrievedUserName = getIntent().getExtras().get("userName").toString();  //Receive the passed Extra
                    retrievedStatus = getIntent().getExtras().get("userStatus").toString();  //Receive the passed Extra
                }
            }
        }


        InitializeFields();

        RetrieveUserInfo();

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();
        userProfileImages = FirebaseStorage.getInstance().getReference().child("Profile Images");

        updateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateSettings();
            }
        });

        // Intent To My Gallery
        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, galleryPick);
            }
        });

    }

   /* @Override
    protected void onStart() {
        super.onStart();
        hasImage = getIntent().getExtras().get("hasImage").toString();  //Receive hasImage boolean
        if (! hasImage.equals("null")) {
            if (hasImage.equals("true")) {
                retrievedUserName = getIntent().getExtras().get("userName").toString();  //Receive the passed Extra
                retrievedStatus = getIntent().getExtras().get("userStatus").toString();  //Receive the passed Extra
                retrievedImage = getIntent().getExtras().get("userImage").toString();  //Receive the passed Extra

            } else if (hasImage.equals("false")) {
                retrievedUserName = getIntent().getExtras().get("userName").toString();  //Receive the passed Extra
                retrievedStatus = getIntent().getExtras().get("userStatus").toString();  //Receive the passed Extra
            }
        }
    }*/

    private void InitializeFields(){
        updateAccountSettings = findViewById(R.id.update_settings_button);
        username = findViewById(R.id.user_name);
        userbio = findViewById(R.id.user_bio);
        userImage = findViewById(R.id.profile_image);
        loadingBar = new ProgressDialog(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == galleryPick && resultCode == RESULT_OK && data != null){
            Uri imageURI = data.getData();
            // start picker to get image for cropping and then use the image in cropping activity
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);  // Cropped Image

            if (resultCode == RESULT_OK){
                loadingBar.setTitle("Set Profile Image");
                loadingBar.setMessage("Uploading The Image..");
                loadingBar.setCanceledOnTouchOutside(true);
                loadingBar.show();

                Uri resultURI = result.getUri();  // Cropped Image URI
                StorageReference filepath = userProfileImages.child(currentUserID + ".jpg");  // Place Of Image In Storage

                filepath.putFile(resultURI).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() { // Storing Image In Place Of FilePath
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(SettingsActivity.this, "Profile Image Uploaded Successfully", Toast.LENGTH_SHORT).show();

                            String downloadURL = task.getResult().getDownloadUrl().toString(); // link of the stored Image (Storage)
                            rootRef.child("Users").child(currentUserID).child("image").setValue(downloadURL).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        Toast.makeText(SettingsActivity.this, "Image Saved Successfully", Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    } else {
                                        String message = task.getException().toString();
                                        Toast.makeText(SettingsActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    }
                                }
                            });

                        } else {
                            String message = task.getException().toString();
                            Toast.makeText(SettingsActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    }
                });
            }

        }
    }

    private void RetrieveUserInfo(){
        username.setHint(retrievedUserName);
        userbio.setHint(retrievedStatus);
        Picasso.get().load(retrievedImage).placeholder(R.drawable.profile_image).into(userImage);
    }

    private void UpdateSettings(){
        String setUserName = username.getText().toString();
        String setStatus = userbio.getText().toString();

        if(TextUtils.isEmpty(setUserName)){
            Toast.makeText(this, "Please Enter UserName..", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(setStatus)){
            Toast.makeText(this, "Please Enter Status..", Toast.LENGTH_SHORT).show();
            return;
        }

        //Else
        HashMap<String, Object> profileMap = new HashMap<>();
        profileMap.put("uid", currentUserID);
        profileMap.put("name", setUserName);
        profileMap.put("status", setStatus);

        rootRef.child("Users").child(currentUserID).updateChildren(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    SendUserToMainActivity();
                    Toast.makeText(SettingsActivity.this, "Profile Updated Successfully..", Toast.LENGTH_SHORT).show();
                } else {
                    String message = task.getException().toString();
                    Toast.makeText(SettingsActivity.this, "Error " + message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void SendUserToMainActivity(){
        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

}
