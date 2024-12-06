package com.example.gametruytimkhobau;

public class TreasureDistance {
    private final Treasure treasure;
    private final double distance;

    public TreasureDistance(Treasure treasure, double distance) {
        this.treasure = treasure;
        this.distance = distance;
    }

    public Treasure getTreasure() {
        return treasure;
    }

    public double getDistance() {
        return distance;
    }
}
