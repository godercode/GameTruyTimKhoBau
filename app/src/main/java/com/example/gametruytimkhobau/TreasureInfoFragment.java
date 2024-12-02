package com.example.gametruytimkhobau;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Collections;
import java.util.List;

public class TreasureInfoFragment extends Fragment {

    private String title;
    private float distance;
    private LatLng currentLocation;
    private Marker selectedMarker;
    private TreasureManager treasureManager;

    public interface OnTreasureFoundListener {
        void onTreasureFound();
    }

    private OnTreasureFoundListener treasureFoundListener;

    public static TreasureInfoFragment newInstance(String title, float distance, LatLng currentLocation, Marker marker) {
        TreasureInfoFragment fragment = new TreasureInfoFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putFloat("distance", distance);
        args.putParcelable("currentLocation", currentLocation);
        fragment.setArguments(args);
        fragment.selectedMarker = marker;
        return fragment;
    }

    public void setOnTreasureFoundListener(OnTreasureFoundListener listener) {
        this.treasureFoundListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_treasure_info, container, false);
        treasureManager = new TreasureManager();

        if (getArguments() != null) {
            title = getArguments().getString("title");
            distance = getArguments().getFloat("distance");
            currentLocation = getArguments().getParcelable("currentLocation");
        }

        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.width = 830;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        view.setLayoutParams(params);

        TextView titleTextView = view.findViewById(R.id.treasure_title);
        TextView distanceTextView = view.findViewById(R.id.treasure_distance);
        Button findTreasureButton = view.findViewById(R.id.find_treasure_button);

        titleTextView.setText(title);
        distanceTextView.setText("Khoảng cách: " + Math.round(distance) + " m");

        findTreasureButton.setOnClickListener(v -> {
            if (distance > 500) {
                Toast.makeText(getActivity(), "Khoảng cách của bạn quá xa, hãy lại gần thêm " + Math.round(distance - 500) + " m", Toast.LENGTH_SHORT).show();
                return;
            }

            showRandomPuzzleDialog();
        });

        return view;
    }

    private void showRandomPuzzleDialog() {
        PuzzleManager puzzleManager = new PuzzleManager();
        puzzleManager.getPuzzlesData(new PuzzleManager.PuzzlesDataCallback() {
            @Override
            public void onSuccess(List<Puzzle> puzzles) {
                if (puzzles == null || puzzles.isEmpty()) {
                    Toast.makeText(getActivity(), "Chưa có câu đố nào!", Toast.LENGTH_SHORT).show();
                    return;
                }

                Collections.shuffle(puzzles);
                Puzzle randomPuzzle = puzzles.get(0);

                PuzzleDialogFragment puzzleDialog = new PuzzleDialogFragment();
                puzzleDialog.setCurrentPuzzle(randomPuzzle, selectedMarker);
                puzzleDialog.show(requireActivity().getSupportFragmentManager(), "PuzzleDialogFragment");

                // Lắng nghe sự kiện sau khi câu đố được giải
                puzzleDialog.setOnDismissListener(dialog -> {
                    if (puzzleDialog.isPuzzleSolved()) {
                        // Cập nhật điểm số
                        int earnedScore = randomPuzzle.getPoint();

                        Toast.makeText(getActivity(), "Chúc mừng! Bạn đã thu thập được kho báu.", Toast.LENGTH_SHORT).show();

                        // Gọi callback nếu có
                        if (treasureFoundListener != null) {
                            treasureFoundListener.onTreasureFound();
                        }

                        // Đóng fragment
                        getParentFragmentManager().beginTransaction().remove(TreasureInfoFragment.this).commit();
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(getActivity(), "Lỗi khi tải câu đố. Vui lòng thử lại!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateScoreToFirebase(int earnedScore) {
        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser mUser = mAuth.getCurrentUser();

        if (mUser == null) {
            Toast.makeText(getActivity(), "Vui lòng đăng nhập!", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mUser.getUid();
        DatabaseReference userRef = mDatabase.getReference("users").child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(getActivity(), "Không tìm thấy thông tin người dùng!", Toast.LENGTH_SHORT).show();
                    return;
                }

                User user = snapshot.getValue(User.class);
                if (user != null) {
                    int currentScore = user.getScore() != 0 ? user.getScore() : 0;
                    int newScore = currentScore + earnedScore;

                    // Cập nhật điểm mới
                    userRef.child("score").setValue(newScore)
                            .addOnSuccessListener(aVoid -> Toast.makeText(getActivity(), "Điểm đã được cập nhật!", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(getActivity(), "Lỗi khi cập nhật điểm!", Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Lỗi Firebase: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
