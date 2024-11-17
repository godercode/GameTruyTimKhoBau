package com.example.gametruytimkhobau;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PlayerAdapter extends RecyclerView.Adapter<PlayerAdapter.UserViewHolder> {
    private Context mContext;
    private List<User> mListUser;

    public PlayerAdapter(Context mContext) {
        this.mContext = mContext;
    }
    public void setData(List<User> list){
        this.mListUser = list;
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_player, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = mListUser.get(position);
        if(user == null){
            return;
        }
        holder.tvRank.setText(String.valueOf(user.getRank()));
        holder.tvName.setText(user.getUserName());
        holder.tvScore.setText(String.valueOf(user.getScore()));
        holder.imgAvatar.setImageResource(user.getAvatar());
    }

    @Override
    public int getItemCount() {
        if(mListUser != null){
            return mListUser.size();
        }
        return 0;
    }

    public class UserViewHolder extends RecyclerView.ViewHolder{
        private TextView tvRank, tvName, tvScore;
        private ImageView imgAvatar;
        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank =itemView.findViewById(R.id.tv_rank);
            tvName =itemView.findViewById(R.id.tv_player);
            tvScore =itemView.findViewById(R.id.tv_score);
            imgAvatar =itemView.findViewById(R.id.img_player);
        }
    }
}