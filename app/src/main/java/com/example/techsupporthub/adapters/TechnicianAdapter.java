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

import java.util.ArrayList;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class TechnicianAdapter extends RecyclerView.Adapter<TechnicianAdapter.ViewHolder> {
    private final Context context;
    private final ArrayList<UserModel> technicians;

    public TechnicianAdapter(Context context, ArrayList<UserModel> technicians) {
        this.context = context;
        this.technicians = technicians;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_technician, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserModel technician = technicians.get(position);
        holder.tvName.setText(technician.getUsername());
        holder.tvCategory.setText(technician.getCategory());
        holder.tvEmail.setText(technician.getEmail());
        holder.tvAbout.setText(technician.getAbout());

        // Format the createdAt timestamp into a readable date format
        Date date = new Date(technician.getCreatedAt());
        CharSequence formattedDate = DateFormat.format("yyyy-MM-dd HH:mm:ss", date);
        holder.tvCreatedAt.setText(formattedDate);  // Set the formatted date to the TextView

        // Load profile image if available
        if (technician.getProfile() != null) {
            Glide.with(context)
                    .load(technician.getProfile())
                    .placeholder(R.drawable.profile)
                    .into(holder.ivProfile);
        } else {
            holder.ivProfile.setImageResource(R.drawable.profile);
        }
    }

    @Override
    public int getItemCount() {
        return technicians.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CircleImageView ivProfile;
        TextView tvName, tvEmail, tvAbout, tvCreatedAt;  // Added tvCreatedAt for the formatted date
        Chip tvCategory;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfile = itemView.findViewById(R.id.iv_profile);
            tvName = itemView.findViewById(R.id.tv_name);
            tvCategory = itemView.findViewById(R.id.tv_category);
            tvEmail = itemView.findViewById(R.id.tv_email);
            tvAbout = itemView.findViewById(R.id.tv_about);  // TextView for About
            tvCreatedAt = itemView.findViewById(R.id.tv_created_at);  // TextView for Created At
        }
    }
}
