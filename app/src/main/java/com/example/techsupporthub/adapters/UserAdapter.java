package com.example.techsupporthub.adapters;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.techsupporthub.R;
import com.example.techsupporthub.models.UserModel;
import com.google.android.material.chip.Chip;

import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private final Context context;
    private final List<UserModel> userList;

    public UserAdapter(Context context, List<UserModel> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserModel user = userList.get(position);
        holder.tvName.setText(user.getUsername());
        holder.tvEmail.setText(user.getEmail());

        // Convert timestamp to readable date
        Date date = new Date(user.getCreatedAt());
        CharSequence formattedDate = DateFormat.format("yyyy-MM-dd HH:mm:ss", date);
        holder.tvCreatedAt.setText(formattedDate);
        holder.role.setText(user.getRole());

        // Load profile image
        Glide.with(context)
                .load(user.getProfile() != null ? user.getProfile() : R.drawable.profile)
                .placeholder(R.drawable.profile)
                .into(holder.ivProfile);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView ivProfile;
        TextView tvName, tvEmail, tvCreatedAt;
        Chip role;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfile = itemView.findViewById(R.id.iv_profile);
            tvName = itemView.findViewById(R.id.tv_name);
            tvEmail = itemView.findViewById(R.id.tv_email);
            tvCreatedAt = itemView.findViewById(R.id.tv_created_at);
            role = itemView.findViewById(R.id.role);
        }
    }
}