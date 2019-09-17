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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    private ProgressDialog loadingBar;

    private Button loginButton, phoneLoginButton;
    private EditText userEmail, userPassword;
    private TextView needNewAccountLink, forgetPasswordLink;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        InitializeFields();

        needNewAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToRegisterActivity();
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AllowUserToLogin();
            }
        });

        phoneLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToPhoneLoginActivity();
            }
        });

    }

    private void InitializeFields(){

        loginButton = findViewById(R.id.login_button);
        phoneLoginButton = findViewById(R.id.phone_login_button);
        userEmail = findViewById(R.id.login_email);
        userPassword = findViewById(R.id.login_password);
        needNewAccountLink = findViewById(R.id.need_new_account_link);
        forgetPasswordLink = findViewById(R.id.forget_password_link);
        loadingBar = new ProgressDialog(this);

    }

    private void AllowUserToLogin(){
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
        loadingBar.setTitle("Log In");
        loadingBar.setMessage("Please Wait..");
        loadingBar.setCanceledOnTouchOutside(true);
        loadingBar.show();
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    String currentUserID = mAuth.getCurrentUser().getUid();
                    String deviceToken = FirebaseInstanceId.getInstance().getToken();  // Mac Address of the Device

                    usersRef.child(currentUserID).child("device_token").setValue(deviceToken)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                SendUserToMainActivity();
                                Toast.makeText(LoginActivity.this, "Logged in Successfully..", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            } else {
                                String message = task.getException().toString();
                                Toast.makeText(LoginActivity.this, "Error " + message, Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });


                } else {
                    String message = task.getException().toString();
                    Toast.makeText(LoginActivity.this, "Error " + message, Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }
            }
        });

    }

    private void SendUserToMainActivity(){
        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void SendUserToRegisterActivity(){
        Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(registerIntent);
    }

    private void SendUserToPhoneLoginActivity(){
        Intent phoneLoginIntent = new Intent(LoginActivity.this, PhoneLoginActivity.class);
        startActivity(phoneLoginIntent);
    }
}
