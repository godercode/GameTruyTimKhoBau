package com.example.gametruytimkhobau;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.PatternsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore fStore;
    private EditText etEmail, etUsername, etPassword;
    private Button btnSignup;
    private CheckBox cbAgree;
    private ProgressBar progressBar;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        initViews();
        setListeners();
    }
    //Hàm này khởi tạo giá trị cho mấy biến
    private void initViews() {
        etEmail = findViewById(R.id.et_email);
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        btnSignup = findViewById(R.id.btn_signup);
        cbAgree = findViewById(R.id.cd_agree);
        progressBar = findViewById(R.id.pgb_signup);
        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
    }
    //Hàm này xử lý sự kiên click
    private void setListeners() {
        btnSignup.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            String email = etEmail.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();
            String name = etUsername.getText().toString().trim();
            Boolean isAgreeChecked = cbAgree.isChecked();

            if (validating(email, pass, name, isAgreeChecked)) {
                createUser(email, pass, name);
            } else {
                Toast.makeText(SignupActivity.this, "Please fill all fields and agree to terms", Toast.LENGTH_SHORT).show();
            }
        });
    }
//Hàm xử lý đăng ký chính
    private void createUser(String email, String pass, String name) {
        //tạo tài khoản với email và pass
        mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, task -> {
                    //cái progress này để hiện cái loading
                    progressBar.setVisibility(View.VISIBLE);
                    //kiểm tra tạo thành công nhảy vào đây
                    if (task.isSuccessful()) {
                        //Gọi hàm xác thực bằng email
                        verificationEmail();
                        userID = mAuth.getCurrentUser().getUid();
                        //Dùng firestore để lưu dữ liệu người dùng lên firebase
                        DocumentReference documentReference = fStore.collection("users").document(userID);
                        Map<String, Object> user = new HashMap<>();
                        user.put("userName", name);
                        user.put("email", email);

                        //Lưu thành công nhảy vào đây
                        documentReference.set(user).addOnSuccessListener(unused -> {
                            Log.d("Signup", "onSuccess: user Profile is created for " + userID);
                            progressBar.setVisibility(View.GONE);
                        });
                        //Xong là gọi intent chuyển sang cho log vào
                        Log.d("Signup", "createUserWithEmail: success");
                        Toast.makeText(SignupActivity.this, "Welcome " + email, Toast.LENGTH_LONG).show();
                        startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                        finishAffinity();
                    //Tạo thất bại nhảy vào đây
                    } else {
                        Log.d("Signup", "createUserWithEmail: failure", task.getException());
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(SignupActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
        });
    }
    //Hàm này xử lý các thực bằng email mặc định copy past
    private void verificationEmail() {
        FirebaseUser user = mAuth.getCurrentUser();
        user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(SignupActivity.this, "Verification Email Has been Sent", Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SignupActivity.this, "Verification Email Failed" + e.getMessage(), Toast.LENGTH_LONG).show();
                Log.d("Signup", "VerificationEmail: failure", e);
            }
        });
    }
    //Hàm này validate dữ liệu
    private boolean validating(String email, String password, String username, Boolean isAgreeChecked) {
        if (email.isEmpty()) {
            etEmail.setError("Please enter an email");
            return false;
        }
        if (!PatternsCompat.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email");
            return false;
        }
        if (username.isEmpty()) {
            etUsername.setError("Please enter a username");
            return false;
        }
        if (password.isEmpty()) {
            etPassword.setError("Please enter a password");
            return false;
        }
        if (!isAgreeChecked) {
            Toast.makeText(this, "Please accept the terms and conditions", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
