package com.example.usseif.whatsapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;

    private ProgressDialog loadingBar;

    private Button createAccountButton;
    private EditText userEmail, userPassword;
    private TextView alreadyHaveAccountLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();

        InitializeFields();

        alreadyHaveAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToLoginActivity();
            }
        });

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateNewAccount();
            }
        });
    }

    private void InitializeFields(){
        createAccountButton = findViewById(R.id.register_button);
        userEmail = findViewById(R.id.register_email);
        userPassword = findViewById(R.id.register_password);
        alreadyHaveAccountLink = findViewById(R.id.already_have_account_link);
        loadingBar = new ProgressDialog(this);
    }

    private void CreateNewAccount(){
        String email = userEmail.getText().toString();
        String password = userPassword.getText().toString();

        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "Please Enter Email..", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please Enter Password..", Toast.LENGTH_SHORT).show();
            return;
        }
        //Else
        loadingBar.setTitle("Creating New Account");
        loadingBar.setMessage("Please Wait, While Creating Your Account..");
        loadingBar.setCanceledOnTouchOutside(true);
        loadingBar.show();
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    String deviceToken = FirebaseInstanceId.getInstance().getToken();
                    String currentUserID = mAuth.getCurrentUser().getUid();
                    rootRef.child("Users").child(currentUserID).setValue("");
                    rootRef.child("Users").child(currentUserID).child("device_token").setValue(deviceToken);

                    SendUserToMainActivity();
                    Toast.makeText(RegisterActivity.this, "Account Created Successfully..", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                } else {
                    String message = task.getException().toString();
                    Toast.makeText(RegisterActivity.this, "Error " + message, Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }
            }
        });
    }

    private void SendUserToLoginActivity(){
        Intent logInIntent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(logInIntent);
    }

    private void SendUserToMainActivity(){
        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }


}
