package com.example.gametruytimkhobau;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class PuzzleDialogFragment extends DialogFragment {

    private TextView questionText;//text hiện thị cau hỏi
    private Button answer1, answer2, answer3, answer4, btnSubmit, btnSkip;
    private Button selectedButton = null;  // Button đã được chọn
    private int correctAnswerIndex = 0;  // Chỉ số đáp án đúng (bắt đầu từ 0)
    //private PointManager pointManager;
    private PuzzleManager puzzleManager;//quản lý câu đố
    private Puzzle currentPuzzle;//câu đố hiện tại

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

        // lấy câu đố ngẫu nhiên
        puzzleManager = new PuzzleManager();
        currentPuzzle = puzzleManager.getRandomPuzzle();

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

            // lấy id đáp án người chơi đã chọn gán cho biến selectedAnswerIndex
            int selectedAnswerIndex = getSelectedAnswerIndex();

            // Kiểm tra đáp án đúng/sai
            if (selectedAnswerIndex == correctAnswerIndex) {
                // xử lý cập nhật điểm cho người chơi
                int pointEarned = currentPuzzle.getPoint(); // Lấy điểm từ câu hỏi hiện tại
                //pointManager.updatePoint(pointEarned); // Cập nhật điểm (giả sử bạn đã tạo hàm updatePoint trong PointManager)

                // Hiển thị thông báo cho người chơi
                Toast.makeText(getActivity(), "Chúc mừng! Bạn đã trả lời đúng và nhận được " + pointEarned + " điểm.", Toast.LENGTH_SHORT).show();

                // Đặt độ trễ nhỏ để hiển thị thông báo trước khi đóng dialog
//                selectedButton.postDelayed(this::dismiss, 1000);
                dismiss();

            } else {
                Toast.makeText(getActivity(), "Sai rồi! Thử lại với một câu hỏi khác.", Toast.LENGTH_SHORT).show();
                // Đóng dialog
                dismiss();
            }
        });


        // Sự kiện click cho nút Bỏ qua
        btnSkip.setOnClickListener(v -> {
            // Đóng dialog và quay lại bản đồ
            dismiss();

        });

        builder.setView(view);
        return builder.create();
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