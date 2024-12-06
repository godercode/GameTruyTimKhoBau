package com.example.gametruytimkhobau;

import android.os.Bundle;
import android.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LeaderBoardFragment extends Fragment implements PlayerAdapter.OnItemClickListener {
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mReference;
    private FirebaseUser mUser;
    private RecyclerView rcvPlayer;
    private PlayerAdapter playerAdapter;
    private ValueEventListener valueEventListener;
    private List<User> userList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_leader_board, container, false);
        initViews(view);
        setLayout();
        getUserDataFromFirebase(); // Tải dữ liệu từ Firebase
        return view;
    }

    private void initViews(View view) {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mUser = mAuth.getCurrentUser();
        rcvPlayer = view.findViewById(R.id.rcv_player);
        playerAdapter = new PlayerAdapter(getActivity(), this);
    }

    private void getUserDataFromFirebase() {
        mReference = FirebaseDatabase.getInstance().getReference("users");
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    User user = userSnapshot.getValue(User.class);
                    if (user != null) {
                        user.setUserId(userSnapshot.getKey());
                        userList.add(user);
                    }
                }

                Collections.sort(userList, (u1, u2) -> Integer.compare(u2.getScore(), u1.getScore()));

                for (int i = 0; i < userList.size(); i++) {
                    User user = userList.get(i);
                    user.setRank(i + 1);
                    String userId = user.getUserId();
                    if (userId != null) {
                        mReference.child(userId).child("rank").setValue(user.getRank());
                    }
                }

                if (getActivity() != null && isAdded()) {
                    playerAdapter.setData(userList);
                    playerAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (getActivity() != null && isAdded()) {
                    Toast.makeText(getActivity(), "Failed to fetch data: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        };

        mReference.addValueEventListener(valueEventListener);
    }

    private void setLayout() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
        rcvPlayer.setLayoutManager(linearLayoutManager);
        rcvPlayer.setAdapter(playerAdapter);
    }

    @Override
    public void onItemClick(User user) {
        // Hiển thị dialog thông tin chi tiết người chơi
        showPlayerInfoDialog(user);
    }

    private void showPlayerInfoDialog(User user) {
        // Tạo AlertDialog.Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Tạo View cho dialog
        View view = getLayoutInflater().inflate(R.layout.dialog_player_details, null);
        builder.setView(view);

        //Log.d("LeaderBoard", user.getAvatar());

        // Liên kết các View trong layout
        ImageView imgAvatar = view.findViewById(R.id.img_avatar);
        TextView tvTitle = view.findViewById(R.id.tv_title);
        TextView tvName = view.findViewById(R.id.tv_name);
        TextView tvEmail = view.findViewById(R.id.tv_email);
        TextView tvScore = view.findViewById(R.id.tv_score);
        Button btnOk = view.findViewById(R.id.btn_ok);

        // Gán dữ liệu từ đối tượng User vào các View
        tvTitle.setText(user.getUserName());
        Glide.with(this)
                .load(user.getAvatar())
                .error(R.drawable.ic_user)
                .into(imgAvatar);

        tvName.setText(user.getUserName());
        tvEmail.setText(user.getEmail());
        tvScore.setText("Score:" +String.valueOf(user.getScore()));

        AlertDialog dialog = builder.create();
        btnOk.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
    @Override
    public void onStop() {
        super.onStop();
        if (mReference != null && valueEventListener != null) {
            mReference.removeEventListener(valueEventListener);
        }
    }
}

