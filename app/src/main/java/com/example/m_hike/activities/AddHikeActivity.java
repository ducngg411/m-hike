// java
package com.example.m_hike.activities;

import java.util.Locale;
import android.util.Log;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.m_hike.R;
import com.example.m_hike.database.DatabaseHelper;
import com.example.m_hike.models.Hike;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;

public class AddHikeActivity extends AppCompatActivity {
    // UI Components
    private TextInputEditText etHikeName, etLocation, etDate, etLength, etDescription, etGroupSize;
    private RadioGroup rgParking;
    private Spinner spinnerDifficulty, spinnerDuration;
    private Button btnSave, btnCancel;

    // Database
    private DatabaseHelper dbHelper;

    // Data arrays for spinners
    private final String[] difficultyLevels = {"Easy", "Moderate", "Hard", "Very Hard"};
    private final String[] durationOptions = {"Less than 1 hour", "1-2 hours", "2-4 hours",
            "4-6 hours", "6-8 hours", "More than 8 hours"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_hike);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("Add Hike");
            }
            toolbar.setNavigationOnClickListener(v -> finish());
        }
        dbHelper = new DatabaseHelper(this);
        initializeViews();
        setupSpinners();
        setupDatePicker();
        setupButtonListeners();
    }

    // Initialize all UI components
    private void initializeViews() {
        etHikeName = findViewById(R.id.etHikeName);
        etLocation = findViewById(R.id.etLocation);
        etDate = findViewById(R.id.etDate);
        etLength = findViewById(R.id.etLength);
        etDescription = findViewById(R.id.etDescription);
        etGroupSize = findViewById(R.id.etGroupSize);

        rgParking = findViewById(R.id.rgParking);
        spinnerDifficulty = findViewById(R.id.spinnerDifficulty);
        spinnerDuration = findViewById(R.id.spinnerDuration);

        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
    }

    // Setup spinners with data
    private void setupSpinners() {
        // Difficulty Spinner
        ArrayAdapter<String> difficultyAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, difficultyLevels);
        difficultyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDifficulty.setAdapter(difficultyAdapter);

        // Duration Spinner
        ArrayAdapter<String> durationAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, durationOptions);
        durationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDuration.setAdapter(durationAdapter);
    }

    // Setup date picker for date input
    private void setupDatePicker() {
        etDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            DatePickerDialog datePickerDialog = new DatePickerDialog(
                AddHikeActivity.this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String date = String.format(Locale.getDefault(), "%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear);
                    etDate.setText(date);
                }, year, month, day);
            datePickerDialog.show();
        });
    }

    // Setup click listeners for buttons
    private void setupButtonListeners() {
        btnSave.setOnClickListener(v -> validateAndShowConfirmation());
        btnCancel.setOnClickListener(v -> finish());
    }

    // Validate all required fields and show confirmation dialog
    private void validateAndShowConfirmation() {
        CharSequence nameCs = etHikeName.getText();
        String name = nameCs != null ? nameCs.toString().trim() : "";
        CharSequence locCs = etLocation.getText();
        String location = locCs != null ? locCs.toString().trim() : "";
        CharSequence dateCs = etDate.getText();
        String date = dateCs != null ? dateCs.toString().trim() : "";
        CharSequence lenCs = etLength.getText();
        String lengthStr = lenCs != null ? lenCs.toString().trim() : "";
        CharSequence descCs = etDescription.getText();
        String description = descCs != null ? descCs.toString().trim() : "";
        CharSequence groupCs = etGroupSize.getText();
        String groupSizeStr = groupCs != null ? groupCs.toString().trim() : "";

        int selectedParkingId = rgParking.getCheckedRadioButtonId();

        String difficulty = spinnerDifficulty.getSelectedItem().toString();
        String duration = spinnerDuration.getSelectedItem().toString();

        if (name.isEmpty()) {
            showError("Please enter hike name");
            etHikeName.requestFocus();
            return;
        }
        if (location.isEmpty()) {
            showError("Please enter location");
            etLocation.requestFocus();
            return;
        }
        if (date.isEmpty()) {
            showError("Please select date");
            etDate.requestFocus();
            return;
        }
        if (lengthStr.isEmpty()) {
            showError("Please enter length");
            etLength.requestFocus();
            return;
        }
        if (selectedParkingId == -1) {
            showError("Please select parking option");
            rgParking.requestFocus();
            return;
        }

        double length;
        try {
            length = Double.parseDouble(lengthStr);
            if (length <= 0) {
                showError("Length must be a positive number");
                etLength.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            showError("Please enter a valid number for length");
            etLength.requestFocus();
            return;
        }

        RadioButton selectedParking = findViewById(selectedParkingId);
        String parking = selectedParking.getText().toString();

        int groupSize = 0;
        if (!groupSizeStr.isEmpty()) {
            try {
                groupSize = Integer.parseInt(groupSizeStr);
                if (groupSize <= 0) {
                    showError("Group size must be greater than 0");
                    etGroupSize.requestFocus();
                    return;
                }
            } catch (NumberFormatException e) {
                showError("Please enter a valid number for group size");
                etGroupSize.requestFocus();
                return;
            }
        }

        showConfirmationDialog(name, location, date, length, difficulty, duration, parking, description, groupSize);
    }

    private void showConfirmationDialog(String name, String location, String date, double length,
                                        String difficulty, String duration, String parking,
                                        String description, int groupSize) {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_confirm_hike, null);

        TextView tvName = dialogView.findViewById(R.id.tvConfirmName);
        TextView tvLocation = dialogView.findViewById(R.id.tvConfirmLocation);
        TextView tvDate = dialogView.findViewById(R.id.tvConfirmDate);
        TextView tvLength = dialogView.findViewById(R.id.tvConfirmLength);
        TextView tvDifficulty = dialogView.findViewById(R.id.tvConfirmDifficulty);
        TextView tvDuration = dialogView.findViewById(R.id.tvConfirmDuration);
        TextView tvParking = dialogView.findViewById(R.id.tvConfirmParking);
        TextView tvDescription = dialogView.findViewById(R.id.tvConfirmDescription);
        TextView tvGroupSize = dialogView.findViewById(R.id.tvConfirmGroupSize);

        tvName.setText(name);
        tvLocation.setText(location);
        tvDate.setText(date);
        tvLength.setText(getString(R.string.length_km, String.valueOf(length)));
        tvDifficulty.setText(difficulty);
        tvDuration.setText(duration);
        tvParking.setText(parking);
        tvDescription.setText(description.isEmpty() ? "N/A" : description);
        tvGroupSize.setText(groupSize > 0 ? String.valueOf(groupSize) : "N/A");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        Button btnEdit = dialogView.findViewById(R.id.btnEdit);
        Button btnConfirm = dialogView.findViewById(R.id.btnConfirm);

        btnEdit.setOnClickListener(v -> dialog.dismiss());
        btnConfirm.setOnClickListener(v -> {
            saveHikeToDatabase(name, location, date, length, difficulty, duration, parking, description, groupSize);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void saveHikeToDatabase(String name, String location, String date,
                                    double length, String difficulty, String duration,
                                    String parking, String description, int groupSize) {
        try {
            Hike hike = new Hike(name, location, date, parking, length, difficulty, description, duration, groupSize);
            long id = dbHelper.addHike(hike);

            if (id > 0) {
                Toast.makeText(this, "Hike saved successfully!", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            } else {
                showError("Failed to save hike. Please try again.");
            }
        } catch (Exception e) {
            showError("Error: " + e.getMessage());
            Log.e("AddHike", "Save error", e);
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}
