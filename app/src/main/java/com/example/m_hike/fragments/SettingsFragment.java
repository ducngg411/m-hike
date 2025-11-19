package com.example.m_hike.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.m_hike.R;
import com.example.m_hike.activities.ProfileActivity;
import com.example.m_hike.database.DatabaseHelper;
import com.google.android.material.textfield.TextInputEditText;

public class SettingsFragment extends Fragment {

    // UI Components
    private TextInputEditText etUserName;
    private Spinner spinnerSortOrder;
    private Button btnSaveName, btnSavePreferences, btnClearAllData;

    // Data
    private SharedPreferences sharedPreferences;
    private DatabaseHelper dbHelper;

    // Sort options
    private final String[] sortOptions = {"Date (Newest First)", "Date (Oldest First)",
            "Name (A-Z)", "Name (Z-A)",
            "Length (Shortest First)", "Length (Longest First)"};

    private static final String TAG = "SettingsFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Initialize SharedPreferences
        sharedPreferences = requireActivity().getSharedPreferences("MHikePrefs", Context.MODE_PRIVATE);

        // Initialize database
        dbHelper = new DatabaseHelper(requireContext());

        // Initialize views
        initializeViews(view);

        // Setup spinner
        setupSpinner();

        // Load saved preferences
        loadPreferences();

        // Setup button listeners
        setupButtonListeners();

        return view;
    }

    private void initializeViews(View view) {
        etUserName = view.findViewById(R.id.etUserName);
        spinnerSortOrder = view.findViewById(R.id.spinnerSortOrder);
        btnSaveName = view.findViewById(R.id.btnSaveName);
        btnSavePreferences = view.findViewById(R.id.btnSavePreferences);
        btnClearAllData = view.findViewById(R.id.btnClearAllData);
    }

    // Setup spinner with sort options
    private void setupSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                sortOptions
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSortOrder.setAdapter(adapter);
    }


    // Load saved preferences from SharedPreferences
    private void loadPreferences() {
        // Load user name
        String userName = sharedPreferences.getString("user_name", "Hiker");
        etUserName.setText(userName);

        // Load sort order
        int sortOrderIndex = sharedPreferences.getInt("sort_order", 0);
        spinnerSortOrder.setSelection(sortOrderIndex);
    }

    // Setup button click listeners
    private void setupButtonListeners() {
        // Save name button
        btnSaveName.setOnClickListener(v -> saveUserName());

        // Save preferences button
        btnSavePreferences.setOnClickListener(v -> savePreferences());

        // Clear all data button
        btnClearAllData.setOnClickListener(v -> showClearDataConfirmation());
    }

    // Save user name to SharedPreferences
    private void saveUserName() {
        CharSequence nameCs = etUserName.getText();
        String name = nameCs != null ? nameCs.toString().trim() : "";

        if (name.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter your name", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save to SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("user_name", name);
        editor.apply();

        Toast.makeText(requireContext(), "Name saved successfully!", Toast.LENGTH_SHORT).show();

        // Update ProfileActivity header if needed
        if (getActivity() != null) {
            ((ProfileActivity) getActivity()).updateUserName(name);
        }
    }

    // Save preferences to SharedPreferences
    private void savePreferences() {
        int sortOrderIndex = spinnerSortOrder.getSelectedItemPosition();

        // Save to SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("sort_order", sortOrderIndex);
        editor.apply();

        Toast.makeText(requireContext(), "Preferences saved successfully!", Toast.LENGTH_SHORT).show();
    }

    private void showClearDataConfirmation() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Clear All Data")
                .setMessage("Are you sure you want to delete ALL hikes and observations? This action cannot be undone.")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Clear All", (dialog, which) -> deleteAllHikes())
                .setNegativeButton("Cancel", null)
                .show();
    }

    // Renamed from clearAllData: relies on ON DELETE CASCADE
    private void deleteAllHikes() {
        try {
            dbHelper.deleteAllHikes();
            Toast.makeText(requireContext(),
                    "All data cleared successfully",
                    Toast.LENGTH_LONG).show();
            if (getActivity() != null) {
                ((ProfileActivity) getActivity()).refreshStatistics();
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to clear all data", e);
            Toast.makeText(requireContext(),
                    "Error clearing data: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}