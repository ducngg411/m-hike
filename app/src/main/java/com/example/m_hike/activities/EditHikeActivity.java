package com.example.m_hike.activities;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.m_hike.R;
import com.example.m_hike.database.DatabaseHelper;
import com.example.m_hike.models.Hike;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;

public class EditHikeActivity extends AppCompatActivity {

    // UI Components
    private ImageButton btnBackEdit;
    private TextInputEditText etEditHikeName, etEditLocation, etEditDate, etEditLength,
            etEditDescription, etEditGroupSize;
    private RadioGroup rgEditParking;
    private Spinner spinnerEditDifficulty, spinnerEditDuration;
    private Button btnUpdateHike, btnCancelEdit;

    // Database
    private DatabaseHelper dbHelper;

    // Current hike
    private Hike currentHike;
    private int hikeId;

    // Data arrays
    private String[] difficultyLevels = {"Easy", "Moderate", "Hard", "Very Hard"};
    private String[] durationOptions = {"Less than 1 hour", "1-2 hours", "2-4 hours",
            "4-6 hours", "6-8 hours", "More than 8 hours"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("EditHikeActivity", "==========================================");
        Log.d("EditHikeActivity", "EditHikeActivity started!");
        setContentView(R.layout.activity_edit_hike);

        // Get hike ID from intent
        hikeId = getIntent().getIntExtra("HIKE_ID", -1);
        Log.d("EditHikeActivity", "Received Hike ID: " + hikeId);

        if (hikeId == -1) {
            Log.e("EditHikeActivity", "ERROR: Invalid hike ID (-1)");
            Toast.makeText(this, "Error: Invalid hike ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.d("EditHikeActivity", "Valid hike ID received, continuing...");

        // Initialize database
        dbHelper = new DatabaseHelper(this);

        // Initialize views
        initializeViews();

        // Setup spinners
        setupSpinners();

        // Setup date picker
        setupDatePicker();

        // Load hike data
        loadHikeData();

        // Setup button listeners
        setupButtonListeners();
    }

    /**
     * Initialize all UI components
     */
    private void initializeViews() {
        btnBackEdit = findViewById(R.id.btnBackEdit);

        etEditHikeName = findViewById(R.id.etEditHikeName);
        etEditLocation = findViewById(R.id.etEditLocation);
        etEditDate = findViewById(R.id.etEditDate);
        etEditLength = findViewById(R.id.etEditLength);
        etEditDescription = findViewById(R.id.etEditDescription);
        etEditGroupSize = findViewById(R.id.etEditGroupSize);

        rgEditParking = findViewById(R.id.rgEditParking);
        spinnerEditDifficulty = findViewById(R.id.spinnerEditDifficulty);
        spinnerEditDuration = findViewById(R.id.spinnerEditDuration);

        btnUpdateHike = findViewById(R.id.btnUpdateHike);
        btnCancelEdit = findViewById(R.id.btnCancelEdit);
    }

    /**
     * Setup spinner adapters with data
     */
    private void setupSpinners() {
        // Difficulty spinner
        ArrayAdapter<String> difficultyAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, difficultyLevels);
        difficultyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEditDifficulty.setAdapter(difficultyAdapter);

        // Duration spinner
        ArrayAdapter<String> durationAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, durationOptions);
        durationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEditDuration.setAdapter(durationAdapter);
    }

    /**
     * Setup date picker dialog for date field
     */
    private void setupDatePicker() {
        etEditDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(
                    EditHikeActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        // Format: DD/MM/YYYY
                        String date = String.format("%02d/%02d/%04d",
                                selectedDay, selectedMonth + 1, selectedYear);
                        etEditDate.setText(date);
                    },
                    year, month, day
            );
            datePickerDialog.show();
        });
    }

    /**
     * Load existing hike data into form
     */
    private void loadHikeData() {
        currentHike = dbHelper.getHikeById(hikeId);

        if (currentHike == null) {
            Toast.makeText(this, "Error: Hike not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Populate fields
        etEditHikeName.setText(currentHike.getName());
        etEditLocation.setText(currentHike.getLocation());
        etEditDate.setText(currentHike.getDate());
        etEditLength.setText(String.valueOf(currentHike.getLength()));
        etEditDescription.setText(currentHike.getDescription());

        if (currentHike.getMaxGroupSize() > 0) {
            etEditGroupSize.setText(String.valueOf(currentHike.getMaxGroupSize()));
        }

        // Set parking radio button
        if (currentHike.getParkingAvailable().equals("Yes")) {
            ((RadioButton) findViewById(R.id.rbEditParkingYes)).setChecked(true);
        } else {
            ((RadioButton) findViewById(R.id.rbEditParkingNo)).setChecked(true);
        }

        // Set difficulty spinner
        for (int i = 0; i < difficultyLevels.length; i++) {
            if (difficultyLevels[i].equals(currentHike.getDifficulty())) {
                spinnerEditDifficulty.setSelection(i);
                break;
            }
        }

        // Set duration spinner
        for (int i = 0; i < durationOptions.length; i++) {
            if (durationOptions[i].equals(currentHike.getEstimatedDuration())) {
                spinnerEditDuration.setSelection(i);
                break;
            }
        }
    }

    /**
     * Setup button click listeners
     */
    private void setupButtonListeners() {
        // Back button
        btnBackEdit.setOnClickListener(v -> finish());

        // Update button
        btnUpdateHike.setOnClickListener(v -> validateAndUpdate());

        // Cancel button
        btnCancelEdit.setOnClickListener(v -> finish());
    }

    /**
     * Validate all required fields and update hike
     */
    private void validateAndUpdate() {
        // Get values from fields
        String name = etEditHikeName.getText().toString().trim();
        String location = etEditLocation.getText().toString().trim();
        String date = etEditDate.getText().toString().trim();
        String lengthStr = etEditLength.getText().toString().trim();
        String description = etEditDescription.getText().toString().trim();
        String groupSizeStr = etEditGroupSize.getText().toString().trim();

        // Get selected parking option
        int selectedParkingId = rgEditParking.getCheckedRadioButtonId();

        // Get spinner values
        String difficulty = spinnerEditDifficulty.getSelectedItem().toString();
        String duration = spinnerEditDuration.getSelectedItem().toString();

        // Validation - Check required fields
        if (name.isEmpty()) {
            showError("Please enter hike name");
            etEditHikeName.requestFocus();
            return;
        }

        if (location.isEmpty()) {
            showError("Please enter location");
            etEditLocation.requestFocus();
            return;
        }

        if (date.isEmpty()) {
            showError("Please select a date");
            etEditDate.requestFocus();
            return;
        }

        if (selectedParkingId == -1) {
            showError("Please select parking availability");
            return;
        }

        if (lengthStr.isEmpty()) {
            showError("Please enter hike length");
            etEditLength.requestFocus();
            return;
        }

        // Validate length is a valid number
        double length;
        try {
            length = Double.parseDouble(lengthStr);
            if (length <= 0) {
                showError("Length must be greater than 0");
                etEditLength.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            showError("Please enter a valid number for length");
            etEditLength.requestFocus();
            return;
        }

        // Get parking value
        RadioButton selectedParking = findViewById(selectedParkingId);
        String parking = selectedParking.getText().toString();

        // Validate and get group size (optional field)
        int groupSize = 0;
        if (!groupSizeStr.isEmpty()) {
            try {
                groupSize = Integer.parseInt(groupSizeStr);
                if (groupSize <= 0) {
                    showError("Group size must be greater than 0");
                    etEditGroupSize.requestFocus();
                    return;
                }
            } catch (NumberFormatException e) {
                showError("Please enter a valid number for group size");
                etEditGroupSize.requestFocus();
                return;
            }
        }

        // Update hike object
        currentHike.setName(name);
        currentHike.setLocation(location);
        currentHike.setDate(date);
        currentHike.setParkingAvailable(parking);
        currentHike.setLength(length);
        currentHike.setDifficulty(difficulty);
        currentHike.setDescription(description);
        currentHike.setEstimatedDuration(duration);
        currentHike.setMaxGroupSize(groupSize);

        // Update in database
        try {
            int rowsAffected = dbHelper.updateHike(currentHike);
            if (rowsAffected > 0) {
                Toast.makeText(this, "Hike updated successfully! ✅", Toast.LENGTH_LONG).show();
                finish(); // Return to previous screen
            } else {
                showError("Failed to update hike. Please try again.");
            }
        } catch (Exception e) {
            showError("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Show error message as Toast
     */
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