package com.example.m_hike.activities;

import java.util.Locale;
import android.util.Log;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.m_hike.R;
import com.example.m_hike.database.DatabaseHelper;
import com.example.m_hike.models.Hike;
import com.example.m_hike.models.Observation;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class AddObservationActivity extends AppCompatActivity {
    private TextView tvObsHikeName;
    private TextInputEditText etObservation, etObsTime, etObsComment;
    private Button btnSaveObs, btnCancelObs;

    // Database
    private DatabaseHelper dbHelper;

    // Data
    private int hikeId;
    private Hike currentHike;
    private Calendar selectedDateTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_observation);

        // Setup toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("Add Observation");
            }
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        // Get hike id from intent
        hikeId = getIntent().getIntExtra("HIKE_ID", -1);
        if (hikeId == -1) {
            Toast.makeText(this, "Error: Invalid hike ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        dbHelper = new DatabaseHelper(this);

        // Initialize views
        initializeViews();

        // Load hike info
        loadHikeInfo();

        // Setup date and time picker
        setupDateTimePicker();

        // Set default time to current time
        setDefaultDateTime();

        // Setup button listeners
        setupButtonListeners();
    }

    // Initialize all UI components
    private void initializeViews() {
        tvObsHikeName = findViewById(R.id.tvObsHikeName);
        etObservation = findViewById(R.id.etObservation);
        etObsTime = findViewById(R.id.etObsTime);
        etObsComment = findViewById(R.id.etObsComment);
        btnSaveObs = findViewById(R.id.btnSaveObs);
        btnCancelObs = findViewById(R.id.btnCancelObs);

        selectedDateTime = Calendar.getInstance();
    }

    // Load hike information from database
    private void loadHikeInfo() {
        currentHike = dbHelper.getHikeById(hikeId);
        if (currentHike != null) {
            tvObsHikeName.setText(currentHike.getName());
        } else {
            Toast.makeText(this, "Error: Hike not found", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    // Setup date time picker
    private void setupDateTimePicker() {
        etObsTime.setOnClickListener(v -> showDateTimePicker());
    }

    // Show date and time picker dialog
    private void showDateTimePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
            (view, year, month, dayOfMonth) -> {
                selectedDateTime.set(Calendar.YEAR, year);
                selectedDateTime.set(Calendar.MONTH, month);
                selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                showTimePicker();
            },
            selectedDateTime.get(Calendar.YEAR),
            selectedDateTime.get(Calendar.MONTH),
            selectedDateTime.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    // Show time picker dialog
    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
            (view, hourOfDay, minute) -> {
                selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                selectedDateTime.set(Calendar.MINUTE, minute);
                updateDateTimeDisplay();
            },
            selectedDateTime.get(Calendar.HOUR_OF_DAY),
            selectedDateTime.get(Calendar.MINUTE),
            true
        );
        timePickerDialog.show();
    }

    // Set default date time to current
    private void setDefaultDateTime() {
        selectedDateTime = Calendar.getInstance();
        updateDateTimeDisplay();
    }

    // Update date time display in EditText
    private void updateDateTimeDisplay() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        etObsTime.setText(sdf.format(selectedDateTime.getTime()));
    }

    // Setup button listeners
    private void setupButtonListeners() {
        // Save observation button
        btnSaveObs.setOnClickListener(v -> validateAndSave());

        // Cancel button
        btnCancelObs.setOnClickListener(v -> finish());
    }

    // Validate input and save observation
    private void validateAndSave() {
        String observation = safeText(etObservation);
        String time = safeText(etObsTime);
        String comment = safeText(etObsComment);

        // Validation
        if (observation.isEmpty()) {
            showError("Please enter an observation.");
            etObservation.requestFocus();
            return;
        }

        if (time.isEmpty()) {
            showError("Please select a time for the observation.");
            etObsTime.requestFocus();
            return;
        }

        // Save to database
        Observation newObservation = new Observation(hikeId, observation, time, comment);

        try {
            long id = dbHelper.addObservation(newObservation);
            if (id > 0) {
                Toast.makeText(this, "Observation added successfully.", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                showError("Failed to add observation. Please try again.");
            }
        } catch (Exception e) {
            showError("Error: " + e.getMessage());
            Log.e("AddObservation", "Add failed", e);
        }
    }

    // Safe text extraction
    private String safeText(TextInputEditText edit){
        CharSequence cs = edit.getText();
        return cs != null ? cs.toString().trim() : "";
    }

    // Show error message
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
