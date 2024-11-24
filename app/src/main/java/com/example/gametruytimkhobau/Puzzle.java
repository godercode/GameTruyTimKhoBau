package com.example.gametruytimkhobau;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Puzzle implements Serializable {
    private int puzzle_id;
    private String question;
    private int correctAnswer; // Index của đáp án đúng (bắt đầu từ 0)
    private int point;
    private List<String> options;

    public Puzzle() {
    }

    public Puzzle(int puzzle_id, String question, int correctAnswer, List<String> options, int point) {
        this.puzzle_id = puzzle_id;
        this.question = question;
        this.correctAnswer = correctAnswer;
        this.options = options;
        this.point = point;
    }

    public int getPuzzle_id() {
        return puzzle_id;
    }

    public void setPuzzle_id(int puzzle_id) {
        this.puzzle_id = puzzle_id;
    }

    public String getQuestion() {
        return question;
    }

    public int getCorrectAnswer() {
        return correctAnswer;
    }

    public List<String> getOptions() {
        return options;
    }

    public int getPoint(){
        return point;
    }

}
