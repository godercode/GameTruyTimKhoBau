package com.example.gametruytimkhobau;

public class Task {
    private String taskId;
    private String userId;
    private int treasure_id;
    private String treasure_name;
    private boolean status;
    private int point;

    public Task() {
    }

    public Task(String userId, int treasure_id, String treasure_name, boolean status, int point) {
        this.userId = userId;
        this.treasure_id = treasure_id;
        this.treasure_name = treasure_name;
        this.status = status;
        this.point = point;
    }

    public String getTreasure_name() {
        return treasure_name;
    }

    public void setTreasure_name(String treasure_name) {
        this.treasure_name = treasure_name;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getTreasure_id() {
        return treasure_id;
    }

    public void setTreasure_id(int treasure_id) {
        this.treasure_id = treasure_id;
    }

    public int getPoint() {
        return point;
    }

    public void setPoint(int point) {
        this.point = point;
    }
}
