package com.example.gametruytimkhobau;

public class User {
    private String userId;
    private String email;
    private int rank;
    private int avatar;
    private String userName;
    private int score;

    public User() {
    }

    public User( String email, String userName) {
        this.email = email;
        this.userName = userName;
    }
    public User(String userId, String email, int rank, int avatar, String userName, int score) {
        this.userId = userId;
        this.email = email;
        this.rank = rank;
        this.avatar = avatar;
        this.userName = userName;
        this.score = score;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public int getAvatar() {
        return avatar;
    }

    public void setAvatar(int avatar) {
        this.avatar = avatar;
    }

    public String getUserName() {
        return userName;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

}
