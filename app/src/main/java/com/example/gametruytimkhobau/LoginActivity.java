package com.example.gametruytimkhobau;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.PatternsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private EditText etEmail, etPassword;
    private TextView tvForgot;
    private Button btnLogin;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initViews();
        setListeners();
    }

    private void initViews() {
        etEmail = findViewById(R.id.et_email_login);
        etPassword = findViewById(R.id.et_password_login);
        btnLogin = findViewById(R.id.btn_login);
        progressBar = findViewById(R.id.pgb_login);
        tvForgot = findViewById(R.id.tv_forgot);
        mAuth = FirebaseAuth.getInstance();
    }

    private void setListeners() {

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();
            if (validating(email, pass)) {
                loginUser(email, pass);
            }
        });
        tvForgot.setOnClickListener(v ->{
            createForgotDialog();
        });
    }

    private void createForgotDialog() {
        EditText resetEmail = new EditText(LoginActivity.this);
        AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(LoginActivity.this);
        passwordResetDialog.setTitle("Reset Password?");
        passwordResetDialog.setMessage("Enter Your Email To Received Reset Link.");
        passwordResetDialog.setView(resetEmail);

        passwordResetDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String email = resetEmail.getText().toString().trim();
                sendResetLink(email);
                dialog.dismiss();
            }
        });
        passwordResetDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        passwordResetDialog.create().show();
    }

    private void sendResetLink(String email) {
        mAuth.sendPasswordResetEmail(email).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(LoginActivity.this, "Reset Link Sent To Your Email.", Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(LoginActivity.this, "Error! Reset Link Is Not Sent. "+ e.getMessage(), Toast.LENGTH_LONG).show();
                Log.d("Login","Forgot failed!", e);
            }
        });
    }

    private void loginUser(String email, String pass) {
        mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(this, task -> {
            progressBar.setVisibility(View.VISIBLE);
            if (task.isSuccessful()) {
                progressBar.setVisibility(View.GONE);
                FirebaseUser user = mAuth.getCurrentUser();
                Log.d("Login", "Login success: " + user.getEmail());
                Toast.makeText(LoginActivity.this, "Welcome " + user.getEmail(), Toast.LENGTH_LONG).show();
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finishAffinity();
            } else {
                progressBar.setVisibility(View.GONE);
                Log.d("Login", "Login failure", task.getException());
                Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean validating(String email, String password) {
        if (email.isEmpty()) {
            etEmail.setError("Please enter an email");
            return false;
        }
        if (!PatternsCompat.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email");
            return false;
        }
        if (password.isEmpty()) {
            etPassword.setError("Please enter a password");
            return false;
        }
        return true;
    }
}
