package com.example.gametruytimkhobau;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskManager {
    private FirebaseDatabase mDatabase;
    private DatabaseReference mRefUser, mRefTreasure, mRefTasks, mRefLastTaskTime;
    private FirebaseAuth mAuth;
    private String userId;
    private double latitudeUser, longitudeUser;
    private List<Treasure> nearestTreasures;
    private List<Task> taskList;

    public TaskManager() {
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            userId = mAuth.getCurrentUser().getUid();
            mDatabase = FirebaseDatabase.getInstance();
            mRefUser = mDatabase.getReference("users").child(userId);
            mRefTreasure = mDatabase.getReference("treasures");
            mRefTasks = mDatabase.getReference("tasks").child(userId); // Store tasks under userId
            mRefLastTaskTime = mDatabase.getReference("lastTaskCreationTime").child(userId);
        } else {
            Log.e("TaskManager", "User not logged in!");
        }
    }

    public void getUserFromFirebase(OnUserLoadedListener listener) {
        if (mRefUser == null) return;
        mRefUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        latitudeUser = user.getLatitude();
                        longitudeUser = user.getLongitude();
                        listener.onUserLoaded(true);
                    }
                } else {
                    Log.d("TaskManager", "User not found");
                    listener.onUserLoaded(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TaskManager", "Failed to read user data", error.toException());
                listener.onUserLoaded(false);
            }
        });
    }

    public void getTreasureNearUser(int numberOfTreasures, OnTreasuresLoadedListener listener) {
        if (mRefTreasure == null) return;
        mRefTreasure.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    List<TreasureDistance> treasureDistances = new ArrayList<>();
                    for (DataSnapshot treasureSnapshot : snapshot.getChildren()) {
                        Treasure treasure = treasureSnapshot.getValue(Treasure.class);
                        if (treasure != null && treasure.isStatus()) {
                            double treasureLat = treasure.getLatitude();
                            double treasureLng = treasure.getLongitude();
                            float[] results = new float[1];
                            android.location.Location.distanceBetween(
                                    latitudeUser, longitudeUser,
                                    treasureLat, treasureLng,
                                    results
                            );
                            float distance = results[0];
                            treasureDistances.add(new TreasureDistance(treasure, distance));
                        }
                    }
                    treasureDistances.sort(Comparator.comparingDouble(TreasureDistance::getDistance));
                    nearestTreasures = new ArrayList<>();
                    for (int i = 0; i < Math.min(numberOfTreasures, treasureDistances.size()); i++) {
                        nearestTreasures.add(treasureDistances.get(i).getTreasure());
                    }
                    listener.onTreasuresLoaded(true, nearestTreasures);
                } else {
                    Log.d("TaskManager", "No treasures found");
                    listener.onTreasuresLoaded(false, null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TaskManager", "Failed to read treasures data", error.toException());
                listener.onTreasuresLoaded(false, null);
            }
        });
    }

    public void createListTask() {
        taskList = new ArrayList<>();
        if (nearestTreasures != null) {
            for (Treasure treasure : nearestTreasures) {
                Task task = new Task(userId, treasure.getId(), treasure.getTreasureName(), treasure.isStatus(), 100);
                taskList.add(task);
            }
        } else {
            Log.d("TaskManager", "No treasures found to generate tasks.");
        }
    }

    public void pushListTaskToFirebase(OnCompleteListener onCompleteListener) {
        if (taskList == null || taskList.isEmpty()) return;
        for (Task task : taskList) {
            String taskKey = mRefTasks.push().getKey();  // Tạo key mới cho từng task
            if (taskKey != null) {
                task.setTaskId(taskKey); // Lưu key vào đối tượng Task
                mRefTasks.child(taskKey).setValue(task)
                        .addOnSuccessListener(aVoid -> Log.d("TaskManager", "Task saved: " + taskKey))
                        .addOnFailureListener(e -> Log.e("TaskManager", "Failed to save task", e));
            }
        }
        onCompleteListener.onComplete();
    }

    public void fetchTasksFromFirebase(OnTasksLoadedListener listener) {
        if (mRefTasks == null) return;

        mRefTasks.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    List<Task> taskList = new ArrayList<>();
                    for (DataSnapshot taskSnapshot : snapshot.getChildren()) {
                        Task task = taskSnapshot.getValue(Task.class);
                        if (task != null) {
                            taskList.add(task);
                        }
                    }
                    listener.onTasksLoaded(true, taskList);
                } else {
                    Log.d("TaskManager", "No tasks found in database.");
                    listener.onTasksLoaded(false, null);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TaskManager", "Failed to fetch tasks from Firebase", error.toException());
                listener.onTasksLoaded(false, null);
            }
        });
    }

    public void handleDailyTaskCreation(OnTasksLoadedListener listener) {
        if (mRefLastTaskTime == null) return;

        mRefLastTaskTime.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String lastTaskDate = snapshot.getValue(String.class);
                String currentDate = getCurrentDate();

                // Nếu ngày cuối cùng tạo nhiệm vụ trùng với ngày hiện tại
                if (lastTaskDate != null && lastTaskDate.equals(currentDate)) {
                    Log.d("TaskManager", "Tasks for today already created. Fetching existing tasks.");
                    fetchTasksFromFirebase(listener); // Tải lại nhiệm vụ hiện tại
                    return; // Không tạo nhiệm vụ mới
                }

                // Xóa nhiệm vụ cũ và tạo mới
                deleteAllTasks(() -> {
                    getTreasureNearUser(10, (treasuresLoaded, treasures) -> {
                        if (treasuresLoaded) {
                            createListTask();
                            pushListTaskToFirebase(() -> {
                                // Cập nhật ngày tạo nhiệm vụ
                                mRefLastTaskTime.setValue(currentDate);
                                fetchTasksFromFirebase(listener); // Lấy danh sách nhiệm vụ mới
                            });
                        } else {
                            Log.d("TaskManager", "No treasures found to create tasks.");
                            listener.onTasksLoaded(false, null);
                        }
                    });
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TaskManager", "Failed to read lastTaskCreationTime", error.toException());
                listener.onTasksLoaded(false, null);
            }
        });
    }


    private void deleteAllTasks(OnCompleteListener onCompleteListener) {
        if (mRefTasks == null) return;
        mRefTasks.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("TaskManager", "All tasks deleted successfully.");
                onCompleteListener.onComplete();
            } else {
                Log.e("TaskManager", "Failed to delete tasks", task.getException());
            }
        });
    }
    public void deleteTask(String taskId, OnCompleteListener onCompleteListener) {
        if (mRefTasks == null) {
            Log.e("TaskManager", "Task reference is null. Cannot delete task.");
            return;
        }
        // Tìm và xóa task có ID tương ứng
        mRefTasks.child(taskId).removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("TaskManager", "Task deleted successfully: " + taskId);
                onCompleteListener.onComplete();
            } else {
                Log.e("TaskManager", "Failed to delete task: " + taskId, task.getException());
            }
        });
    }
    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    public interface OnUserLoadedListener {
        void onUserLoaded(boolean success);
    }

    public interface OnTreasuresLoadedListener {
        void onTreasuresLoaded(boolean success, List<Treasure> treasures);
    }

    public interface OnTasksLoadedListener {
        void onTasksLoaded(boolean success, List<Task> tasks);
    }

    public interface OnCompleteListener {
        void onComplete();
    }
}
