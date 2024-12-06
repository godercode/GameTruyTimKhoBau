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
        //Hàm này kiểu dạng hàm main gọi thực thực thi các hàm
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initViews();
        setListeners();
    }
    //Hàm này khởi tạo giá trị cho mấy biến
    private void initViews() {
        etEmail = findViewById(R.id.et_email_login);
        etPassword = findViewById(R.id.et_password_login);
        btnLogin = findViewById(R.id.btn_login);
        progressBar = findViewById(R.id.pgb_login);
        tvForgot = findViewById(R.id.tv_forgot);
        mAuth = FirebaseAuth.getInstance();
    }
    //Hàm này xử lý sự kiên click
    private void setListeners() {
        // Xử lý  khi bấm đăng nhập
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String pass = etPassword.getText().toString().trim();
            // Gọi hàm kiểm tra thông tin nhập vào validate mình viết bên dưới ctrl + chuột trái là ra
            if (validating(email, pass)) {
                loginUser(email, pass);
            }
        });
        //Này xử lý click quên mật khẩu
        tvForgot.setOnClickListener(v ->{
            createForgotDialog();
        });
    }
    //Này xử lý tạo ra cái dialog cho người dùng nhập email vào
    private void createForgotDialog() {
        //Tham số cần thiết khi tạo dialog cơ bản thôi
        EditText resetEmail = new EditText(LoginActivity.this);
        AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(LoginActivity.this);
        passwordResetDialog.setTitle("Reset Password?");
        passwordResetDialog.setMessage("Enter Your Email To Received Reset Link.");
        passwordResetDialog.setView(resetEmail);
        //Đây xử lý cái nút Yes khi bấm vào
        passwordResetDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Bấm vào thì gọi đến hàm sendResetLink mình viết bến dưới với tham số là cái email
                String email = resetEmail.getText().toString().trim();
                sendResetLink(email);
                dialog.dismiss();
            }
        });
        //Xử lý nút No thù dismiss đi thôi
        passwordResetDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        passwordResetDialog.create().show();
    }
    //Hàm này để gửi linkReset pass vào mail mà người dùng nhập vaò trong dialog
    private void sendResetLink(String email) {
        //Mấy cái này là mặc định của Firebase thôi mình chỉ xử lý thông báo ra thôi
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

    //Hàm xử lý login chính đây
    private void loginUser(String email, String pass) {
        //Mắc định của FireBase Auth cần ohair có cấu trúc ntn
        mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(this, task -> {
            //Này là hiện cái loading vì là call API nên để cho người dùng biết là đang chạy thôi
            progressBar.setVisibility(View.VISIBLE);
            //mấy cái dưới này là mặc định
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
    //Hàm check dữ liệu nhập vào validate thôi
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
