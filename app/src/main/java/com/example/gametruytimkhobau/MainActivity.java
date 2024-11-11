package com.example.gametruytimkhobau;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomNavigationView = findViewById(R.id.bottom_nav);
        replaceFragment(new LocationFragment());
        bottomNavigationView.setSelectedItemId(R.id.action_location);
        bottomNavigationView.setOnItemSelectedListener(menuItem -> {
            if(menuItem.getItemId() == R.id.action_user){
                replaceFragment(new UserFragment());
            }
            if(menuItem.getItemId() == R.id.action_leaderboard){
                replaceFragment(new LeaderBoardFragment());
            }
            if(menuItem.getItemId() == R.id.action_location){
                replaceFragment(new LocationFragment());
            }
            if(menuItem.getItemId() == R.id.action_progress){
                replaceFragment(new ProgressFragment());
            }
            if(menuItem.getItemId() == R.id.action_more){
                replaceFragment(new MoreFragment());
            }
            return true;
        });
    }
    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frl_main, fragment);
        fragmentTransaction.commit();
    }
}