package com.example.gametruytimkhobau;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

public class LeaderBoardFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_leader_board, container, false);
        //Sau khi Clone code về thì mặc định sẽ đứng ở nhánh Main
        //Để code theo issue thì cần tạo nhanh mới
        //Tạo theo số và tên của issue nhớ thêm feature/ vào
        //tạo xong nhánh mới thì code
        //của mình là create signupActivity nên mình sẽ tạo
    }
}