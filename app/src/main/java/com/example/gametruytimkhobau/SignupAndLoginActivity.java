package com.example.gametruytimkhobau;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SignupAndLoginActivity extends AppCompatActivity {
    private Button btnSignup, btnLogin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup_and_login);
        initViews();
        btnLoginClick();
        btnSignupClick();
    }
    private void initViews(){
        btnLogin = findViewById(R.id.btn_login_activity);
        btnSignup = findViewById(R.id.btn_signup_activity);
    }
    private void btnLoginClick(){
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignupAndLoginActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }
    private void btnSignupClick(){
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignupAndLoginActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });
    }
}