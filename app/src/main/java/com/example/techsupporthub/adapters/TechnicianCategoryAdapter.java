package com.example.techsupporthub.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.techsupporthub.R;
import com.example.techsupporthub.TechnicianDetailsActivity;
import com.example.techsupporthub.models.UserModel;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;

public class TechnicianCategoryAdapter extends RecyclerView.Adapter<TechnicianCategoryAdapter.ViewHolder> {
    private final Context context;
    private final ArrayList<UserModel> techniciansList;

    public TechnicianCategoryAdapter(Context context, ArrayList<UserModel> techniciansList) {
        this.context = context;
        this.techniciansList = techniciansList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_technician_user_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserModel technician = techniciansList.get(position);
        holder.nameTextView.setText(technician.getUsername());
        holder.deviceCategoryChip.setText(technician.getCategory());
        Intent intent = new Intent(context, TechnicianDetailsActivity.class);
        intent.putExtra("userId", technician.getId());
        holder.cardView.setOnClickListener(v -> context.startActivity(intent));

        // Load profile image using Glide (if profile exists)
        if (technician.getProfile() != null && !technician.getProfile().isEmpty()) {
            Glide.with(context)
                    .load(technician.getProfile())
                    .placeholder(R.drawable.profile) // Default placeholder image
                    .into(holder.profileImageView);
        } else {
            holder.profileImageView.setImageResource(R.drawable.profile);
        }
    }

    @Override
    public int getItemCount() {
        return techniciansList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        Chip deviceCategoryChip;
        ImageView profileImageView;
        MaterialCardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.technicianName);
            deviceCategoryChip = itemView.findViewById(R.id.deviceCategory);
            profileImageView = itemView.findViewById(R.id.profileImage);
            cardView = itemView.findViewById(R.id.card);
        }
    }
}