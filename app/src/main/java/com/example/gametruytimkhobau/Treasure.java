package com.example.gametruytimkhobau;

public class Treasure {
    private int treasure_id;
    private String treasure_name;
    private boolean status;
    private double latitude;
    private double longitude;

    public Treasure() {
    }

    public Treasure(int treasure_id, String treasure_name, boolean status, double latitude, double longitude) {
        this.treasure_id = treasure_id;
        this.treasure_name = treasure_name;
        this.status = status;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public int getId() {return treasure_id;}

    public void setId(int treasure_id) {this.treasure_id = treasure_id;}

    public String getTreasureName() {return treasure_name;}

    public void setTreasureName(String treasure_name) {this.treasure_name = treasure_name;}

    public boolean isStatus() {return status;}

    public void setStatus(boolean status) {this.status = status;}

    public double getLatitude() {return latitude;}

    public void setLatitude(double latitude) {this.latitude = latitude;}

    public double getLongitude() {return longitude;}

    public void setLongitude(double longitude) {this.longitude = longitude;}
}
