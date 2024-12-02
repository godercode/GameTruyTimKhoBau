package com.example.gametruytimkhobau;

import android.content.Context;
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
    private OnItemClickListener mListener;

    public TaskAdapter(Context mContext, OnItemClickListener listener) {
        this.mContext = mContext;
        this.mListener = listener;
    }
    public void setData(List<Task> list) {
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
        // Hiển thị hoặc ẩn nút btnGet
        if (!task.isStatus()) {
            holder.btnGet.setVisibility(View.VISIBLE);
            holder.btnGet.setOnClickListener(v -> mListener.onGetButtonClick(task));
        } else {
            holder.btnGet.setVisibility(View.GONE);
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
