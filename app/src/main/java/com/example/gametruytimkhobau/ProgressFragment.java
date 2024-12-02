package com.example.gametruytimkhobau;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class ProgressFragment extends Fragment implements TaskAdapter.OnItemClickListener {
    private RecyclerView rcvTask;
    private TaskAdapter taskAdapter;
    private  TaskManager taskManager;
    private BottomNavigationView bottomNavigationView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_progress, container, false);
        initViews(view);
        return view;
    }

    private void initViews(View view) {
        rcvTask = view.findViewById(R.id.rcv_task);
        bottomNavigationView = getActivity().findViewById(R.id.bottom_nav);
        // Thiết lập LayoutManager cho RecyclerView
        rcvTask.setLayoutManager(new LinearLayoutManager(getActivity()));

        taskAdapter = new TaskAdapter(getActivity(), this);
        rcvTask.setAdapter(taskAdapter);

        taskManager = new TaskManager();

        // Kiểm tra và tạo nhiệm vụ hằng ngày
        taskManager.getUserFromFirebase(success -> {
            if (success) {
                taskManager.handleDailyTaskCreation((tasksLoaded, tasks) -> {
                    if (tasksLoaded && tasks != null) {
                        // Cập nhật dữ liệu vào RecyclerView
                        new Handler(Looper.getMainLooper()).post(() -> {
                            taskAdapter.setData(tasks);
                        });
                    } else {
                        Log.d("ProgressFragment", "No tasks to display.");
                    }
                });
            } else {
                Log.d("ProgressFragment", "Failed to load user data.");
            }
        });
    }

    @Override
    public void onItemClick(Task task) {
        LocationFragment locationFragment = new LocationFragment();
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.frl_main, locationFragment)
                .addToBackStack(null)
                .commit();
        // Xử lý chuyển fragment khi click biểu tượng tương ứng trên bottom menu
        bottomNavigationView.setSelectedItemId(R.id.action_location);
    }

    @Override
    public void onGetButtonClick(Task task) {
        // Cập nhật điểm trong Firebase
        PuzzleDialogFragment puzzleDialogFragment = new PuzzleDialogFragment();
        puzzleDialogFragment.UpdateScoreFirebase(task.getPoint());

        // Xóa task
        taskManager.deleteTask(task.getTaskId(), () -> {
            Log.d("onGetButtonClick", "Task has been deleted: " + task.getTaskId());

            // Sau khi xóa task, tải lại danh sách nhiệm vụ
            taskManager.getUserFromFirebase(success -> {
                if (success) {
                    taskManager.handleDailyTaskCreation((tasksLoaded, tasks) -> {
                        if (tasksLoaded && tasks != null) {
                            // Cập nhật lại dữ liệu vào RecyclerView
                            new Handler(Looper.getMainLooper()).post(() -> {
                                taskAdapter.setData(tasks); // Cập nhật dữ liệu mới vào adapter
                            });
                        } else {
                            Log.d("ProgressFragment", "No tasks to display.");
                        }
                    });
                } else {
                    Log.d("ProgressFragment", "Failed to load user data.");
                }
            });
        });
    }
}
