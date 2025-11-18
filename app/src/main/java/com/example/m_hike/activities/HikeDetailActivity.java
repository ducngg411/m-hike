package com.example.m_hike.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.m_hike.R;
import com.example.m_hike.database.DatabaseHelper;
import com.example.m_hike.models.Hike;
import com.google.android.material.appbar.MaterialToolbar;

public class HikeDetailActivity extends AppCompatActivity {
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
        MaterialToolbar toolbar = findViewById(R.id.toolbar); // reverted id
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("Hike Details");
            }
            toolbar.setNavigationOnClickListener(v -> finish());
        }

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
            Toast.makeText(this, R.string.na, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        tvDetailHikeName.setText(currentHike.getName());
        tvDetailDifficulty.setText(currentHike.getDifficulty());
        tvDetailLocation.setText(currentHike.getLocation());
        tvDetailDate.setText(currentHike.getDate());
        tvDetailParking.setText(currentHike.getParkingAvailable());
        tvDetailLength.setText(getString(R.string.length_km, String.valueOf(currentHike.getLength())));
        tvDetailDuration.setText(currentHike.getEstimatedDuration());
        // Group size
        if (currentHike.getMaxGroupSize() > 0) {
            tvDetailGroupSize.setText(getString(R.string.group_size_people, currentHike.getMaxGroupSize()));
        } else {
            tvDetailGroupSize.setText(getString(R.string.na));
        }
        String description = currentHike.getDescription();
        if (description != null && !description.trim().isEmpty()) {
            tvDetailDescription.setText(description);
            cardDescription.setVisibility(View.VISIBLE);
        } else {
            cardDescription.setVisibility(View.GONE);
        }
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

        btnEditHike.setOnClickListener(v -> {
            Intent intent = new Intent(HikeDetailActivity.this, EditHikeActivity.class);
            intent.putExtra("HIKE_ID", hikeId);
            startActivity(intent);
        });

        btnDeleteHike.setOnClickListener(v -> showDeleteConfirmation());

        // Add Observation button
        btnAddObservation.setOnClickListener(v -> {
            Intent intent = new Intent(HikeDetailActivity.this, AddObservationActivity.class);
            intent.putExtra("HIKE_ID", hikeId);
            startActivity(intent);
        });

        // View Observations button
        btnViewObservations.setOnClickListener(v -> {
            Intent intent = new Intent(HikeDetailActivity.this, ObservationListActivity.class);
            intent.putExtra("HIKE_ID", hikeId);
            startActivity(intent);
        });
    }

    // Show delete confirmation dialog
    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_hike_dialog_title)
                .setMessage(getString(R.string.delete_hike_dialog_message, currentHike.getName()))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(R.string.delete, (dialog, which) -> deleteHike())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    // Delete hike from database
    private void deleteHike() {
        try {
            dbHelper.deleteHike(hikeId);
            Toast.makeText(this, R.string.hike_deleted_success, Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(HikeDetailActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } catch (Exception e) {
            Toast.makeText(this, "Error deleting hike: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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