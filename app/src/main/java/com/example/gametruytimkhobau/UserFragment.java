package com.example.gametruytimkhobau;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class UserFragment extends Fragment {
    private FirebaseAuth mAuth;
    private FirebaseFirestore mStore;
    private FirebaseUser mUser;
    private String userID;
    private Button btnLogout, btnResetPass, btnVerify;
    private TextView tvUsername, tvVerify;
    private ImageView imgUserAvatar;

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
        mStore = FirebaseFirestore.getInstance();
        mUser = mAuth.getCurrentUser();
        //kiểm tra đăng nhập rồi mới được vào đây
        if (mUser != null) {
            //Lấy ra id của người dùng này
            userID = mUser.getUid();

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
            //Láy dữ liệu từ firestore hiện ra thôi
            DocumentReference documentReference = mStore.collection("users").document(userID);
            documentReference.addSnapshotListener((value, error) -> {
                if (error != null) {
                    Log.w("UserFragment", "Listen failed.", error);
                    return;
                }
                if (value != null && value.exists() && getActivity() != null && isAdded()) {
                    //Đây lấy username từ firestore hiện ra
                    tvUsername.setText(value.getString("userName"));
                }
            });
        }
    }
    //Xử lý click cho các nút
    private void setListeners() {
        btnLogout.setOnClickListener(v -> signOutAndRedirect());
        btnResetPass.setOnClickListener(v -> createResetPassDialog());
        btnVerify.setOnClickListener(v -> sendVerificationEmail());
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
    }
    //Hàm này đổi mật khẩu copy past
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
            getActivity().finish();
        }
    }
}
