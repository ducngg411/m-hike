package com.example.m_hike.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.m_hike.R;
import com.example.m_hike.activities.HikeDetailActivity;
import com.example.m_hike.models.Hike;

import java.util.List;
import java.util.Locale;

public class UpcomingHikeAdapter extends RecyclerView.Adapter<UpcomingHikeAdapter.ViewHolder> {

    private Context context;
    private List<Hike> hikeList;

    public UpcomingHikeAdapter(Context context, List<Hike> hikeList) {
        this.context = context;
        this.hikeList = hikeList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_upcoming_hike, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Hike hike = hikeList.get(position);
        holder.tvHikeName.setText(hike.getName());
        holder.tvHikeDate.setText(hike.getDate());
        holder.tvHikeLocation.setText(hike.getLocation());
        holder.tvHikeLength.setText(String.format(Locale.getDefault(), "%.1f km", hike.getLength()));

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, HikeDetailActivity.class);
            intent.putExtra("HIKE_ID", hike.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return hikeList.size();
    }

    public void updateList(List<Hike> newHikeList) {
        this.hikeList = newHikeList;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvHikeName, tvHikeDate, tvHikeLocation, tvHikeLength;

        ViewHolder(View itemView) {
            super(itemView);
            tvHikeName = itemView.findViewById(R.id.tvUpcomingHikeName);
            tvHikeDate = itemView.findViewById(R.id.tvUpcomingHikeDate);
            tvHikeLength = itemView.findViewById(R.id.tvUpcomingHikeLength);
        }
    }
}