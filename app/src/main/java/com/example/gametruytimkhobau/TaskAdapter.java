package com.example.gametruytimkhobau;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    private Context mContext;
    private List<Task> mListTask;
    private List<Treasure> mListTreasure;
    private OnItemClickListener mListener;

    public TaskAdapter(Context mContext, OnItemClickListener listener) {
        this.mContext = mContext;
        this.mListener = listener;
    }
    public void setData(List<Task> list, List<Treasure> listTreasure) {
        this.mListTreasure = listTreasure;
        this.mListTask = list;
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = mListTask.get(position);
        if (task == null) {
            return;
        }
        holder.tvSTT.setText(String.valueOf(position + 1));
        holder.tvTreasureName.setText(task.getTreasure_name());
        holder.tvPoint.setText(String.valueOf(task.getPoint()));
        holder.itemView.setOnClickListener(v -> mListener.onItemClick(task));

        // Tìm kho báu liên quan đến Task
        Treasure treasure = findTreasureById(mListTreasure, task.getTreasure_id());

        if (treasure != null) {
            // Kiểm tra nếu trạng thái chưa xử lý và đúng userId thì hiển thị nút
            if (!task.isStatus() && treasure.getUserId() != null && treasure.getUserId().equals(task.getUserId())) {
                holder.btnGet.setVisibility(View.VISIBLE);
                holder.btnGet.setOnClickListener(v -> mListener.onGetButtonClick(task));
            } else {
                holder.btnGet.setVisibility(View.GONE);
            }
        } else {
            holder.btnGet.setVisibility(View.GONE);
        }
        Log.d("TaskAdapter", "Binding Task: " + task.getTaskId());
        Log.d("TaskAdapter", "Treasure Name: " + task.getTreasure_name());
        Log.d("TaskAdapter", "Task UserId: " + task.getUserId());
        Log.d("TaskAdapter", "Task Status: " + task.isStatus());
        Log.d("TaskAdapter", "Treasure UserId: " + (treasure != null ? treasure.getUserId() : "null"));
    }
    public Treasure findTreasureById(List<Treasure> treasureList, int id) {
        if(treasureList != null) {
            for (Treasure treasure : treasureList) {
                if (treasure.getId() == id) { // So sánh id
                    return treasure; // Trả về đối tượng tìm thấy
                }
            }
            Log.e("TaskAdapter", "Treasure with id " + id + " not found in list.");
            return null;
        }
        else{
            Log.e("TaskAdapter","treasure list null");
            return null;
        }
    }

    @Override
    public int getItemCount() {
        return mListTask != null ? mListTask.size() : 0;
    }

    public class TaskViewHolder extends RecyclerView.ViewHolder{
        private TextView tvSTT, tvTreasureName, tvPoint;
        private Button btnGet;
        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSTT = itemView.findViewById(R.id.tv_stt);
            tvTreasureName = itemView.findViewById(R.id.tv_treasure_name);
            tvPoint = itemView.findViewById(R.id.tv_point);
            btnGet = itemView.findViewById(R.id.btn_get);
        }
    }
    public interface OnItemClickListener {
        void onItemClick(Task task);
        void onGetButtonClick(Task task);
    }
}
