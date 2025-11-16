package com.example.m_hike.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.m_hike.activities.AddHikeActivity;
//import com.example.m_hike.activities.EditHikeActivity;
import com.example.m_hike.adapters.HikeAdapter;
import com.example.m_hike.database.DatabaseHelper;
import com.example.m_hike.models.Hike;
import com.example.m_hike.R;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements HikeAdapter.OnHikeActionListener {

    // UI Components
    private RecyclerView recyclerViewHikes;
    private LinearLayout emptyStateLayout;
    private EditText etSearch;
    private Button btnSearch, btnAddHike, btnDeleteAll;
    private TextView tvHikeCount;

    // Data
    private DatabaseHelper dbHelper;
    private HikeAdapter hikeAdapter;
    private List<Hike> hikeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_hike);

        // Initialize database
        dbHelper = new DatabaseHelper(this);

        // Initialize views
        initializeViews();

        // Setup RecyclerView
        setupRecyclerView();

        // Setup button listeners
        setupButtonListeners();

        // Load hikes from database
        loadHikes();
    }


     // Initialize all UI components

    private void initializeViews() {
        recyclerViewHikes = findViewById(R.id.recyclerViewHikes);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        etSearch = findViewById(R.id.etSearch);
        btnSearch = findViewById(R.id.btnSearch);
        btnAddHike = findViewById(R.id.btnAddHike);
        btnDeleteAll = findViewById(R.id.btnDeleteAll);
        tvHikeCount = findViewById(R.id.tvHikeCount);
    }


    // Setup RecyclerView with adapter and layout manager

    private void setupRecyclerView() {
        hikeList = new ArrayList<>();
        hikeAdapter = new HikeAdapter(this, hikeList, this);

        recyclerViewHikes.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewHikes.setAdapter(hikeAdapter);
        recyclerViewHikes.setHasFixedSize(true);
    }


    // Setup button click listeners

    private void setupButtonListeners() {
        // Add Hike button
        btnAddHike.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddHikeActivity.class);
            startActivity(intent);
        });

        // Search button
        btnSearch.setOnClickListener(v -> performSearch());

        // Delete All button
        btnDeleteAll.setOnClickListener(v -> showDeleteAllConfirmation());

        recyclerViewHikes.setOnClickListener(v -> {
            v.getId();
        });
    }


    // Load all hikes from database

    private void loadHikes() {
        hikeList.clear();
        hikeList.addAll(dbHelper.getAllHikes());
        hikeAdapter.notifyDataSetChanged();
        updateUI();
    }


    // Perform search by hike name

    private void performSearch() {
        String searchTerm = etSearch.getText().toString().trim();

        if (searchTerm.isEmpty()) {
            // If search is empty, show all hikes
            loadHikes();
            return;
        }

        // Search in database
        List<Hike> searchResults = dbHelper.searchHikesByName(searchTerm);

        if (searchResults.isEmpty()) {
            Toast.makeText(this, "No hikes found matching '" + searchTerm + "'",
                    Toast.LENGTH_SHORT).show();
        }

        // Update RecyclerView with search results
        hikeList.clear();
        hikeList.addAll(searchResults);
        hikeAdapter.notifyDataSetChanged();
        updateUI();
    }


    // Update UI based on hike list

    private void updateUI() {
        int count = hikeList.size();

        // Update count
        tvHikeCount.setText(count + (count == 1 ? " hike" : " hikes"));

        // Show/hide empty state
        if (count == 0) {
            recyclerViewHikes.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.VISIBLE);
        } else {
            recyclerViewHikes.setVisibility(View.VISIBLE);
            emptyStateLayout.setVisibility(View.GONE);
        }
    }


    // Show confirmation dialog before deleting all hikes

    private void showDeleteAllConfirmation() {
        if (hikeList.isEmpty()) {
            Toast.makeText(this, "No hikes to delete", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Delete All Hikes")
                .setMessage("Are you sure you want to delete all " + hikeList.size() +
                        " hike(s)? This action cannot be undone.")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Delete All", (dialog, which) -> deleteAllHikes())
                .setNegativeButton("Cancel", null)
                .show();
    }


    // Delete all hikes from database

    private void deleteAllHikes() {
        try {
            dbHelper.deleteAllHikes();
            hikeList.clear();
            hikeAdapter.notifyDataSetChanged();
            updateUI();
            Toast.makeText(this, "All hikes deleted successfully", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error deleting hikes: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }


    // Handle edit button click from adapter

    @Override
    public void onEditClick(Hike hike) {
        Intent intent = new Intent(MainActivity.this, EditHikeActivity.class);
        intent.putExtra("HIKE_ID", hike.getId());
        startActivity(intent);
    }


    // Handle delete button click from adapter

    @Override
    public void onDeleteClick(Hike hike, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Hike")
                .setMessage("Are you sure you want to delete \"" + hike.getName() + "\"?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Delete", (dialog, which) -> {
                    try {
                        dbHelper.deleteHike(hike.getId());
                        hikeAdapter.removeItem(position);
                        updateUI();
                        Toast.makeText(MainActivity.this,
                                "Hike deleted successfully", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this,
                                "Error deleting hike: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // Reload hikes when returning to this activity
    public void onRestart() {
        super.onRestart();
        loadHikes();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload hikes when returning to this activity
        loadHikes();
        // Clear search field
        etSearch.setText("");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}