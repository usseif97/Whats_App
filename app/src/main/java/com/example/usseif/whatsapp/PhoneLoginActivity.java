package com.example.usseif.whatsapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {

    private EditText inputPhoneNumber, inputVerificationCode;
    private Button sendVerificationCode, verifyButton;

    private ProgressDialog loadingBar;

    private FirebaseAuth mAuth;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    private String mVerificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        mAuth = FirebaseAuth.getInstance();

        InitializeFields();

        sendVerificationCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String phoneNumber = inputPhoneNumber.getText().toString();
                if(TextUtils.isEmpty(phoneNumber)){
                    Toast.makeText(PhoneLoginActivity.this, "Please, Enter Your Phone Number", Toast.LENGTH_SHORT).show();
                } else {
                    loadingBar.setTitle("Phone Verification");
                    loadingBar.setMessage("Please wait, while authenticating with your phone");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    PhoneAuthProvider.getInstance().verifyPhoneNumber(   // Send Verification Code (callbacks)
                            phoneNumber,        // Phone number to verify
                            60,                 // Timeout duration
                            TimeUnit.SECONDS,   // Unit of timeout
                            PhoneLoginActivity.this,               // Activity (for callback binding)
                            callbacks);        // OnVerificationStateChangedCallbacks
                }
            }
        });

        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendVerificationCode.setVisibility(View.INVISIBLE);
                inputPhoneNumber.setVisibility(View.INVISIBLE);

                String verificationCode = inputVerificationCode.getText().toString();
                if (TextUtils.isEmpty(verificationCode)){
                    Toast.makeText(PhoneLoginActivity.this, "Enter Verification Code..", Toast.LENGTH_SHORT).show();
                } else {
                    loadingBar.setTitle("Verification Code");
                    loadingBar.setMessage("Please wait for Verifying");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });

        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                mVerificationId = verificationId;
                mResendToken = token;
                loadingBar.dismiss();
                Toast.makeText(PhoneLoginActivity.this, "Code has Sent Successfully..", Toast.LENGTH_SHORT).show();

                sendVerificationCode.setVisibility(View.INVISIBLE);
                inputPhoneNumber.setVisibility(View.INVISIBLE);
                inputVerificationCode.setVisibility(View.VISIBLE);
                verifyButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                loadingBar.dismiss();
                Toast.makeText(PhoneLoginActivity.this, "Invalid Phone Number, Please Enter Correct Phone Number", Toast.LENGTH_SHORT).show();

                sendVerificationCode.setVisibility(View.VISIBLE);
                inputPhoneNumber.setVisibility(View.VISIBLE);
                inputVerificationCode.setVisibility(View.INVISIBLE);
                verifyButton.setVisibility(View.INVISIBLE);
            }
        };
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            loadingBar.dismiss();
                            Toast.makeText(PhoneLoginActivity.this, "You LoggedIn Successfully", Toast.LENGTH_SHORT).show();
                            SendUserToMainActivity();
                        } else {
                            String message = task.getException().toString();
                            Toast.makeText(PhoneLoginActivity.this, "Error " + message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void InitializeFields(){
        inputPhoneNumber = findViewById(R.id.phone_number_input);
        inputVerificationCode = findViewById(R.id.verification_code_input);
        sendVerificationCode = findViewById(R.id.send_verification_code_button);
        verifyButton = findViewById(R.id.verify_button);
        loadingBar = new ProgressDialog(this);
    }

    private void SendUserToMainActivity(){
        Intent mainIntent = new Intent(PhoneLoginActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }


}
