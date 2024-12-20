package com.example.gametruytimkhobau;

public class User {
    private String userId;
    private String email;
    private int rank;
    private String avatar;
    private String userName;
    private int score;
    private double latitude;
    private double longitude;

    public User() {
    }

    public User( String email, String userName) {
        this.email = email;
        this.userName = userName;
    }
    public User(String userId, String email, int rank, String avatar, String userName, int score) {
        this.userId = userId;
        this.email = email;
        this.rank = rank;
        this.avatar = avatar;
        this.userName = userName;
        this.score = score;
    }

    public User(String userId, String email, int rank, String avatar, String userName, int score, double latitude, double longitude) {
        this.userId = userId;
        this.email = email;
        this.rank = rank;
        this.avatar = avatar;
        this.userName = userName;
        this.score = score;
        this.latitude = latitude;
        this.longitude = longitude;
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

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
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

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
