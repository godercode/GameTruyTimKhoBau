package com.example.gametruytimkhobau;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

public class TreasureInfoFragment extends Fragment {

    private String title;
    private float distance;
    private LatLng currentLocation;

    public interface OnTreasureFoundListener {
        void onTreasureFound();
    }

    private OnTreasureFoundListener treasureFoundListener;

    public static TreasureInfoFragment newInstance(String title, float distance, LatLng currentLocation) {
        TreasureInfoFragment fragment = new TreasureInfoFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putFloat("distance", distance);
        args.putParcelable("currentLocation", currentLocation);
        fragment.setArguments(args);
        return fragment;
    }


    public void setOnTreasureFoundListener(OnTreasureFoundListener listener) {
        this.treasureFoundListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_treasure_info, container, false);

        if (getArguments() != null) {
            title = getArguments().getString("title");
            distance = getArguments().getFloat("distance");
            currentLocation = getArguments().getParcelable("currentLocation"); // vị tríd
        }

        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.width = 830;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        view.setLayoutParams(params);

        TextView titleTextView = view.findViewById(R.id.treasure_title);
        TextView distanceTextView = view.findViewById(R.id.treasure_distance);
        Button findTreasureButton = view.findViewById(R.id.find_treasure_button);

        titleTextView.setText(title);
        distanceTextView.setText("Khoảng cách: " + Math.round(distance) + " m");

        findTreasureButton.setOnClickListener(v -> {
            if (distance <= 50) {
                Toast.makeText(requireContext(), "Chúc mừng, bạn đã tìm thấy kho báu " + title, Toast.LENGTH_SHORT).show();
                if (treasureFoundListener != null) {
                    treasureFoundListener.onTreasureFound();
                }
                getParentFragmentManager().beginTransaction().remove(TreasureInfoFragment.this).commit();
            } else {
                    Toast.makeText(requireContext(), "Khoảng cách của bạn quá xa, hãy lại gần thêm "+ Math.round(distance -50) + " m", Toast.LENGTH_SHORT).show();
                }
        });


        return view;
    }


}
