package com.example.gametruytimkhobau;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TreasureManager {
    private List<Marker> treasureMarkers = new ArrayList<>();

    public void createTreasureLocations(GoogleMap mMap, LatLng centerLocation, LocationFragment context) {
        Random random = new Random();

        for (int i = 0; i < 20; i++) {
            double[] offsets = randomOffset(random, 1, 100, centerLocation);
            LatLng treasureLocation = new LatLng(centerLocation.latitude + offsets[0], centerLocation.longitude + offsets[1]);

            Bitmap originalBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.pngkhobau);
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, 100, 100, false);

            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(treasureLocation)
                    .title("Kho báu #" + (i + 1))
                    .icon(BitmapDescriptorFactory.fromBitmap(scaledBitmap)));

            treasureMarkers.add(marker);
        }
    }

    private double[] randomOffset(Random random, int minDistance, int maxDistance, LatLng centerLocation) {
        double latitude = centerLocation.latitude;
        double metersPerDegreeLatitude = 111320;
        double metersPerDegreeLongitude = 111320 * Math.cos(Math.toRadians(latitude));

        double latOffset = (minDistance + (random.nextDouble() * (maxDistance - minDistance))) / metersPerDegreeLatitude;
        double lngOffset = (minDistance + (random.nextDouble() * (maxDistance - minDistance))) / metersPerDegreeLongitude;

        latOffset = random.nextBoolean() ? latOffset : -latOffset;
        lngOffset = random.nextBoolean() ? lngOffset : -lngOffset;

        return new double[]{latOffset, lngOffset};
    }

    public Marker getNearbyTreasureMarker(LatLng currentLocation) {
        for (Marker treasureMarker : treasureMarkers) {
            LatLng treasureLocation = treasureMarker.getPosition();

            float[] results = new float[1];
            android.location.Location.distanceBetween(currentLocation.latitude, currentLocation.longitude,
                    treasureLocation.latitude, treasureLocation.longitude, results);

            if (results[0] < 50) {
                return treasureMarker;
            }
        }
        return null;
    }

    public void collectTreasure(Marker treasureMarker) {
        if (treasureMarker != null) {
            treasureMarker.remove();
            treasureMarkers.remove(treasureMarker);
        }
    }
    public boolean isWithinDistance(LatLng currentLocation, Marker treasureMarker, float distance) {
        LatLng treasureLocation = treasureMarker.getPosition();
        float[] results = new float[1];
        android.location.Location.distanceBetween(
                currentLocation.latitude, currentLocation.longitude,
                treasureLocation.latitude, treasureLocation.longitude,
                results
        );
        return results[0] <= distance;
    }
    public List<Marker> getAllTreasures() {
        return treasureMarkers;
    }
    public Marker getClosestTreasure(LatLng currentLocation) {
        Marker closestMarker = null;
        float closestDistance = Float.MAX_VALUE;

        for (Marker treasureMarker : treasureMarkers) {
            LatLng treasureLocation = treasureMarker.getPosition();
            float[] results = new float[1];
            android.location.Location.distanceBetween(
                    currentLocation.latitude, currentLocation.longitude,
                    treasureLocation.latitude, treasureLocation.longitude, results
            );

            if (results[0] < closestDistance) {
                closestDistance = results[0];
                closestMarker = treasureMarker;
            }
        }
        return closestMarker;
    }



}
