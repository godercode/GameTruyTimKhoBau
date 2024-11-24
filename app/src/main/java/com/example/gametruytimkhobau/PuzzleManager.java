package com.example.gametruytimkhobau;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
public class PuzzleManager {
    private FirebaseDatabase mDatabase;
    private DatabaseReference mReference;

    public PuzzleManager() {
        mDatabase = FirebaseDatabase.getInstance();
        mReference = mDatabase.getReference("puzzles");
    }
    public void pushPuzzlesDataToFirebase(){

        mReference=mDatabase.getReference("puzzles");  // Tham chiếu tới node "puzzles" trong Firebase
        // Khởi tạo danh sách câu đố
        List<Puzzle> puzzlesList = new ArrayList<>();

        // push câu đổ vào firebase
        puzzlesList.add(new Puzzle(1, "Năm nào Bác Hồ đọc Tuyên ngôn Độc lập?", 1, Arrays.asList("1944", "1945", "1946", "1947"), 5));
        puzzlesList.add(new Puzzle(2, "Thành phố nào được mệnh danh là thành phố sương mù của Việt Nam?", 1, Arrays.asList("Hà Nội", "Đà Lạt", "Huế", "Hải Phòng"), 5));
        puzzlesList.add(new Puzzle(3, "Ai là vị tướng chỉ huy chiến dịch Điện Biên Phủ?", 2, Arrays.asList("Trường Chinh", "Phạm Văn Đồng", "Võ Nguyên Giáp", "Hoàng Văn Thái"), 10));
        puzzlesList.add(new Puzzle(4, "Sông nào dài nhất Việt Nam?", 2, Arrays.asList("Sông Hồng", "Sông Mekong", "Sông Đồng Nai", "Sông Đà"), 5));
        puzzlesList.add(new Puzzle(5, "Di tích cố đô Huế thuộc tỉnh nào?", 1, Arrays.asList("Hà Nội", "Quảng Bình", "Thừa Thiên Huế", "Quảng Trị"), 5));
        puzzlesList.add(new Puzzle(6, "Thành phố nào được gọi là 'Thành phố Hoa phượng đỏ'?", 1, Arrays.asList("Hà Nội", "Hải Phòng", "Hải Dương", "Hạ Long"), 5));
        puzzlesList.add(new Puzzle(7, "Quốc hoa của Việt Nam là gì?", 1, Arrays.asList("Hoa sen", "Hoa mai", "Hoa đào", "Hoa hồng"), 5));
        puzzlesList.add(new Puzzle(8, "Phong Nha - Kẻ Bàng nằm ở tỉnh nào?", 2, Arrays.asList("Quảng Bình", "Quảng Trị", "Hà Tĩnh", "Nghệ An"), 5));
        puzzlesList.add(new Puzzle(9, "Đảo lớn nhất Việt Nam là đảo nào?", 2, Arrays.asList("Côn Đảo", "Phú Quốc", "Cát Bà", "Lý Sơn"), 5));
        puzzlesList.add(new Puzzle(10, "Ai là người sáng lập triều đại nhà Nguyễn?", 2, Arrays.asList("Nguyễn Huệ", "Nguyễn Ánh", "Nguyễn Nhạc", "Nguyễn Phúc Khoát"), 5));
        puzzlesList.add(new Puzzle(11, "Vịnh Hạ Long thuộc tỉnh nào?", 1, Arrays.asList("Quảng Ninh", "Hải Phòng", "Hà Nam", "Nam Định"), 5));
        puzzlesList.add(new Puzzle(12, "Thành phố nào được gọi là thành phố biển lớn nhất Việt Nam?", 2, Arrays.asList("Nha Trang", "Vũng Tàu", "Đà Nẵng", "Hải Phòng"), 5));
        puzzlesList.add(new Puzzle(13, "Dãy núi Trường Sơn nằm ở khu vực nào?", 2, Arrays.asList("Bắc Bộ", "Trung Bộ", "Nam Bộ", "Tây Bắc"), 5));
        puzzlesList.add(new Puzzle(14, "Ai là vị vua đầu tiên của nhà Lý?", 1, Arrays.asList("Lý Thái Tổ", "Lý Thánh Tông", "Lý Nhân Tông", "Lý Anh Tông"), 5));
        puzzlesList.add(new Puzzle(15, "Thành cổ Quảng Trị gắn liền với cuộc chiến năm nào?", 2, Arrays.asList("1971", "1972", "1973", "1974"), 5));
        puzzlesList.add(new Puzzle(16, "Đỉnh núi Fansipan nằm ở tỉnh nào?", 1, Arrays.asList("Lào Cai", "Hà Giang", "Lạng Sơn", "Điện Biên"), 5));
        puzzlesList.add(new Puzzle(17, "Ai là người được gọi là 'Hưng Đạo Đại Vương'?", 1, Arrays.asList("Trần Quốc Toản", "Trần Hưng Đạo", "Trần Nhân Tông", "Trần Quang Khải"), 5));
        puzzlesList.add(new Puzzle(18, "Nơi nào được mệnh danh là 'Tây Đô' của Việt Nam?", 1, Arrays.asList("Cần Thơ", "Hà Nội", "Huế", "Đà Nẵng"), 5));
        puzzlesList.add(new Puzzle(19, "Nhà Trần có tổng cộng bao nhiêu đời vua?", 2, Arrays.asList("10", "12", "14", "16"), 10));
        puzzlesList.add(new Puzzle(20, "Cột cờ Lũng Cú thuộc tỉnh nào?", 2, Arrays.asList("Cao Bằng", "Hà Giang", "Lào Cai", "Sơn La"), 5));
        puzzlesList.add(new Puzzle(21, "Ai là vị vua đã ban hành Bộ luật Hồng Đức?", 1, Arrays.asList("Lê Thánh Tông", "Lê Lợi", "Lê Hiển Tông", "Lê Nhân Tông"), 5));
        puzzlesList.add(new Puzzle(22, "Di tích lịch sử Ấp Bắc nằm ở tỉnh nào?", 1, Arrays.asList("Tiền Giang", "Đồng Tháp", "Cần Thơ", "An Giang"), 5));
        puzzlesList.add(new Puzzle(23, "Hồ lớn nhất Việt Nam là hồ nào?", 1, Arrays.asList("Hồ Ba Bể", "Hồ Tây", "Hồ Thác Bà", "Hồ Dầu Tiếng"), 5));
        puzzlesList.add(new Puzzle(24, "Triều đại nào xây dựng Kinh thành Huế?", 2, Arrays.asList("Nhà Nguyễn", "Nhà Lê", "Nhà Trần", "Nhà Tây Sơn"), 5));
        puzzlesList.add(new Puzzle(25, "Biển nào nằm ở phía đông Việt Nam?", 1, Arrays.asList("Biển Đông", "Biển Tây", "Biển Nam", "Biển Bắc"), 5));
        puzzlesList.add(new Puzzle(26, "Núi Bà Đen nằm ở tỉnh nào?", 1, Arrays.asList("Tây Ninh", "Bình Dương", "Bình Phước", "Đồng Nai"), 5));
        puzzlesList.add(new Puzzle(27, "Cuộc kháng chiến chống Nguyên - Mông lần thứ hai diễn ra vào năm nào?", 2, Arrays.asList("1283", "1285", "1287", "1288"), 5));
        puzzlesList.add(new Puzzle(28, "Tháp Bà Ponagar thuộc tỉnh nào?", 1, Arrays.asList("Khánh Hòa", "Ninh Thuận", "Bình Thuận", "Phú Yên"), 5));
        puzzlesList.add(new Puzzle(29, "Ai là người được gọi là 'Bình Định Vương'?", 2, Arrays.asList("Lê Lợi", "Nguyễn Huệ", "Quang Trung", "Trần Nhân Tông"), 5));
        puzzlesList.add(new Puzzle(30, "Sông Hồng bắt nguồn từ đâu?", 2, Arrays.asList("Trung Quốc", "Lào", "Việt Nam", "Thái Lan"), 5));
        puzzlesList.add(new Puzzle(31, "Hang Sơn Đoòng thuộc tỉnh nào?", 1, Arrays.asList("Quảng Bình", "Quảng Trị", "Thừa Thiên Huế", "Hà Tĩnh"), 5));
        puzzlesList.add(new Puzzle(32, "Ai là người lập ra nhà Đinh?", 1, Arrays.asList("Đinh Tiên Hoàng", "Đinh Bộ Lĩnh", "Lý Nam Đế", "Ngô Quyền"), 5));
        puzzlesList.add(new Puzzle(33, "Cầu Nhật Tân nằm ở thành phố nào?", 1, Arrays.asList("Hà Nội", "Hải Phòng", "Đà Nẵng", "Cần Thơ"), 5));
        puzzlesList.add(new Puzzle(34, "Ngọn núi nào được gọi là 'nóc nhà Đông Dương'?", 1, Arrays.asList("Fansipan", "Bạch Mã", "Tam Đảo", "Hoàng Liên Sơn"), 5));
        puzzlesList.add(new Puzzle(35, "Cuộc khởi nghĩa Lam Sơn do ai lãnh đạo?", 1, Arrays.asList("Lê Lợi", "Nguyễn Huệ", "Quang Trung", "Phạm Ngũ Lão"), 5));
        puzzlesList.add(new Puzzle(36, "Địa đạo Củ Chi nằm ở tỉnh nào?", 1, Arrays.asList("TP. Hồ Chí Minh", "Bình Dương", "Bình Phước", "Tây Ninh"), 5));
        puzzlesList.add(new Puzzle(37, "Quốc hiệu Việt Nam chính thức xuất hiện vào triều đại nào?", 1, Arrays.asList("Nhà Nguyễn", "Nhà Lý", "Nhà Trần", "Nhà Lê"), 5));
        puzzlesList.add(new Puzzle(38, "Chùa Một Cột nằm ở đâu?", 1, Arrays.asList("Hà Nội", "Huế", "Đà Nẵng", "Hải Phòng"), 5));
        puzzlesList.add(new Puzzle(39, "Ai là người đầu tiên đặt nền móng cho chữ quốc ngữ?", 2, Arrays.asList("Alexandre de Rhodes", "Pétrus Ký", "Trương Vĩnh Ký", "Nguyễn Trãi"), 5));
        puzzlesList.add(new Puzzle(40, "Ngã ba Đồng Lộc thuộc tỉnh nào?", 1, Arrays.asList("Hà Tĩnh", "Nghệ An", "Quảng Bình", "Quảng Trị"), 5));
        puzzlesList.add(new Puzzle(41, "Cố đô Hoa Lư thuộc tỉnh nào?", 1, Arrays.asList("Ninh Bình", "Thanh Hóa", "Hà Nam", "Nam Định"), 5));
        puzzlesList.add(new Puzzle(42, "Chiến thắng Bạch Đằng năm 938 do ai chỉ huy?", 1, Arrays.asList("Ngô Quyền", "Lý Bí", "Đinh Tiên Hoàng", "Trần Hưng Đạo"), 5));
        puzzlesList.add(new Puzzle(43, "Địa danh nào được gọi là 'thủ đô kháng chiến'?", 1, Arrays.asList("Tân Trào", "Đình Hồng", "Đoan Hùng", "Ba Đình"), 5));
        puzzlesList.add(new Puzzle(44, "Cánh đồng lớn nhất Việt Nam là gì?", 1, Arrays.asList("Cánh đồng Mường Thanh", "Cánh đồng Châu Đốc", "Cánh đồng Hồng Hà", "Cánh đồng Tây Nguyên"), 5));
        puzzlesList.add(new Puzzle(45, "Quần đảo Trường Sa thuộc tỉnh nào?", 1, Arrays.asList("Khánh Hòa", "Bình Thuận", "Quảng Ngãi", "Ninh Thuận"), 5));
        puzzlesList.add(new Puzzle(46, "Cửa khẩu Mộc Bài nằm ở tỉnh nào?", 1, Arrays.asList("Tây Ninh", "Lạng Sơn", "Lào Cai", "Cao Bằng"), 5));
        puzzlesList.add(new Puzzle(47, "Năm nào Chiến thắng Điện Biên Phủ diễn ra?", 1, Arrays.asList("1953", "1954", "1955", "1956"), 5));
        puzzlesList.add(new Puzzle(48, "Thác Bản Giốc nằm ở tỉnh nào?", 1, Arrays.asList("Cao Bằng", "Hà Giang", "Lạng Sơn", "Lào Cai"), 5));
        puzzlesList.add(new Puzzle(49, "Vua nào lập ra Văn Miếu - Quốc Tử Giám?", 2, Arrays.asList("Lý Thái Tổ", "Lý Nhân Tông", "Lý Thánh Tông", "Trần Nhân Tông"), 10));
        puzzlesList.add(new Puzzle(50, "Quốc lộ 1A nối từ tỉnh nào tới tỉnh nào?", 2, Arrays.asList("Hà Nội - Cà Mau", "Lạng Sơn - Cà Mau", "Hải Phòng - Cà Mau", "Quảng Ninh - Cà Mau"), 10));

        // xử lý push dữ liệu lên Firebase Realtime Database
        mReference.setValue(puzzlesList).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("PuzzleManager", "Puzzles data pushed successfully!");
            } else {
                Log.e("PuzzleManager", "Failed to push data:failure! " + task.getException().getMessage());
            }
        });
    }
    public void getPuzzlesData(final PuzzlesDataCallback callback) {
        mReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Puzzle> puzzlesList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Puzzle puzzle = snapshot.getValue(Puzzle.class);
                    if (puzzle != null) {
                        puzzlesList.add(puzzle);
                    }
                }
                // Trả kết quả qua callback
                callback.onSuccess(puzzlesList);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Trả lỗi qua callback
                callback.onFailure(databaseError.toException());
            }
        });
    }

    // Interface callback
    public interface PuzzlesDataCallback {
        void onSuccess(List<Puzzle> puzzlesList);
        void onFailure(Exception e);
    }
}
