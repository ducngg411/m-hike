package com.example.m_hike.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.m_hike.activities.MainActivity;
import com.example.m_hike.database.DatabaseHelper;
import com.example.m_hike.models.Hike;
import com.example.m_hike.R;

import java.util.ArrayList;
import java.util.List;

public class HikeDetailActivity extends AppCompatActivity {
    private ImageButton btnBack;
    private TextView tvDetailHikeName, tvDetailLocation, tvDetailDate,
            tvDetailParking, tvDetailLength, tvDetailDifficulty,
            tvDetailDescription, tvDetailDuration, tvDetailGroupSize;
    private CardView cardDescription;
    private Button btnEditHike, btnDeleteHike, btnViewObservations, btnAddObservation;
    private DatabaseHelper dbHelper;
    private Hike currentHike;
    private int hikeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hike_detail);

        // Get hike ID from intent
        hikeId = getIntent().getIntExtra("HIKE_ID", -1);
        if (hikeId == -1) {
            Toast.makeText(this, "Error: Invalid hike ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize database
        dbHelper = new DatabaseHelper(this);

        // Initialize views
        initializeViews();

        // Load hike details
        loadHikeDetails();

        // Setup button listeners
        setupButtonListeners();
    }

    // Initialize all UI components
    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);

        tvDetailHikeName = findViewById(R.id.tvDetailHikeName);
        tvDetailLocation = findViewById(R.id.tvDetailLocation);
        tvDetailDate = findViewById(R.id.tvDetailDate);
        tvDetailParking = findViewById(R.id.tvDetailParking);
        tvDetailLength = findViewById(R.id.tvDetailLength);
        tvDetailDifficulty = findViewById(R.id.tvDetailDifficulty);
        tvDetailDescription = findViewById(R.id.tvDetailDescription);
        tvDetailDuration = findViewById(R.id.tvDetailDuration);
        tvDetailGroupSize = findViewById(R.id.tvDetailGroupSize);

        cardDescription = findViewById(R.id.cardDescription);

        btnEditHike = findViewById(R.id.btnEditHike);
        btnDeleteHike = findViewById(R.id.btnDeleteHike);
        btnViewObservations = findViewById(R.id.btnViewObservations);
        btnAddObservation = findViewById(R.id.btnAddObservation);
    }

    // Load hike details from database
    private void loadHikeDetails() {
        currentHike = dbHelper.getHikeById(hikeId);
        if (currentHike == null) {
            Toast.makeText(this, "Error: Hike not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set data to views
        tvDetailHikeName.setText(currentHike.getName());
        tvDetailDifficulty.setText(currentHike.getDifficulty());
        tvDetailLocation.setText(currentHike.getLocation());
        tvDetailDate.setText(currentHike.getDate());
        tvDetailParking.setText(currentHike.getParkingAvailable());
        tvDetailLength.setText(currentHike.getLength() + " km");
        tvDetailDescription.setText(currentHike.getDescription());
        tvDetailDuration.setText(currentHike.getEstimatedDuration());

        // Set group size
        if (currentHike.getMaxGroupSize() > 0) {
            tvDetailGroupSize.setText(String.valueOf(currentHike.getMaxGroupSize() + "people"));
        } else {
            tvDetailGroupSize.setText("N/A");
        }

        // Set description
        String description = currentHike.getDescription();
        if (description != null || !description.trim().isEmpty()) {
            tvDetailDescription.setText(description);
            cardDescription.setVisibility(View.VISIBLE);
        } else {
            cardDescription.setVisibility(View.GONE);
        }

        // Set difficulty badge color
        setDifficultyColor(currentHike.getDifficulty());
    }

    // Set difficulty badge color based on level
    private void setDifficultyColor(String difficulty) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(20 * getResources().getDisplayMetrics().density);

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
        tvDetailDifficulty.setBackground(drawable);
    }

    // Setup button listeners
    private void setupButtonListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnEditHike.setOnClickListener(v -> {
            Intent intent = new Intent(HikeDetailActivity.this, EditHikeActivity.class);
            intent.putExtra("HIKE_ID", hikeId);
            startActivity(intent);
        });

        btnDeleteHike.setOnClickListener(v -> showDeleteConfirmation());

        // Add Observation button
        btnAddObservation.setOnClickListener(v -> {
            // TODO: Will implement in Feature C
            Toast.makeText(this, "Add Observation feature coming soon!",
                    Toast.LENGTH_SHORT).show();
        });

        // View Observations button
        btnViewObservations.setOnClickListener(v -> {
            // TODO: Will implement in Feature C
            Toast.makeText(this, "View Observations feature coming soon!",
                    Toast.LENGTH_SHORT).show();
        });
    }

    // Show delete confirmation dialog
    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Hike")
                .setMessage("Are you sure you want to delete \"" + currentHike.getName() + "\"?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteHike();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // Delete hike from database
    private void deleteHike() {
        try {
            dbHelper.deleteHike(hikeId);
            Toast.makeText(this, "Hike deleted successfully", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(HikeDetailActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "Error deleting hike: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload data when returning from edit
        loadHikeDetails();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}