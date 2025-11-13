package com.example.m_hike.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.m_hike.R;
import com.example.m_hike.activities.HikeDetailActivity;
import com.example.m_hike.models.Hike;

import java.util.List;

public class HikeAdapter extends RecyclerView.Adapter<HikeAdapter.HikeViewHolder> {

    private Context context;
    private List<Hike> hikeList;
    private OnHikeActionListener listener;

    // Interface for handling actions
    public interface OnHikeActionListener {
        void onEditClick(Hike hike);
        void onDeleteClick(Hike hike, int position);
    }

    public HikeAdapter(Context context, List<Hike> hikeList, OnHikeActionListener listener) {
        this.context = context;
        this.hikeList = hikeList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public HikeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_hike, parent, false);
        return new HikeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HikeViewHolder holder, int position) {
        Hike hike = hikeList.get(position);

        // Set data
        holder.tvHikeName.setText(hike.getName());
        holder.tvLocation.setText(hike.getLocation());
        holder.tvDate.setText(hike.getDate());
        holder.tvLength.setText(hike.getLength() + " km");
        holder.tvParking.setText(hike.getParkingAvailable());
        holder.tvDifficulty.setText(hike.getDifficulty());

        // Set difficulty badge color based on level
        setDifficultyColor(holder.tvDifficulty, hike.getDifficulty());

        // View details button
        holder.btnViewDetails.setOnClickListener(v -> {
            Intent intent = new Intent(context, HikeDetailActivity.class);
            intent.putExtra("HIKE_ID", hike.getId());
            context.startActivity(intent);
        });

        // Edit button
        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClick(hike);
            }
        });

        // Delete button
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(hike, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return hikeList.size();
    }

    /**
     * Set badge color based on difficulty level
     */
    private void setDifficultyColor(TextView textView, String difficulty) {
        GradientDrawable drawable = new GradientDrawable();
        @ColorInt
        int color;
        switch (difficulty.toLowerCase()) {
            case "easy":
                color = Color.parseColor("#27AE60"); // Green
                break;
            case "moderate":
                color = Color.parseColor("#F39C12"); // Orange
                break;
            case "hard":
                color = Color.parseColor("#E74C3C"); // Red
                break;
            case "very hard":
                color = Color.parseColor("#8E44AD"); // Purple
                break;
            default:
                color = Color.parseColor("#95A5A6"); // Gray
        }
        drawable.setColor(color);
        textView.setBackground(drawable);
    }

    /**
     * Update the list of hikes
     */
    public void updateList(List<Hike> newList) {
        this.hikeList = newList;
        notifyDataSetChanged();
    }

    /**
     * Remove item at position
     */
    public void removeItem(int position) {
        hikeList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, hikeList.size());
    }

    /**
     * ViewHolder class
     */
    static class HikeViewHolder extends RecyclerView.ViewHolder {
        TextView tvHikeName, tvLocation, tvDate, tvLength, tvParking, tvDifficulty;
        Button btnViewDetails, btnEdit, btnDelete;

        public HikeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHikeName = itemView.findViewById(R.id.tvHikeName);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvLength = itemView.findViewById(R.id.tvLength);
            tvParking = itemView.findViewById(R.id.tvParking);
            tvDifficulty = itemView.findViewById(R.id.tvDifficulty);

            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}