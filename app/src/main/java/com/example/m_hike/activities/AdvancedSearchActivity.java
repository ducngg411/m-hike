package com.example.m_hike.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.m_hike.R;
import com.example.m_hike.adapters.HikeAdapter;
import com.example.m_hike.database.DatabaseHelper;
import com.example.m_hike.models.Hike;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import android.util.Log;

public class AdvancedSearchActivity extends AppCompatActivity
    implements HikeAdapter.OnHikeActionListener {

    private static final String TAG = "AdvancedSearchActivity"; // corrected constant name

    // UI components
    private TextInputEditText etSearchName, etSearchLocation, etSearchDate,
            etSearchLengthMin, etSearchLengthMax;
    private Button btnSearch, btnClearSearch;
    private RecyclerView recyclerViewSearchResults;
    private LinearLayout emptySearchLayout;
    private TextView tvSearchResultCount;

    // Data
    private DatabaseHelper dbHelper;
    private HikeAdapter searchAdapter;
    private List<Hike> searchResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advanced_search);

        // Setup toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("Advanced Search");
            }
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        // Initialize database
        dbHelper = new DatabaseHelper(this);

        // Initialize views
        initializeViews();

        // Setup button listeners
        setupButtonListeners();

        // Setup RecyclerView
        setupRecyclerView();

        // Setup date picker for date input
        setupDatePicker();
    }

    private void initializeViews() {
        etSearchName = findViewById(R.id.etSearchName);
        etSearchLocation = findViewById(R.id.etSearchLocation);
        etSearchDate = findViewById(R.id.etSearchDate);
        etSearchLengthMin = findViewById(R.id.etSearchLengthMin);
        etSearchLengthMax = findViewById(R.id.etSearchLengthMax);
        btnSearch = findViewById(R.id.btnSearch);
        btnClearSearch = findViewById(R.id.btnClearSearch);
        recyclerViewSearchResults = findViewById(R.id.recyclerViewSearchResults);
        emptySearchLayout = findViewById(R.id.emptySearchLayout);
        tvSearchResultCount = findViewById(R.id.tvSearchResultCount);
    }

    // Setup RecyclerView with adapter and layout manager
    private void setupRecyclerView() {
        searchResults = new ArrayList<>();
        searchAdapter = new HikeAdapter(this, searchResults, this);

        recyclerViewSearchResults.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewSearchResults.setAdapter(searchAdapter);
        recyclerViewSearchResults.setHasFixedSize(true);
    }

    // Setup date picker
    private void setupDatePicker() {
        etSearchDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String date = String.format(Locale.getDefault(), "%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear);
                    etSearchDate.setText(date);
                }, year, month, day);
            datePickerDialog.show();
        });
    }

    // Setup button click listeners
    private void setupButtonListeners() {
        // Search button
        btnSearch.setOnClickListener(v -> performAdvancedSearch());

        // Clear button
        btnClearSearch.setOnClickListener(v -> clearSearchFields());
    }

    // Perform advanced search
    private void performAdvancedSearch() {
        String name = safeText(etSearchName);
        String location = safeText(etSearchLocation);
        String date = safeText(etSearchDate);
        String minLengthStr = safeText(etSearchLengthMin);
        String maxLengthStr = safeText(etSearchLengthMax);

        // Check if at least one criterion
        if (name.isEmpty() && location.isEmpty() && date.isEmpty() && minLengthStr.isEmpty() && maxLengthStr.isEmpty()) {
            Toast.makeText(this, "Please enter at least one search criteria", Toast.LENGTH_SHORT).show();
            return;
        }

        // Parse length values
        Double minLength = null;
        Double maxLength = null;

        try {
            if (!minLengthStr.isEmpty()) {
                minLength = Double.parseDouble(minLengthStr);
                if (minLength < 0) {
                    Toast.makeText(this, "Minimum length cannot be negative", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            if (!maxLengthStr.isEmpty()) {
                maxLength = Double.parseDouble(maxLengthStr);
                if (maxLength < 0) {
                    Toast.makeText(this, "Maximum length cannot be negative", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            if (minLength != null && maxLength != null && minLength > maxLength) {
                Toast.makeText(this, "Minimum length cannot be greater than maximum length", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numbers for length", Toast.LENGTH_SHORT).show();
            return;
        }

        // Perform search
        try {
            List<Hike> newResults = dbHelper.advancedSearchHikes(
                    name.isEmpty() ? null : name,
                    location.isEmpty() ? null : location,
                    date.isEmpty() ? null : date,
                    minLength,
                    maxLength
            );

            // Update reference & adapter
            searchResults = newResults;
            searchAdapter.updateList(newResults); // replaces notifyDataSetChanged()
            updateSearchResultsUI();

            // Show message
            if (searchResults.isEmpty()) {
                Toast.makeText(this, "No hikes found matching your criteria",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, searchResults.size() + " hike(s) found",
                        Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error performing search: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error performing search", e); // uses TAG
        }
    }

    // Clear all search
    private void clearSearchFields() {
        etSearchName.setText("");
        etSearchLocation.setText("");
        etSearchDate.setText("");
        etSearchLengthMin.setText("");
        etSearchLengthMax.setText("");
        searchResults = new ArrayList<>(); // new empty list
        searchAdapter.updateList(searchResults); // replaces notifyDataSetChanged()
        updateSearchResultsUI();

        Toast.makeText(this, "Search fields cleared", Toast.LENGTH_SHORT).show();
    }

    // Update search results UI
    private void updateSearchResultsUI() {
        int count = searchResults.size();

        tvSearchResultCount.setText(getResources().getQuantityString(R.plurals.search_results_found, count, count));

        if (count == 0) {
            recyclerViewSearchResults.setVisibility(View.GONE);
            emptySearchLayout.setVisibility(View.VISIBLE);
        } else {
            recyclerViewSearchResults.setVisibility(View.VISIBLE);
            emptySearchLayout.setVisibility(View.GONE);
        }
    }

    // Handle edit hike click
    @Override
    public void onEditClick(Hike hike) {
        Intent intent = new Intent(AdvancedSearchActivity.this,
                EditHikeActivity.class);
        intent.putExtra("HIKE_ID", hike.getId());
        startActivity(intent);
    }

    // Handle delete hike click
    @Override
    public void onDeleteClick(Hike hike, int position) {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Delete Hike")
                .setMessage("Are you sure you want to delete \"" + hike.getName() + "\"?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Delete", (dialog, which) -> {
                    try {
                        dbHelper.deleteHike(hike.getId());
                        searchAdapter.removeItem(position);
                        updateSearchResultsUI();
                        Toast.makeText(AdvancedSearchActivity.this,
                                "Hike deleted successfully",
                                Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(AdvancedSearchActivity.this,
                                "Error deleting hike: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        Log.e("AdvancedSearch", "Error deleting hike", e);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh results if any changes were made
        if (!searchResults.isEmpty()) {
            performAdvancedSearch();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    private String safeText(TextInputEditText edit) {
        CharSequence cs = edit.getText();
        return cs != null ? cs.toString().trim() : "";
    }
}