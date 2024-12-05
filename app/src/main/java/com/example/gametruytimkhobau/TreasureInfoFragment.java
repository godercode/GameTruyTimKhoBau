package com.example.gametruytimkhobau;

import android.app.Dialog;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TreasureInfoFragment extends Fragment implements SensorEventListener,PuzzleDialogFragment.OnAnswerListener{

    private String title;
    private float distance;
    private LatLng currentLocation;
    private Marker selectedMarker;
    private TreasureManager treasureManager;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private boolean isShaking = false;
    private PuzzleDialogFragment puzzleDialog;

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

        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // Lắng nghe sự kiện lắc
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);

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

            getRandomPuzzle();
        });

        return view;
    }

    private List<Puzzle> cachedPuzzles = new ArrayList<>();

    private void getRandomPuzzle() {
        if (cachedPuzzles.isEmpty()) { // Nếu chưa có dữ liệu cache
            PuzzleManager puzzleManager = new PuzzleManager();
            puzzleManager.getPuzzlesData(new PuzzleManager.PuzzlesDataCallback() {
                @Override
                public void onSuccess(List<Puzzle> puzzlesList) {
                    if (puzzlesList != null && !puzzlesList.isEmpty()) {
                        cachedPuzzles.addAll(puzzlesList); // Cache dữ liệu
                        showRandomPuzzleFromCache(); // Hiển thị câu đố từ cache
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(getActivity(), "Lỗi khi tải câu đố. Vui lòng thử lại!", Toast.LENGTH_SHORT).show();
                }
            });
        } else { // Nếu đã có dữ liệu cache
            showRandomPuzzleFromCache(); // Hiển thị câu đố từ cache
        }
    }


    private void showRandomPuzzleFromCache() {

        new Thread(() -> { // Chạy trong thread riêng
            Collections.shuffle(cachedPuzzles);
            Puzzle randomPuzzle = cachedPuzzles.get(0);
            getActivity().runOnUiThread(() -> { // Cập nhật UI trên main thread
                if (puzzleDialog != null && puzzleDialog.isAdded()) {
                    puzzleDialog.dismiss();
                }

                // Hiển thị câu đố, kết hợp logic từ showRandomPuzzleDialog
                if (randomPuzzle == null) {
                    Toast.makeText(getActivity(), "Chưa có câu đố nào!", Toast.LENGTH_SHORT).show();
                    return;
                }

                puzzleDialog = new PuzzleDialogFragment();
                puzzleDialog.setCurrentPuzzle(randomPuzzle, selectedMarker);
                puzzleDialog.show(requireActivity().getSupportFragmentManager(), "PuzzleDialogFragment");
                puzzleDialog.setOnAnswerListener(this);
//                puzzleDialog.setOnDismissListener(dialog -> {
//                    if (puzzleDialog.isSkipped()) {
//                        Toast.makeText(getActivity(), "Bạn đã bỏ qua câu đố.", Toast.LENGTH_SHORT).show();
//                    } else if (puzzleDialog.isPuzzleSolved()) {
//                        Toast.makeText(getActivity(), "Chúc mừng! Bạn đã thu thập được kho báu.", Toast.LENGTH_SHORT).show();
//                        if (treasureFoundListener != null) {
//                            treasureFoundListener.onTreasureFound();
//                        }
//                        getParentFragmentManager().beginTransaction().remove(TreasureInfoFragment.this).commit();
//                    } else if (!puzzleDialog.isPuzzleSolved()) {
//                        showWrongDialog();
//                    }
//                });
            });
        }).start();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            // Lấy giá trị gia tốc trên 3 trục X, Y, Z
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            // Tính gia tốc tổng hợp (khoảng cách thay đổi trên ba trục)
            float acceleration = (x * x + y * y + z * z) / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);

            // Kiểm tra sự lắc, nếu gia tốc lớn hơn ngưỡng 2.0 (tùy chỉnh theo nhu cầu)
            if (acceleration > 2.0 && !isShaking) {
                isShaking = true;  // Đánh dấu trạng thái lắc máy

                if (puzzleDialog != null && puzzleDialog.isAdded()) {
                    puzzleDialog.dismiss();
                    puzzleDialog = null;
                }
                // Gọi phương thức lấy câu đố ngẫu nhiên sau khi lắc máy
                getRandomPuzzle();

                // Đặt lại trạng thái sau một khoảng thời gian ngắn (ví dụ 1 giây)
                new Handler().postDelayed(() -> isShaking = false, 1000);  // 1000ms = 1 giây
            }
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Không cần xử lý
    }


    // Đảm bảo dừng cảm biến khi fragment không còn sử dụng
    @Override
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onAnswer(boolean isCorrect) {
        if (puzzleDialog != null && puzzleDialog.isAdded()) {
            puzzleDialog.dismiss(); // Ẩn dialog sau khi trả lời
        }

        if (!isCorrect) {
            showWrongDialog();
        } else {
            // Xử lý khi trả lời đúng
            Toast.makeText(getActivity(), "Chúc mừng! Bạn đã thu thập được kho báu.", Toast.LENGTH_SHORT).show();
            if (treasureFoundListener != null) {
                treasureFoundListener.onTreasureFound();
            }
            getParentFragmentManager().beginTransaction().remove(TreasureInfoFragment.this).commit();
        }
    }
    private void showWrongDialog() {
        if (getContext() != null && isAdded()) {
            // Trước khi hiển thị dialog "Thử lại", ẩn câu đố cũ (nếu có)
            if (puzzleDialog != null && puzzleDialog.isAdded()) {
                puzzleDialog.dismiss();  // Đóng câu đố cũ nếu đang hiển thị
            }

            // Tạo và hiển thị dialog thông báo "Vui lòng thử lại"
            Dialog dialog = new Dialog(getContext(), R.style.TransparentDialog);
            dialog.setContentView(R.layout.dialog_wrong_notification);
            dialog.setCancelable(false);

            Button btnTryAgain = dialog.findViewById(R.id.btn_try_again);

            btnTryAgain.setOnClickListener(v -> {
                dialog.dismiss();
                getRandomPuzzle();
            });

            dialog.show();
        }
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
