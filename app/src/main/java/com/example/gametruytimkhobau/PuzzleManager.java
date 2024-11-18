package com.example.gametruytimkhobau;


import java.util.List;
import java.util.Random;

public class PuzzleManager {
    private List<Puzzle> puzzlesList;

    public PuzzleManager() {
        // Khởi tạo danh sách câu đố bằng phương thức getAllPuzzles() từ lớp Puzzles
        puzzlesList = Puzzle.getAllPuzzles();
    }

    // Phương thức để lấy một câu đố ngẫu nhiên
    public Puzzle getRandomPuzzle() {
        if (puzzlesList == null || puzzlesList.isEmpty()) {
            return null; // Nếu danh sách rỗng, trả về null
        }
        Random random = new Random();
        return puzzlesList.get(random.nextInt(puzzlesList.size())); // Lấy một câu đố ngẫu nhiên từ danh sách
    }

    // Phương thức để thêm câu đố mới vào danh sách
    public void addPuzzle(Puzzle puzzle) {
        if (puzzlesList != null) {
            puzzlesList.add(puzzle); // Thêm câu đố vào danh sách
        }
    }

    // Phương thức để lấy tất cả câu đố trong danh sách
    public List<Puzzle> getAllPuzzles() {
        return puzzlesList;
    }
}

