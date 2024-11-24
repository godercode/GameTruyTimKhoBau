package com.example.gametruytimkhobau;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import java.util.List;

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
            puzzleManager.pushPuzzlesDataToFirebase();
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
        if (currentLocation != null) {
            // Lấy kho báu gần nhất
            closestMarker = treasureManager.getNearbyTreasureMarker(currentLocation);

            // Kiểm tra khoảng cách 50 mét
            if (closestMarker != null && treasureManager.isWithinDistance(currentLocation, closestMarker, 50)) {
                String treasureTitle = closestMarker.getTitle();
                Toast.makeText(getActivity(), "Bạn đã tìm thấy " + treasureTitle, Toast.LENGTH_SHORT).show();
                showRandomPuzzleDialog(); // Hiển thị câu hỏi, xử lý marker tại đây
            } else {
                // Hiển thị thông tin về kho báu gần nhất nếu không nằm trong 50m
                if (closestMarker != null) {
                    float[] results = new float[1];
                    android.location.Location.distanceBetween(
                            currentLocation.latitude, currentLocation.longitude,
                            closestMarker.getPosition().latitude, closestMarker.getPosition().longitude,
                            results
                    );

                    TreasureInfoFragment treasureInfoFragment = TreasureInfoFragment.newInstance(
                            closestMarker.getTitle(),
                            results[0],
                            currentLocation
                    );

                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.info_fragment_container, treasureInfoFragment)
                            .addToBackStack(null)
                            .commit();

                    View infoFragmentContainer = getActivity().findViewById(R.id.info_fragment_container);
                    if (infoFragmentContainer != null) {
                        infoFragmentContainer.setVisibility(View.VISIBLE);
                    }
                } else {
                    Toast.makeText(getActivity(), "Khoảng cách của bạn quá xa, hãy lại gần thêm", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(getActivity(), "Chưa xác định được vị trí hiện tại!", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create()
                .setInterval(2000)
                .setFastestInterval(1000)
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

        // Kiểm tra và yêu cầu quyền truy cập vị trí nếu chưa được cấp
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);

//            // Nút My Location và la bàn
//            map.getUiSettings().setMyLocationButtonEnabled(true);
//            map.getUiSettings().setCompassEnabled(true);
//            map.getUiSettings().setMapToolbarEnabled(true);

            startLocationUpdates();

            LocationCallback locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult == null) {
                        return;
                    }

                    for (Location location : locationResult.getLocations()) {
                        currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        treasureManager.createTreasureLocations(map, currentLocation, LocationFragment.this);
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 18));
                    }
                }
            };

            fusedLocationClient.requestLocationUpdates(LocationRequest.create(), locationCallback, null);
        } else {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        // nhấn vào kho baus
        map.setOnMarkerClickListener(marker -> {
            selectedTreasureMarker = marker;  // Đánh dấu kho báu được chọn

            // Tính khoảng cách từ vị trí hiện tại đến kho báu
            float[] results = new float[1];
            Location.distanceBetween(
                    currentLocation.latitude, currentLocation.longitude,
                    marker.getPosition().latitude, marker.getPosition().longitude,
                    results
            );
            float distanceInMeters = results[0];

            // Hiển thị thông tin chi tiết kho báu bằng Fragment
            showTreasureInfo(marker, distanceInMeters);
            return false;
        });

        map.setOnMapClickListener(latLng -> hideTreasureInfo());
    }

    private void showTreasureInfo(Marker marker, float distanceInMeters) {
        String title = marker.getTitle();
        float distance = distanceInMeters;

        TreasureInfoFragment treasureInfoFragment = TreasureInfoFragment.newInstance(title, distance, currentLocation);
        hideFindTreasureButton();

        treasureInfoFragment.setOnTreasureFoundListener(() -> {
            if (selectedTreasureMarker != null) {
                treasureManager.collectTreasure(selectedTreasureMarker);
                selectedTreasureMarker.remove();
                selectedTreasureMarker = null;
            } else {
                Marker closestMarker = treasureManager.getNearbyTreasureMarker(currentLocation);
                if (closestMarker != null && treasureManager.isWithinDistance(currentLocation, closestMarker, 50)) {
                    treasureManager.collectTreasure(closestMarker);
                    closestMarker.remove();
                    Toast.makeText(getActivity(), "Chúc mừng, bạn đã tìm thấy kho báu " + title + " cách bạn " + Math.round(distance) + "m", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "Không có kho báu nào ở cách bạn 50m!", Toast.LENGTH_SHORT).show();
                }
            }
            hideTreasureInfo();
        });

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
