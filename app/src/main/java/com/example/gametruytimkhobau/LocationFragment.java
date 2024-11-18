package com.example.gametruytimkhobau;

import android.Manifest;
import android.annotation.SuppressLint;
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
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class LocationFragment extends Fragment implements OnMapReadyCallback {
    private FirebaseDatabase mDatabase;
    private DatabaseReference mReference;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    private String userId;
    User user;
    private GoogleMap map;
    private FusedLocationProviderClient fusedLocationClient;
    private LatLng currentLocation;
    private TreasureManager treasureManager;
    private Marker selectedTreasureMarker = null;
    private Button findTreasureButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_location, container, false);

        // Khởi tạo FusedLocationProviderClient để lấy vị trí cua minfh
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        // Khởi tạo mapơ
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        findTreasureButton = view.findViewById(R.id.btn_find_treasure);
        findTreasureButton.setOnClickListener(v -> findNearbyTreasure());

        super.onCreate(savedInstanceState); // gọi pthc onCreate đảm bảo cac logic của lớp cha được thực thii
        treasureManager = new TreasureManager();
        treasureManager = new TreasureManager();
        initViews();
        return view;
    }

    //Hàm khởi tạo
    private void initViews(){
        //lấy database realtime ra
        mDatabase = FirebaseDatabase.getInstance();
        //Lấy ra user hiện tại thông qua FAuth
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        userId = mUser.getUid();
        pushDataToFirebase(100);
    }
    private void pushDataToFirebase(int score) {
        mReference = mDatabase.getReference("users").child(userId);
        Map<String, Object> updates = new HashMap<>();
        updates.put("score", score);
        mReference.updateChildren(updates, (error, ref) -> {
            if (error == null) {
                Log.d("LocationFragment", "Score updated successfully");
            } else {
                Log.e("LocationFragment", "Failed to update score", error.toException());
                Toast.makeText(getActivity(), "Failed to update score: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
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
            // Lấy kho báu gần nhất so với vị trí hiện tại
            Marker closestMarker = treasureManager.getNearbyTreasureMarker(currentLocation);

            // Kiểm tra xem kho báu gần nhất có nằm trong khoảng cách dưới 30 mét không
            if (closestMarker != null && treasureManager.isWithinDistance(currentLocation, closestMarker, 50)) {
                String treasureTitle = closestMarker.getTitle();
                treasureManager.collectTreasure(closestMarker); // Xóa kho báu khỏi hệ thống
                closestMarker.remove(); // Xóa kho báu khỏi bản đồ
                Toast.makeText(getActivity(), "Bạn đã tìm thấy " + treasureTitle, Toast.LENGTH_SHORT).show();

                // lấy câu hỏi từ mỗi kho báu
                PuzzleDialogFragment puzzleDialog = new PuzzleDialogFragment();
                puzzleDialog.show(getActivity().getSupportFragmentManager(), "PuzzleDialog");


            } else {
                // Nếu không có kho báu nào trong khoảng cách dưới 30 mét, hiển thị thông tin về kho báu gần nhất
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
                    Toast.makeText(getActivity(), "Khoảng cách của bạn quá xa, hãy lại gần thêm ", Toast.LENGTH_SHORT).show();
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
}
