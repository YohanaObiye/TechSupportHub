package com.example.techsupporthub.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.techsupporthub.R;
import com.example.techsupporthub.model.Workshop;

import java.util.List;

public class WorkshopAdapter extends RecyclerView.Adapter<WorkshopAdapter.WorkshopViewHolder> {

    private final Context context;
    private final List<Workshop> workshopList;

    public WorkshopAdapter(Context context, List<Workshop> workshopList) {
        this.context = context;
        this.workshopList = workshopList;
    }

    @NonNull
    @Override
    public WorkshopViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_workshop, parent, false);
        return new WorkshopViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkshopViewHolder holder, int position) {
        Workshop workshop = workshopList.get(position);
        holder.titleTextView.setText(workshop.getTitle());
        holder.descriptionTextView.setText(workshop.getDescription());
        holder.startDateTextView.setText("Start Date: " + workshop.getStartDate());
        holder.endDateTextView.setText("End Date: " + workshop.getEndDate());

        // Load image using Glide
        Glide.with(context).load(workshop.getImageUrl()).into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return workshopList.size();
    }

    public static class WorkshopViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView titleTextView;
        TextView descriptionTextView;
        TextView startDateTextView;
        TextView endDateTextView;

        public WorkshopViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
            titleTextView = itemView.findViewById(R.id.title_text_view);
            descriptionTextView = itemView.findViewById(R.id.description_text_view);
            startDateTextView = itemView.findViewById(R.id.start_date_text_view);
            endDateTextView = itemView.findViewById(R.id.end_date_text_view);
        }
    }
}