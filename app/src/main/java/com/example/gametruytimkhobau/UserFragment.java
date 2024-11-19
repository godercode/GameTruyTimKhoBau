package com.example.gametruytimkhobau;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.HashMap;
import java.util.Map;

public class UserFragment extends Fragment {
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mReference;
    private FirebaseUser mUser;
    private String userID;
    private Button btnLogout, btnResetPass, btnVerify, btnUpdate;
    private TextView tvUsername, tvVerify;
    private ImageView imgUserAvatar;
    private Uri mUri;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);
        initViews(view);
        createProfile();
        setListeners();
        return view;
    }
    //Hàm khởi tạo biến
    private void initViews(View view) {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mUser = mAuth.getCurrentUser();
        //kiểm tra đăng nhập rồi mới được vào đây
        if (mUser != null) {
            //Lấy ra id của người dùng này
            userID = mUser.getUid();

            btnUpdate = view.findViewById(R.id.btn_update_profile);
            btnLogout = view.findViewById(R.id.btn_logout);
            tvUsername = view.findViewById(R.id.tv_user_name);
            imgUserAvatar = view.findViewById(R.id.img_user_avatar);
            btnResetPass = view.findViewById(R.id.btn_reset_password);
            tvVerify = view.findViewById(R.id.tv_verify);
            btnVerify = view.findViewById(R.id.btn_verify);
            //Kiểm tra xem verify chưa thì yêu cầu
            if (!mUser.isEmailVerified()) {
                tvVerify.setVisibility(View.VISIBLE);
                btnVerify.setVisibility(View.VISIBLE);
            }
        } else {
            Toast.makeText(getActivity(), "User not logged in", Toast.LENGTH_SHORT).show();
            signOutAndRedirect();
        }
    }
    //Tạo cái profile cho người dùng
    private void createProfile(){
        if (userID != null) {
            getUserDataFromFirebase(userID);
        }
    }

    private void getUserDataFromFirebase(String userID) {
        mReference = mDatabase.getReference("users").child(userID); // Trỏ tới nút của user cụ thể
        mReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Chuyển dữ liệu về đối tượng User
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                       tvUsername.setText(user.getUserName());
                        Glide.with(getActivity()).load(user.getAvatar()).error(R.drawable.ic_user).into(imgUserAvatar);
                    }
                } else {
                    Log.d("Firebase", "User not found");

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Failed to read user data", error.toException());
            }
        });
    }

    //Xử lý click cho các nút
    private void setListeners() {
        btnLogout.setOnClickListener(v -> signOutAndRedirect());
        btnResetPass.setOnClickListener(v -> createResetPassDialog());
        btnVerify.setOnClickListener(v -> sendVerificationEmail());
        imgUserAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPermission();
            }
        });
        btnUpdate.setOnClickListener(v -> updateUserDataToFireBase());
    }

    private void requestPermission() {
      Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
      startActivityForResult(intent, 1000);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000 && resultCode == RESULT_OK && data != null) {
            mUri = data.getData();
            if (getActivity() != null && mUri != null) {
                imgUserAvatar.setImageURI(mUri);
                Log.d("UserFragment", mUri.toString());
            }
        }
    }

    //Gửi mã xức thực vào email
    private void sendVerificationEmail() {
        if (mUser != null) {
            mUser.sendEmailVerification().addOnSuccessListener(unused -> {
                Toast.makeText(getActivity(), "Verification Email Has been Sent", Toast.LENGTH_LONG).show();
            }).addOnFailureListener(e -> {
                Toast.makeText(getActivity(), "Verification Email Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                Log.d("UserFragment", "sendVerificationEmail: failure", e);
            });
        }
    }
//Tạo cái dialog cho nhập pass mới
    private void createResetPassDialog() {
        EditText resetPassword = new EditText(getActivity());
        AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(getActivity());
        passwordResetDialog.setTitle("Reset Password?");
        passwordResetDialog.setMessage("Enter Your New Password");
        passwordResetDialog.setView(resetPassword);
        //chọn yes thì nảy vào đây
        passwordResetDialog.setPositiveButton("Yes", (dialog, which) -> {
            String newPassword = resetPassword.getText().toString().trim();
            //gọi hàm updatePassword viết bên dưới
            updatePassword(newPassword);
            dialog.dismiss();
        });
        passwordResetDialog.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
        passwordResetDialog.create().show();
    }//Hàm này đổi mật khẩu copy past
    private void updatePassword(String newPassword) {
        if (mUser != null) {
            mUser.updatePassword(newPassword).addOnSuccessListener(unused -> {
                Toast.makeText(getActivity(), "Password Reset Successfully.", Toast.LENGTH_LONG).show();
            }).addOnFailureListener(e -> {
                Toast.makeText(getActivity(), "Password Reset Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                Log.d("UserFragment", "updatePassword: failure", e);
            });
        }
    }
    //Đăng xuất
    private void signOutAndRedirect() {
        mAuth.signOut();
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), SplashActivity.class);
            startActivity(intent);
            getActivity().finish();  // Close the current Activity
        } else {
            Log.e("UserFragment", "getActivity() is null, unable to redirect after sign-out");
        }
    }
    private void updateUserDataToFireBase() {
        if (mUri != null) {
            mReference = mDatabase.getReference("users").child(userID);
            Map<String, Object> updates = new HashMap<>();
            updates.put("avatar", mUri.toString());
            mReference.updateChildren(updates, (error, ref) -> {
                if (error == null) {
                    Log.d("UserFragment", "updateAvatar");
                } else {
                    Log.e("UserFragment", "Failed to update avatar", error.toException());
                    Toast.makeText(getActivity(), "Failed to update avatar: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            Log.e("UserFragment", "URI is null!");
        }
    }

}
