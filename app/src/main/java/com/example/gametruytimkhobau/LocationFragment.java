package com.example.gametruytimkhobau;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocationFragment extends Fragment implements OnMapReadyCallback, PuzzleDialogFragment.OnScoreUpdateListener {
    private static final String TAG = "LocationFragment";

    private FirebaseDatabase mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private DatabaseReference userRef;

    private GoogleMap map;
    private FusedLocationProviderClient fusedLocationClient;
    private LatLng currentLocation;
    private TreasureManager treasureManager;
    private Marker selectedTreasureMarker = null;

    private Button findTreasureButton;
    private Button btnShowScore;

    private PuzzleManager puzzleManager;
    private List<Puzzle> puzzlesList;
    private Marker closestMarker;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_location, container, false);
        initFirebase();
        initViews(view);
        loadInitialData();
        return view;
    }

    private void initFirebase() {
        mDatabase = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        if (mUser != null) {
            userRef = mDatabase.getReference("users").child(mUser.getUid());
        } else {
            Log.e(TAG, "User is not logged in!");
            navigateToLogin();
        }
    }

    private void initViews(View view) {
        puzzleManager = new PuzzleManager();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        treasureManager = new TreasureManager();

        btnShowScore = view.findViewById(R.id.btn_show_score);
        findTreasureButton = view.findViewById(R.id.btn_find_treasure);
        findTreasureButton.setOnClickListener(v -> findNearbyTreasure());

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void loadInitialData() {
        if (puzzleManager != null) {
            //puzzleManager.pushPuzzlesDataToFirebase();
            fetchPuzzlesDataFromFirebase();
        }
        getScoreFromFirebase();
    }

    private void fetchPuzzlesDataFromFirebase() {
        puzzleManager.getPuzzlesData(new PuzzleManager.PuzzlesDataCallback() {
            @Override
            public void onSuccess(List<Puzzle> puzzles) {
                puzzlesList = puzzles;
                for (Puzzle puzzle : puzzles) {
                    Log.d(TAG, "Puzzle ID: " + puzzle.getPuzzle_id() + ", Question: " + puzzle.getQuestion());
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e(TAG, "Error fetching puzzles data: ", e);
            }
        });
    }

    private void getScoreFromFirebase() {
        if (userRef == null) {
            Toast.makeText(getActivity(), "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        int currentScore = user.getScore();
                        btnShowScore.setText(String.valueOf(currentScore));
                    } else {
                        Log.e(TAG, "User data is null");
                    }
                } else {
                    Toast.makeText(getActivity(), "Không có thông tin người chơi", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Không thể cập nhật điểm", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Firebase error: " + error.getMessage());
            }
        });
    }

    private void navigateToLogin() {
        if (getActivity() == null) return;
        Toast.makeText(getActivity(), "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(getActivity(), LoginActivity.class));
        getActivity().finish();
    }

    private void showRandomPuzzleDialog() {
        if (puzzlesList != null && !puzzlesList.isEmpty()) {
            Collections.shuffle(puzzlesList);
            Puzzle randomPuzzle = puzzlesList.get(0);

            PuzzleDialogFragment puzzleDialog = new PuzzleDialogFragment();
            puzzleDialog.setCurrentPuzzle(randomPuzzle, closestMarker);
            puzzleDialog.show(requireActivity().getSupportFragmentManager(), "PuzzleDialogFragment");
        } else {
            Toast.makeText(getActivity(), "Chưa có câu đố nào!", Toast.LENGTH_SHORT).show();
        }
    }

    public void hideFindTreasureButton(){
        if(findTreasureButton != null){
            findTreasureButton.setVisibility(View.GONE);
        }
    }

    public void showFindTreasureButton(){
        if(findTreasureButton != null){
            findTreasureButton.setVisibility(View.VISIBLE);
        }
    }

    private void findNearbyTreasure() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Yêu cầu quyền truy cập vị trí nếu chưa được cấp
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            return;
        }

        // Kiểm tra nếu vị trí ok
        if (currentLocation == null) {
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    // Cập nhật vị trí hiện tại
                    currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    Log.d("LocationFragment", "Success Update Location: " + currentLocation);
                    updateCurrentLocationToFirebase(currentLocation);
                    // Tiếp tục tìm kho báu sau khi cập nhật vị trí
                    findTreasureAfterLocationUpdate();
                } else {
                    Toast.makeText(getActivity(), "Không thể xác định vị trí hiện tại!", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                Log.e("LocationFragment", "Error getting location: " + e.getMessage());
                Toast.makeText(getActivity(), "Lỗi khi lấy vị trí hiện tại!", Toast.LENGTH_SHORT).show();
            });
        } else {
            // Nếu vị trí hiện tại ok thì tiếp tục tìm kho báu gần
            findTreasureAfterLocationUpdate();
            updateCurrentLocationToFirebase(currentLocation);
        }
    }

    private void updateCurrentLocationToFirebase(LatLng currentLocation) {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        double latitude = currentLocation.latitude;
                        double longitude = currentLocation.longitude;
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("latitude", latitude);
                        updates.put("longitude", longitude);
                        userRef.updateChildren(updates, (error, ref) -> {
                            if (error == null) {
                                Log.d("LocationFragment", "Location updated successfully");
                            } else {
                                Log.e("LocationFragment", "Failed to update Location", error.toException());
                            }
                        });
                    } else {
                        Log.e("LocationFragment", "Đối tượng user null");
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("LocationFragment", "Database error: " + error.getMessage());
            }
        });
    }
    private void findTreasureAfterLocationUpdate() {
        closestMarker = treasureManager.getNearbyTreasureMarker(currentLocation);

        if (closestMarker != null) {
            // Này tính khoảng cách đến kho báu gần nhất
            float[] results = new float[1];
            android.location.Location.distanceBetween(
                    currentLocation.latitude, currentLocation.longitude,
                    closestMarker.getPosition().latitude, closestMarker.getPosition().longitude,
                    results
            );

            float distanceToTreasure = results[0]; // Khoảng cách từ người chơi đến kho báu

            TreasureInfoFragment treasureInfoFragment = TreasureInfoFragment.newInstance(
                    closestMarker.getTitle(),
                    distanceToTreasure,
                    currentLocation,
                    closestMarker
            );

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.info_fragment_container, treasureInfoFragment)
                    .addToBackStack(null)
                    .commit();
//            hideFindTreasureButton();
            View infoFragmentContainer = getActivity().findViewById(R.id.info_fragment_container);
            if (infoFragmentContainer != null) {
                infoFragmentContainer.setVisibility(View.VISIBLE);
            }

            Toast.makeText(getActivity(), "Kho báu gần nhất cách bạn "+ Math.round(distanceToTreasure)+ "m", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity(), "Không tìm thấy kho báu nào gần bạn.", Toast.LENGTH_SHORT).show();
        }
    }





    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create()
                .setInterval(5000)
                .setFastestInterval(2000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }

                for (Location location : locationResult.getLocations()) {
                    currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 18));
                    updateDistanceToTreasures(currentLocation);  // cập nhật lại khoảng cách khi vịt rí bị load
                }
            }
        };

        // Yêu cầu cập nhật vị trí với FusedLocationProviderClient
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void updateDistanceToTreasures(LatLng userLocation) {
        for (Marker treasure : treasureManager.getAllTreasures()) {
            float[] results = new float[1];
            Location.distanceBetween(userLocation.latitude, userLocation.longitude,
                    treasure.getPosition().latitude, treasure.getPosition().longitude, results);

            if (results[0] < 30) {
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        // Khởi tạo và đồng bộ dữ liệu kho báu leen fbase laji đẻ nhiều máy cùng update
        treasureManager.pushDataToFBase(
                map,
                BitmapFactory.decodeResource(getResources(), R.drawable.pngkhobau)
        );

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);

            // get ra vị trí hiện tại
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 18));
                } else {
                    Log.e("LocationFragment", "Error Location.");
                    //Toast.makeText(getActivity(), "Vị trí hiện tại chưa sẵn sàng. Vui lòng thử lại sau!", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                Log.e("LocationFragment", "Error getting Location " + e.getMessage());
                //Toast.makeText(getActivity(), "Không thể lấy vị trí hiện tại!", Toast.LENGTH_SHORT).show();
            });

            // update vị trí liên tục
            startLocationUpdates();

        } else {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        // btn marker kho báu
        map.setOnMarkerClickListener(marker -> {
            selectedTreasureMarker = marker;

            if (currentLocation != null) {
                // Tính khoảng cách từ vị trí hiện tại đến marker
                float[] results = new float[1];
                Location.distanceBetween(
                        currentLocation.latitude, currentLocation.longitude,
                        marker.getPosition().latitude, marker.getPosition().longitude,
                        results
                );
                float distanceInMeters = results[0];

                showTreasureInfo(marker, distanceInMeters);
            } else {
                Toast.makeText(getActivity(), "Không xác định được vị trí hiện tại.", Toast.LENGTH_SHORT).show();
            }
            return false;
        });

        map.setOnMapClickListener(latLng -> hideTreasureInfo());
    }
    private void showTreasureInfo(Marker marker, float distanceInMeters) {
        String title = marker.getTitle();
        float distance = distanceInMeters;

        TreasureInfoFragment treasureInfoFragment = TreasureInfoFragment.newInstance(title, distance, currentLocation, marker);
        hideFindTreasureButton();

        getParentFragmentManager().beginTransaction()
                .replace(R.id.info_fragment_container, treasureInfoFragment)
                .addToBackStack(null)
                .commit();

        View infoFragmentContainer = getActivity().findViewById(R.id.info_fragment_container);
        if (infoFragmentContainer != null) {
            infoFragmentContainer.setVisibility(View.VISIBLE);
        }
    }

    private void hideTreasureInfo() {
        getParentFragmentManager().popBackStack();

        View infoFragmentContainer = getActivity().findViewById(R.id.info_fragment_container);
        if (infoFragmentContainer != null) {
            infoFragmentContainer.setVisibility(View.GONE);
        }
        showFindTreasureButton();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                map.setMyLocationEnabled(true);
                startLocationUpdates();
            }
        } else {
            Toast.makeText(getActivity(), "Lỗi truy cập", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onScoreUpdated(int newScore) {
        if (btnShowScore != null) {
            btnShowScore.setText(String.valueOf(newScore));
        }
    }
}
