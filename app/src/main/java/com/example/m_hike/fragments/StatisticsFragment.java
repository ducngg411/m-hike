package com.example.m_hike.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.m_hike.R;
import com.example.m_hike.adapters.UpcomingHikeAdapter;
import com.example.m_hike.database.DatabaseHelper;
import com.example.m_hike.models.Hike;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StatisticsFragment extends Fragment {

    // UI Components
    private TextView tvStatTotalHikes, tvStatTotalObs, tvStatTotalDistance;
    private TextView tvStatEasy, tvStatModerate, tvStatHard, tvStatVeryHard;
    private ProgressBar progressEasy, progressModerate, progressHard, progressVeryHard;
    private RecyclerView recyclerUpcomingHikes;
    private TextView tvNoUpcoming;

    // Data
    private DatabaseHelper dbHelper;
    private UpcomingHikeAdapter upcomingAdapter;

    private static final String TAG = "StatisticsFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);

        // Initialize database
        dbHelper = new DatabaseHelper(requireContext());

        // Initialize views
        initializeViews(view);

        // Setup RecyclerView
        setupRecyclerView();

        // Load statistics
        loadStatistics();

        return view;
    }

    private void initializeViews(View view) {
        // Overall stats
        tvStatTotalHikes = view.findViewById(R.id.tvStatTotalHikes);
        tvStatTotalObs = view.findViewById(R.id.tvStatTotalObs);
        tvStatTotalDistance = view.findViewById(R.id.tvStatTotalDistance);

        // Difficulty stats
        tvStatEasy = view.findViewById(R.id.tvStatEasy);
        tvStatModerate = view.findViewById(R.id.tvStatModerate);
        tvStatHard = view.findViewById(R.id.tvStatHard);
        tvStatVeryHard = view.findViewById(R.id.tvStatVeryHard);

        // Progress bars
        progressEasy = view.findViewById(R.id.progressEasy);
        progressModerate = view.findViewById(R.id.progressModerate);
        progressHard = view.findViewById(R.id.progressHard);
        progressVeryHard = view.findViewById(R.id.progressVeryHard);

        // Upcoming hikes
        recyclerUpcomingHikes = view.findViewById(R.id.recyclerUpcomingHikes);
        tvNoUpcoming = view.findViewById(R.id.tvNoUpcoming);
    }

    private void setupRecyclerView() {
        upcomingAdapter = new UpcomingHikeAdapter(requireContext(), new ArrayList<>());
        recyclerUpcomingHikes.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerUpcomingHikes.setAdapter(upcomingAdapter);
    }

    private void loadStatistics() {
        List<Hike> allHikes = dbHelper.getAllHikes();

        // Total hikes
        int totalHikes = allHikes.size();
        tvStatTotalHikes.setText(String.valueOf(totalHikes));

        // Total observations
        int totalObs = 0;
        for (Hike hike : allHikes) {
            totalObs += dbHelper.getObservationsForHike(hike.getId()).size();
        }
        tvStatTotalObs.setText(String.valueOf(totalObs));

        // Total distance
        double totalDistance = 0;
        for (Hike hike : allHikes) {
            totalDistance += hike.getLength();
        }
        tvStatTotalDistance.setText(String.format(Locale.getDefault(), "%.1f km", totalDistance));

        // Difficulty distribution (null-safe)
        int easyCount = 0, moderateCount = 0, hardCount = 0, veryHardCount = 0;

        for (Hike hike : allHikes) {
            String diffRaw = hike.getDifficulty();
            String difficulty = diffRaw == null ? "" : diffRaw.toLowerCase(Locale.getDefault());
            switch (difficulty) {
                case "easy":
                    easyCount++;
                    break;
                case "moderate":
                    moderateCount++;
                    break;
                case "hard":
                    hardCount++;
                    break;
                case "very hard":
                    veryHardCount++;
                    break;
                default:
                    // ignore unknown/empty difficulty
                    break;
            }
        }

        // Update difficulty counts
        tvStatEasy.setText(String.valueOf(easyCount));
        tvStatModerate.setText(String.valueOf(moderateCount));
        tvStatHard.setText(String.valueOf(hardCount));
        tvStatVeryHard.setText(String.valueOf(veryHardCount));

        // Update progress bars
        if (totalHikes > 0) {
            progressEasy.setProgress((easyCount * 100) / totalHikes);
            progressModerate.setProgress((moderateCount * 100) / totalHikes);
            progressHard.setProgress((hardCount * 100) / totalHikes);
            progressVeryHard.setProgress((veryHardCount * 100) / totalHikes);
        } else {
            progressEasy.setProgress(0);
            progressModerate.setProgress(0);
            progressHard.setProgress(0);
            progressVeryHard.setProgress(0);
        }

        // Load upcoming hikes
        loadUpcomingHikes(allHikes);
    }

    private void loadUpcomingHikes(List<Hike> allHikes) {
        List<Hike> upcomingHikes = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        Date today = new Date();

        for (Hike hike : allHikes) {
            try {
                String dateStr = hike.getDate();
                if (dateStr == null || dateStr.trim().isEmpty()) {
                    continue;
                }
                Date hikeDate = sdf.parse(dateStr);
                if (hikeDate != null && hikeDate.after(today)) {
                    upcomingHikes.add(hike);
                }
            } catch (Exception e) { // catch any parsing or null issues
                Log.e(TAG, "Failed to parse hike date for hike id=" + hike.getId(), e);
            }
        }

        if (upcomingHikes.isEmpty()) {
            recyclerUpcomingHikes.setVisibility(View.GONE);
            tvNoUpcoming.setVisibility(View.VISIBLE);
        } else {
            recyclerUpcomingHikes.setVisibility(View.VISIBLE);
            tvNoUpcoming.setVisibility(View.GONE);
            upcomingAdapter.updateList(upcomingHikes);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload statistics when fragment becomes visible
        loadStatistics();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}