package com.example.gametruytimkhobau;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Khởi tạo location frangment khi mainActivity được gọi
        replaceFragment(new LocationFragment());

        bottomNavigationView = findViewById(R.id.bottom_nav);
        // Xử lý chuyển fragment khi click biểu tượng tương ứng trên bottom menu
        bottomNavigationView.setSelectedItemId(R.id.action_location);
        bottomNavigationView.setOnItemSelectedListener(menuItem -> {
            Fragment selectedFragment = null;

            if (menuItem.getItemId() == R.id.action_user) {
                selectedFragment = new UserFragment();
            } else if (menuItem.getItemId() == R.id.action_leaderboard) {
                selectedFragment = new LeaderBoardFragment();
            } else if (menuItem.getItemId() == R.id.action_location) {
                selectedFragment = new LocationFragment();
            } else if (menuItem.getItemId() == R.id.action_progress) {
                selectedFragment = new ProgressFragment();
            }
            if (menuItem.getItemId() != R.id.action_location) {
                hideTreasureInfoFragment();
            }

            if (selectedFragment != null) {
                replaceFragment(selectedFragment);
            }
            return true;
        });
    }

    // Phương thức thay thế Fragment chính
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frl_main, fragment);
        fragmentTransaction.commit();
    }
    //Code này của Nam ko biết mục đich chắc là bất tắt thông tin kho báu
    private void hideTreasureInfoFragment() {
        Fragment treasureInfoFragment = getSupportFragmentManager().findFragmentById(R.id.info_fragment_container);
        if (treasureInfoFragment != null) {
            getSupportFragmentManager().beginTransaction().remove(treasureInfoFragment).commit();
            View infoFragmentContainer = findViewById(R.id.info_fragment_container);
            if (infoFragmentContainer != null) {
                infoFragmentContainer.setVisibility(View.GONE);
            }
        }
    }
}
