package com.example.m_hike.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.m_hike.R;
import com.example.m_hike.adapters.ObservationAdapter;
import com.example.m_hike.database.DatabaseHelper;
import com.example.m_hike.models.Hike;
import com.example.m_hike.models.Observation;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;


public class ObservationListActivity extends AppCompatActivity
        implements ObservationAdapter.OnObservationActionListener {

    // UI Components
    private TextView tvObsListHikeName, tvObsCount;
    private RecyclerView recyclerViewObservations;
    private LinearLayout emptyObsLayout;
    private FloatingActionButton fabAddObservation;

    // Data
    private DatabaseHelper dbHelper;
    private ObservationAdapter observationAdapter;
    private List<Observation> observationList;
    private int hikeId;
    private Hike currentHike;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_observation_list);

        // Setup toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("Observations");
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

        // Load hike info
        loadHikeInfo();

        // Set up RecyclerView
        setupRecyclerView();

        // Setup button listeners
        setupButtonListeners();

        // Load observations from database
        loadObservations();

    }

    // Initialize all UI components
    private void initializeViews() {
        tvObsListHikeName = findViewById(R.id.tvObsListHikeName);
        tvObsCount = findViewById(R.id.tvObsCount);
        recyclerViewObservations = findViewById(R.id.recyclerViewObservations);
        emptyObsLayout = findViewById(R.id.emptyObsLayout);
        fabAddObservation = findViewById(R.id.fabAddObservation);
    }

    // Load hike information from database
    private void loadHikeInfo() {
        currentHike = dbHelper.getHikeById(hikeId);
        if (currentHike != null) {
            tvObsListHikeName.setText(currentHike.getName());
        } else {
            Toast.makeText(this, "Error: Hike not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    // Setup RecyclerView with adapter
    private void setupRecyclerView() {
        observationList = new ArrayList<>();
        observationAdapter = new ObservationAdapter(this, observationList, this);

        recyclerViewObservations.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewObservations.setAdapter(observationAdapter);
        recyclerViewObservations.setHasFixedSize(true);
    }

    // Setup button click listeners
    private void setupButtonListeners() {
        fabAddObservation.setOnClickListener(v -> {
            // Open AddObservationActivity}
            Intent intent = new Intent(ObservationListActivity.this, AddObservationActivity.class);
            intent.putExtra("HIKE_ID", hikeId);
            startActivity(intent);
        });
    }

    // Load observations from database
    private void loadObservations() {
        observationList.clear();
        observationList.addAll(dbHelper.getObservationsByHikeId(hikeId));
        observationAdapter.notifyDataSetChanged();
        updateUI();
    }

    // Update UI based on observation list
    private void updateUI() {
        int count = observationList.size();
        tvObsCount.setText(getResources().getQuantityString(R.plurals.observation_items, count, count));
        if (count == 0) {
            recyclerViewObservations.setVisibility(View.GONE);
            emptyObsLayout.setVisibility(View.VISIBLE);
        } else {
            recyclerViewObservations.setVisibility(View.VISIBLE);
            emptyObsLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onEditClick(Observation observation) {
        Intent intent = new Intent(ObservationListActivity.this,
                com.example.m_hike.activities.EditObservationActivity.class);
        intent.putExtra("OBSERVATION_ID", observation.getId());
        intent.putExtra("HIKE_ID", hikeId);
        startActivity(intent);
    }

    // Edit button click handler
    @Override
    public void onDeleteClick(Observation observation, int position) {
        new AlertDialog.Builder(this)
            .setTitle(R.string.delete_observation_dialog_title)
            .setMessage(R.string.delete_observation_dialog_message)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton(R.string.delete, (dialog, which) -> {
                try {
                    dbHelper.deleteObservation(observation.getId());
                    observationAdapter.removeItem(position);
                    updateUI();
                    Toast.makeText(this, R.string.observation_deleted_success, Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Log.e("ObservationList", "Delete failed", e);
                    Toast.makeText(this, "Error deleting observation: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload observations when returning to this activity
        loadObservations();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}
