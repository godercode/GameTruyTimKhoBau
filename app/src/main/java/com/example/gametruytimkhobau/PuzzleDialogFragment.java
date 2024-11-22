package com.example.gametruytimkhobau;

import android.annotation.SuppressLint;
import android.app.Dialog;
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
    private DatabaseReference mReference;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private String userId;
    private TextView questionText;//text hiện thị cau hỏi
    private Button answer1, answer2, answer3, answer4, btnSubmit, btnSkip;
    private Button selectedButton = null;  // Button đã được chọn
    private int correctAnswerIndex = 0;  // Chỉ số đáp án đúng (bắt đầu từ 0)
    private Puzzle currentPuzzle;//câu đố hiện tại

    public interface OnScoreUpdateListener{
        void onScoreUpdated(int newSocre);
    }

    private OnScoreUpdateListener scoreUpdateListener;
    public void setOnScoreUpdateListener(OnScoreUpdateListener listener){
        this.scoreUpdateListener = listener;
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
                    selectedButton.setBackgroundColor(Color.parseColor("#FFCDD2"));  // reset màu của button trước
                }
                selectedButton = (Button) v;
                selectedButton.setBackgroundColor(Color.YELLOW);  // đổi màu khi button được chọn
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

            // lấy chỉ số của đáp án mà người chơi đã chọn gán cho biến selectedAnswerIndex
            int selectedAnswerIndex = getSelectedAnswerIndex();

            // Kiểm tra đáp án đúng/sai : nếu chỉ so mà ng chơi đã chọn bằng với chỉ so của đ/án đúng thì cộng điểm và thong báo
            if (selectedAnswerIndex == correctAnswerIndex) {
                // xử lý cập nhật điểm cho người chơi
                int earnedScore = currentPuzzle.getPoint(); // Lấy điểm từ câu hỏi hiện tại
                UpdateScoreFirebase(earnedScore);
                dismiss();
            } else {
                Toast.makeText(getActivity(), "Sai rồi! Thử lại với một câu hỏi khác.", Toast.LENGTH_SHORT).show();
                dismiss();
            }

        });


        // Sự kiện click cho nút Bỏ qua
        btnSkip.setOnClickListener(v ->dismiss());

        builder.setView(view);
        return builder.create();
    }
    public void setCurrentPuzzle(Puzzle puzzle) {
        this.currentPuzzle = puzzle;
    }


    // Cập nhâật điểm vào firebase sau khi được cộng thêm điểm
    private void UpdateScoreFirebase(int earnedScore){
        mDatabase = FirebaseDatabase.getInstance();
        //Lấy ra user hiện tại thông qua FAuth
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        userId = mUser.getUid();

        DatabaseReference userRef = mDatabase.getReference("users").child(userId);

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                int currentScore = user.getScore();

                int newScore = currentScore + earnedScore;
                user.setScore(newScore);
                userRef.setValue(user);

                if(scoreUpdateListener != null){
                    scoreUpdateListener.onScoreUpdated(newScore);
                }
                Toast.makeText(getActivity(), "Điểm đã được cập nhật",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Lỗi khi lấy dữ liệu từ Firebase", Toast.LENGTH_SHORT).show();
            }
        });

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
        return -1;  // Không có đáp án nào được chọn
    }
}
