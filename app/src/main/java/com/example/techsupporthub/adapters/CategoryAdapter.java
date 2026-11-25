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

import com.example.techsupporthub.R;
import com.example.techsupporthub.TechniciansCategoryActivity;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {
    private final Context context;
    private final ArrayList<String> categories;
    private final int[] categoryIcons;

    public CategoryAdapter(Context context, ArrayList<String> categories, int[] categoryIcons) {
        this.context = context;
        this.categories = categories;
        this.categoryIcons = categoryIcons;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.categoryName.setText(categories.get(position));
        holder.categoryIcon.setImageResource(categoryIcons[position]);
        holder.card.setOnClickListener(v -> {
            Intent intent = new Intent(context, TechniciansCategoryActivity.class);
            intent.putExtra("category", categories.get(position));
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView categoryIcon;
        TextView categoryName;
        MaterialCardView card;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryIcon = itemView.findViewById(R.id.categoryIcon);
            categoryName = itemView.findViewById(R.id.categoryName);
            card = itemView.findViewById(R.id.card);
        }
    }
}