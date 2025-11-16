package com.example.m_hike.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.m_hike.R;
import com.example.m_hike.models.Observation;

import java.util.List;

public class ObservationAdapter extends RecyclerView.Adapter<ObservationAdapter.ObservationViewHolder> {

    private Context context;
    private List<Observation> observationList;
    private OnObservationActionListener listener;

    // Interface for handling actions
    public interface OnObservationActionListener {
        void onEditClick(Observation observation);
        void onDeleteClick(Observation observation, int position);
    }

    public ObservationAdapter(Context context, List<Observation> observationList,
                              OnObservationActionListener listener) {
        this.context = context;
        this.observationList = observationList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ObservationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_observation, parent, false);
        return new ObservationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ObservationViewHolder holder, int position) {
        Observation observation = observationList.get(position);

        // Set data
        holder.tvObsTime.setText(observation.getTime());
        holder.tvObsObservation.setText(observation.getObservation());

        // Set comment if exists
        String comment = observation.getComment();
        if (comment != null && !comment.trim().isEmpty()) {
            holder.layoutComment.setVisibility(View.VISIBLE);
            holder.tvObsComment.setText(comment);
        } else {
            holder.layoutComment.setVisibility(View.GONE);
        }

        // Edit button click listener
        holder.btnEditObs.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClick(observation);
            }
        });

        // Delete button click listener
        holder.btnDeleteObs.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(observation, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return observationList.size();
    }

     // Update the entire list of observations

    public void updateList(List<Observation> newList) {
        this.observationList = newList;
        notifyDataSetChanged();
    }


    // Remove item at specific position

    public void removeItem(int position) {
        if (position >= 0 && position < observationList.size()) {
            observationList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, observationList.size());
        }
    }


     // ViewHolder class for Observation items

    static class ObservationViewHolder extends RecyclerView.ViewHolder {
        TextView tvObsTime, tvObsObservation, tvObsComment;
        LinearLayout layoutComment;
        Button btnEditObs, btnDeleteObs;

        public ObservationViewHolder(@NonNull View itemView) {
            super(itemView);

            // Initialize views
            tvObsTime = itemView.findViewById(R.id.tvObsTime);
            tvObsObservation = itemView.findViewById(R.id.tvObsObservation);
            tvObsComment = itemView.findViewById(R.id.tvObsComment);
            layoutComment = itemView.findViewById(R.id.layoutComment);

            btnEditObs = itemView.findViewById(R.id.btnEditObs);
            btnDeleteObs = itemView.findViewById(R.id.btnDeleteObs);
        }
    }
}
