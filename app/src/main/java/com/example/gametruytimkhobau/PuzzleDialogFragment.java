package com.example.gametruytimkhobau;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.Marker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class PuzzleDialogFragment extends DialogFragment {

    private FirebaseDatabase mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private String userId;
    private TextView questionText;//text hiện thị cau hỏi
    private Button answer1, answer2, answer3, answer4, btnSubmit, btnSkip;
    private Button selectedButton = null;  // Button đã được chọn
    private int correctAnswerIndex = 0;  // Chỉ số đáp án đúng (bắt đầu từ 0)
    private Puzzle currentPuzzle;//câu đố hiện tại
    private Marker closestMarker;
    private OnScoreUpdateListener scoreUpdateListener;
    private DialogInterface.OnDismissListener onDismissListener;

    public void setOnDismissListener(DialogInterface.OnDismissListener listener) {
        this.onDismissListener = listener;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (onDismissListener != null) {
            onDismissListener.onDismiss(dialog);
        }
    }

    // Flag xác định câu đố đã được giải đúng
    private boolean puzzleSolved = false;



    public interface OnScoreUpdateListener{
        void onScoreUpdated(int newSocre);
    }
    public void setCurrentPuzzle(Puzzle puzzle, Marker marker) {
        this.currentPuzzle = puzzle;
        this.closestMarker = marker;
    }

    // Trả về trạng thái giải câu đố
    public boolean isPuzzleSolved() {
        return puzzleSolved;
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_puzzle_dialog, null);

        // khởi tạo các thành phần câu đố
        questionText = view.findViewById(R.id.tvQuestion);
        answer1 = view.findViewById(R.id.btnAnswer1);
        answer2 = view.findViewById(R.id.btnAnswer2);
        answer3 = view.findViewById(R.id.btnAnswer3);
        answer4 = view.findViewById(R.id.btnAnswer4);
        btnSubmit = view.findViewById(R.id.btnSubmit);
        btnSkip = view.findViewById(R.id.btnSkip);


        if (currentPuzzle != null) {
            // gán câu hỏi và đáp án vào các button
            questionText.setText(currentPuzzle.getQuestion());
            answer1.setText(currentPuzzle.getOptions().get(0));
            answer2.setText(currentPuzzle.getOptions().get(1));
            answer3.setText(currentPuzzle.getOptions().get(2));
            answer4.setText(currentPuzzle.getOptions().get(3));

            // lưu chỉ số đáp án đúng
            correctAnswerIndex = currentPuzzle.getCorrectAnswer();
        }

        // lắng nghe sự kiện click cho từng đáp án
        View.OnClickListener answerClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedButton != null) {
                    selectedButton.setBackgroundColor(Color.parseColor("#FFCDD2"));
                    selectedButton.setTextColor(Color.BLACK);
                }
                selectedButton = (Button) v;
                selectedButton.setBackgroundColor(getResources().getColor(R.color.brown));
                selectedButton.setTextColor(Color.WHITE);
            }
        };
        // gán sự kiện click cho từng đáp án
        answer1.setOnClickListener(answerClickListener);
        answer2.setOnClickListener(answerClickListener);
        answer3.setOnClickListener(answerClickListener);
        answer4.setOnClickListener(answerClickListener);

        // sự kiện click cho nút Submit gửi đáp án đi
        btnSubmit.setOnClickListener(v -> {
            if (selectedButton == null) {
                Toast.makeText(getActivity(), "Hãy chọn một đáp án", Toast.LENGTH_SHORT).show();
                return;
            }

            int selectedAnswerIndex = getSelectedAnswerIndex();
            Log.d("PuzzleDialogFragment", "Selected index: " + selectedAnswerIndex);
            Log.d("PuzzleDialogFragment", "Correct index: " + correctAnswerIndex);

            if (selectedAnswerIndex == correctAnswerIndex) {
                puzzleSolved = true; // Đánh dấu câu đố đã giải
                // Kiểm tra marker và cập nhật trạng thái kho báu
                if (closestMarker != null && closestMarker.getTag() instanceof Treasure) {
                    Treasure treasure = (Treasure) closestMarker.getTag();

                    DatabaseReference treasureRef = FirebaseDatabase.getInstance().getReference("treasures")
                            .child(String.valueOf(treasure.getId()));

                    treasureRef.child("status").setValue(false)
                            .addOnSuccessListener(aVoid -> {
                                Log.d("PuzzleDialogFragment", "Treasure collected: " + treasure.getTreasureName());

                                // Xóa Marker khỏi bản đồ
                                if (closestMarker != null) {
                                    closestMarker.remove();
                                }
                            })
                            .addOnFailureListener(e -> Log.e("PuzzleDialogFragment", "Error updating treasure status", e));
                }
                else {
                    Log.e("PuzzleDialogFragment", "Null marker or Treasure to update");
                }

                int earnedScore = currentPuzzle.getPoint(); // Điểm của câu hỏi
//                closestMarker.remove();

                Log.d("PuzzleDialogFragment", "Calling showScoreDialog with score: " + earnedScore);
                showScoreDialog(earnedScore);
                dismiss();
            } else {
                showWrongDialog();
                dismiss();
            }
        });
        btnSkip.setOnClickListener(v ->dismiss());
        builder.setView(view);
        return builder.create();
    }

    private void showScoreDialog(int earnedScore){
        if (getContext() != null && isAdded()) {
            Dialog dialog = new Dialog(getContext(), R.style.TransparentDialog);
            dialog.setContentView(R.layout.dialog_correct_notification);
            dialog.setCancelable(false);

            TextView scoreView = dialog.findViewById(R.id.scoreText);
            Button btn_receive_score = dialog.findViewById(R.id.btn_receive_score);

            scoreView.setText(String.valueOf(earnedScore));
            btn_receive_score.setOnClickListener(v -> {
                UpdateScoreFirebase(earnedScore);
                dialog.dismiss();
            });
            dialog.show();
        }
    }

    private void UpdateScoreFirebase(int earnedScore){
        mDatabase = FirebaseDatabase.getInstance();
        //Lấy ra user hiện tại thông qua FAuth
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        userId = mUser.getUid();

        DatabaseReference userRef = mDatabase.getReference("users").child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) { // Kiểm tra snapshot có tồn tại
                    User user = snapshot.getValue(User.class);
                    if (user != null) { // Kiểm tra user không null
                        // Lấy điểm hiện tại hoặc gán giá trị mặc định là 0
                        int currentScore = user.getScore() != 0 ? user.getScore() : 0;
                        int newScore = currentScore + earnedScore;
                        Map<String, Object> updates = new HashMap<>();
                            updates.put("score", newScore);
                            userRef.updateChildren(updates, (error, ref) -> {
                                if (error == null) {
                                    Log.d("LocationFragment", "Score updated successfully");
                                } else {
                                    Log.e("LocationFragment", "Failed to update score", error.toException());
                                    Toast.makeText(getActivity(), "Failed to update score: " + error.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                    } else {
                        Log.e("FirebaseError", "Đối tượng user null");
                    }
                } else {
                    Log.e("FirebaseError", "Snapshot does not exist for userRef");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", error.getMessage());
            }
        });

    }

    private void showWrongDialog() {
        if (getContext() != null && isAdded()) {
            Dialog dialog = new Dialog(getContext(), R.style.TransparentDialog);
            dialog.setContentView(R.layout.dialog_wrong_notification);
            dialog.setCancelable(false);

            Button btnTryAgain = dialog.findViewById(R.id.btn_try_again);

            btnTryAgain.setOnClickListener(v -> {
                dialog.dismiss();
            });

            dialog.show();
        }
    }


    // Hàm trả về chỉ số của đáp án đã chọn (0 -> 3)
    private int getSelectedAnswerIndex() {
        if (selectedButton == answer1) {
            return 0;
        } else if (selectedButton == answer2) {
            return 1;
        } else if (selectedButton == answer3) {
            return 2;
        } else if (selectedButton == answer4) {
            return 3;
        }
        return -1;
    }
}
