package com.example.gametruytimkhobau;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Puzzle implements Serializable {
    private String question;
    private int correctAnswer; // Index của đáp án đúng (bắt đầu từ 0)
    private int point;
    private List<String> options;


    public Puzzle(String question, int correctAnswer, List<String> options, int point) {
        this.question = question;
        this.correctAnswer = correctAnswer;
        this.options = options;
        this.point = point;
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


    public static List<Puzzle> getAllPuzzles() {
        List<Puzzle> puzzlesList = new ArrayList<>();
        puzzlesList.add(new Puzzle(
                "5 + 3?",
                1,
                Arrays.asList("6", "8", "7", "9"),
                10
        ));
        puzzlesList.add(new Puzzle(
                "9 - 4?",
                0,
                Arrays.asList("5", "6", "4", "3"),
                10
        ));
        puzzlesList.add(new Puzzle(
                "6 × 2?",
                3,
                Arrays.asList("10", "11", "13", "12"),
                10
        ));
        puzzlesList.add(new Puzzle(
                "15 ÷ 3?",
                2,
                Arrays.asList("4", "6", "5", "3"),
                10
        ));
        puzzlesList.add(new Puzzle(
                "7 + 6 × 2?",
                3,
                Arrays.asList("26", "20", "25", "19"),
                10
        ));
        return puzzlesList;
    }


}
