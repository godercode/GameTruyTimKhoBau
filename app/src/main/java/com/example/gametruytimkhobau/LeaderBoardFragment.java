package com.example.gametruytimkhobau;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

public class LeaderBoardFragment extends Fragment {
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mReference;
    private FirebaseUser mUser;
    private RecyclerView rcvPlayer;
    private PlayerAdapter playerAdapter;
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
        playerAdapter = new PlayerAdapter(getActivity());
    }

    private void getUserDataFromFirebase() {
        mReference = FirebaseDatabase.getInstance().getReference("users");
        mReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();

                // Lấy dữ liệu từ Firebase vào userList
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    User user = userSnapshot.getValue(User.class);
                    if (user != null) {
                        userList.add(user);
                    }
                }

                // Sắp xếp danh sách theo điểm số từ cao đến thấp
                Collections.sort(userList, (u1, u2) -> Integer.compare(u2.getScore(), u1.getScore()));

                // Cập nhật thuộc tính rank theo thứ tự xếp hạng và cập nhật Firebase
                for (int i = 0; i < userList.size(); i++) {
                    User user = userList.get(i);
                    user.setRank(i + 1); // Xếp hạng từ 1 trở đi

                    // Cập nhật rank vào Firebase
                    String userId = userList.get(i).getUserId(); // Lấy userId từ Firebase
                    if (userId != null) {
                        // Cập nhật rank của user lên Firebase
                        mReference.child(userId).child("rank").setValue(user.getRank(), (error, ref) -> {
                            if (error == null) {
                                Log.d("Leaderboard", "Rank updated successfully for user: " + userId);
                            } else {
                                Log.e("Leaderboard", "Failed to update rank", error.toException());
                            }
                        });
                    }
                }
                // Cập nhật dữ liệu cho Adapter
                playerAdapter.setData(userList);
                playerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Leaderboard", "Failed to read user data", error.toException());
                Toast.makeText(getActivity(), "Failed to fetch data: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }


    private void setLayout() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
        rcvPlayer.setLayoutManager(linearLayoutManager);
        rcvPlayer.setAdapter(playerAdapter);
    }
}
