package com.example.gametruytimkhobau;

import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class TreasureManager {
    private List<Marker> treasureMarkers = new ArrayList<>();
    private FirebaseDatabase mDatabase;
    private DatabaseReference mReference, mTreasureRef;

    public TreasureManager() {
        mDatabase = FirebaseDatabase.getInstance();
        mReference = mDatabase.getReference("puzzles");
        mTreasureRef = mDatabase.getReference("treasures");
    }

    public Bitmap resizeBitmap(Bitmap bitmap, int width, int height) {
        return Bitmap.createScaledBitmap(bitmap, width, height, false);
    }

    private double[] generateRandomOffset(Random random, int minDistance, int maxDistance, LatLng center) {
        double latitude = center.latitude;
        double metersPerDegreeLatitude = 111320; // Số mét mỗi độ vĩ
        double metersPerDegreeLongitude = 111320 * Math.cos(Math.toRadians(latitude)); // Số mét mỗi độ kinh

        // Tạo offset ngẫu nhiên trong khoảng từ minDistance đến maxDistance
        double latOffset = (minDistance + (random.nextDouble() * (maxDistance - minDistance))) / metersPerDegreeLatitude;
        double lngOffset = (minDistance + (random.nextDouble() * (maxDistance - minDistance))) / metersPerDegreeLongitude;

        // offset dương hoặc âm
        latOffset = random.nextBoolean() ? latOffset : -latOffset;
        lngOffset = random.nextBoolean() ? lngOffset : -lngOffset;

        return new double[]{latOffset, lngOffset};
    }

    private void randomNewTreasures(DatabaseReference treasuresRef) {
        Random random = new Random();
        LatLng center = new LatLng(20.977995, 105.834677); // Tọa độ trung tâm
        Map<String, Object> newTreasures = new HashMap<>();

        for (int i = 0; i < 50; i++) {
            double[] offsets = generateRandomOffset(random, 1, 200, center); // Random tọa độ cách 1 -> 200m
            double latitude = center.latitude + offsets[0];
            double longitude = center.longitude + offsets[1];

            Treasure treasure = new Treasure(
                    i + 1,
                    "Kho báu số " + (i + 1),
                    true,
                    latitude,
                    longitude
            );

            newTreasures.put(String.valueOf(treasure.getId()), treasure);
        }

        // Cập nhật Firebase
        treasuresRef.setValue(newTreasures).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("TreasureManager", "Random coordinates successful.");
            } else {
                Log.e("TreasureManager", "Lỗi random tọa độ mới: " + task.getException().getMessage());
            }
        });
    }

    // hamf push lên fbase
    public void pushDataToFBase(GoogleMap map, Bitmap originalBitmap) {
        DatabaseReference treasuresRef = FirebaseDatabase.getInstance().getReference("treasures");
        DatabaseReference stateRef = FirebaseDatabase.getInstance().getReference("state");

        // Kiểm tra trạng thái dữ liệu từ Firebase
        stateRef.child("dataInitialized").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Boolean dataInitialized = snapshot.getValue(Boolean.class);

                if (dataInitialized != null && dataInitialized) {
                    Log.d("TreasureManager", "Notification update Treasures.");
                } else {
                    Log.d("TreasureManager", "Data Treasures null, bigin update.");
                    randomNewTreasures(treasuresRef); // Random và lưu dữ liệu mới
                    stateRef.child("dataInitialized").setValue(true); // Đánh dấu dữ liệu đã được khởi tạo
                }

                // Lắng nghe thay đổi từ Firebase để đồng bộ trạng thái bản đồ
                syncMapWithFirebase(map, originalBitmap, treasuresRef);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TreasureManager", "Error check status: " + error.getMessage());
            }
        });
    }




    // Tải kho báu từ Firebase và hiển thị lên bản đồ
    public void loadTreasuresFromFbaseToMap(GoogleMap map, Bitmap originalBitmap) {
        DatabaseReference treasuresRef = FirebaseDatabase.getInstance().getReference("treasures");

        treasuresRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Log.e("TreasureManager", "Not found Treasures.");
                    return;
                }

                treasureMarkers.clear();

                // Xóa tất cả marker hiện tại khỏi bản đồ
                for (Marker marker : treasureMarkers) {
                    marker.remove();
                }

                boolean allTreasuresCollected = true; // Biến kiểm tra trạng thái

                for (DataSnapshot treasureSnapshot : snapshot.getChildren()) {
                    Treasure treasure = treasureSnapshot.getValue(Treasure.class);

                    if (treasure == null || !treasure.isStatus()) continue;

                    if (treasure.isStatus()) {
                        // Nếu có ít nhất một kho báu chưa thu thập thi false
                        allTreasuresCollected = false;
                    }

                    Bitmap resizedBitmap = resizeBitmap(originalBitmap, 120, 120); // Chỉnh kích thước

                    LatLng location = new LatLng(treasure.getLatitude(), treasure.getLongitude());
                    Marker marker = map.addMarker(new MarkerOptions()
                            .position(location)
                            .title(treasure.getTreasureName())
                            .snippet("ID: " + treasure.getId())
                            .icon(BitmapDescriptorFactory.fromBitmap(resizedBitmap)));

                    if (marker != null) {
                        marker.setTag(treasure);
                        treasureMarkers.add(marker);
                    }
                }

                if (allTreasuresCollected) {
                    // random lại khi all được thu thập
                    randomNewTreasures(treasuresRef);
                } else {
                    Log.d("TreasureManager", "abc");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TreasureManager", "Errror load Fbase " + error.getMessage());
            }
        });
    }


    private void syncMapWithFirebase(GoogleMap map, Bitmap originalBitmap, DatabaseReference treasuresRef) {
        treasuresRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Log.e("TreasureManager", "Not found data treasures.");
                    return;
                }

                for (Marker marker : treasureMarkers) {
                    marker.remove();
                }
                treasureMarkers.clear();

                // Duyệt qua tất cả kho báu trong Firebase
                for (DataSnapshot treasureSnapshot : snapshot.getChildren()) {
                    Treasure treasure = treasureSnapshot.getValue(Treasure.class);

                    if (treasure == null || !treasure.isStatus()) {
                        continue;
                    }

                    Bitmap resizedBitmap = resizeBitmap(originalBitmap, 120, 120);

                    LatLng location = new LatLng(treasure.getLatitude(), treasure.getLongitude());
                    Marker marker = map.addMarker(new MarkerOptions()
                            .position(location)
                            .title(treasure.getTreasureName())
                            .snippet("ID: " + treasure.getId())
                            .icon(BitmapDescriptorFactory.fromBitmap(resizedBitmap)));

                    if (marker != null) {
                        marker.setTag(treasure); // Gắn thông tin kho báu vào marker
                        treasureMarkers.add(marker); // Thêm vào danh sách marker
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TreasureManager", "Error Fbsase: " + error.getMessage());
            }
        });
    }






    public Marker getNearbyTreasureMarker(LatLng currentLocation) {
        if (currentLocation == null || treasureMarkers.isEmpty()) {
            Log.e("TreasureManager", "Location null or nun treasure.");
            return null;
        }

        Marker closestMarker = null;
        float closestDistance = Float.MAX_VALUE; // Khoảng cách lớn nhất để so sánh

        for (Marker treasureMarker : treasureMarkers) {
            if (treasureMarker.getTag() instanceof Treasure) {
                LatLng treasureLocation = treasureMarker.getPosition();

                float[] results = new float[1];
                android.location.Location.distanceBetween(
                        currentLocation.latitude, currentLocation.longitude,
                        treasureLocation.latitude, treasureLocation.longitude,
                        results
                );

                float distance = results[0];

                if (distance < closestDistance) { // Kiểm tra nếu khoảng cách nhỏ hơn khoảng cách gần nhất hiện tại
                    closestDistance = distance;
                    closestMarker = treasureMarker;
                }
            }
        }

        if (closestMarker != null) {
            Log.d("TreasureManager", "Kho bau gan nhat : " + closestMarker.getTitle() + " ở khoảng cách " + closestDistance + "m.");
        } else {
            Log.d("TreasureManager", "No found treasure.");
        }

        return closestMarker;
    }

    public void removeTreasureMarker(Marker marker) {
        if (marker != null) {
            if (treasureMarkers.contains(marker)) {
                treasureMarkers.remove(marker); // Cập nhật danh sách
                marker.remove();
                Log.d("TreasureManager", "Deleted marker: " + marker.getTitle());
            } else {
                Log.w("TreasureManager", "Marker dont exist: " + marker.getTitle());
            }
        } else {
            Log.e("TreasureManager", "Marker null.");
        }
    }





    // Xác định khoảng cách của kho báu
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
    public void getAllTresureFromFbase(final TreasureDataCallback callback){
        mTreasureRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Treasure> treasureList = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Treasure treasure= snapshot.getValue(Treasure.class);
                    if (treasure != null) {
                        treasureList.add(treasure);
                    }
                }
                // Trả kết quả qua callback
                callback.onSuccess(treasureList);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Trả lỗi qua callback
                callback.onFailure(databaseError.toException());
            }
        });
    }
    public interface TreasureDataCallback {
        void onSuccess(List<Treasure> treasureList);
        void onFailure(Exception e);
    }
    public List<Marker> getAllTreasures() {
        return treasureMarkers;
    }
}
