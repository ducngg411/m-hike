package com.example.m_hike.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.m_hike.R;
import com.example.m_hike.database.DatabaseHelper;
import com.example.m_hike.models.Hike;
import com.example.m_hike.models.Observation;
import com.google.android.material.textfield.TextInputEditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class EditObservationActivity extends AppCompatActivity {

    // UI Components
    private ImageButton btnBackEditObs;
    private TextView tvEditObsHikeName;
    private TextInputEditText etEditObservation, etEditObsTime, etEditObsComment;
    private Button btnUpdateObs, btnCancelEditObs;

    // Database
    private DatabaseHelper dbHelper;

    // Data
    private int observationId;
    private int hikeId;
    private Observation currentObservation;
    private Hike currentHike;
    private Calendar selectedDateTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_observation);

        // Get IDs from intent
        observationId = getIntent().getIntExtra("OBSERVATION_ID", -1);
        hikeId = getIntent().getIntExtra("HIKE_ID", -1);

        if (observationId == -1 || hikeId == -1) {
            Toast.makeText(this, "Error: Invalid observation or hike ID",
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize database
        dbHelper = new DatabaseHelper(this);

        // Initialize views
        initializeViews();

        // Load data
        loadHikeInfo();
        loadObservationData();

        // Setup date time picker
        setupDateTimePicker();

        // Setup button listeners
        setupButtonListeners();
    }


    // Initialize all UI components
    private void initializeViews() {
        btnBackEditObs = findViewById(R.id.btnBackEditObs);
        tvEditObsHikeName = findViewById(R.id.tvEditObsHikeName);
        etEditObservation = findViewById(R.id.etEditObservation);
        etEditObsTime = findViewById(R.id.etEditObsTime);
        etEditObsComment = findViewById(R.id.etEditObsComment);
        btnUpdateObs = findViewById(R.id.btnUpdateObs);
        btnCancelEditObs = findViewById(R.id.btnCancelEditObs);

        selectedDateTime = Calendar.getInstance();
    }


    // Load hike information
    private void loadHikeInfo() {
        currentHike = dbHelper.getHikeById(hikeId);
        if (currentHike != null) {
            tvEditObsHikeName.setText(currentHike.getName());
        }
    }


    // Load observation data
    private void loadObservationData() {
        // Get observation from database
        currentObservation = getObservationById(observationId);

        if (currentObservation == null) {
            Toast.makeText(this, "Error: Observation not found",
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Populate fields
        etEditObservation.setText(currentObservation.getObservation());
        etEditObsTime.setText(currentObservation.getTime());
        etEditObsComment.setText(currentObservation.getComment());

        // Parse time string to Calendar
        parseTimeString(currentObservation.getTime());
    }


    // Get observation by ID
    private Observation getObservationById(int id) {
        // Get all observations for this hike and find the one with matching ID
        for (Observation obs : dbHelper.getObservationsForHike(hikeId)) {
            if (obs.getId() == id) {
                return obs;
            }
        }
        return null;
    }


    // Parse time string to Calendar object
    private void parseTimeString(String timeString) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            selectedDateTime.setTime(sdf.parse(timeString));
        } catch (ParseException e) {
            e.printStackTrace();
            selectedDateTime = Calendar.getInstance();
        }
    }


    // Setup date and time picker
    private void setupDateTimePicker() {
        etEditObsTime.setOnClickListener(v -> showDateTimePicker());
    }


    // Show date and time picker dialogs
    private void showDateTimePicker() {
        // First show date picker
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDateTime.set(Calendar.YEAR, year);
                    selectedDateTime.set(Calendar.MONTH, month);
                    selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    // Then show time picker
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
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedDateTime.set(Calendar.MINUTE, minute);

                    // Update the text field
                    updateDateTimeDisplay();
                },
                selectedDateTime.get(Calendar.HOUR_OF_DAY),
                selectedDateTime.get(Calendar.MINUTE),
                true // 24-hour format
        );
        timePickerDialog.show();
    }


    // Update date time display in text field

    private void updateDateTimeDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        etEditObsTime.setText(sdf.format(selectedDateTime.getTime()));
    }


    // Setup button click listeners

    private void setupButtonListeners() {
        // Back button
        btnBackEditObs.setOnClickListener(v -> finish());

        // Update button
        btnUpdateObs.setOnClickListener(v -> validateAndUpdate());

        // Cancel button
        btnCancelEditObs.setOnClickListener(v -> finish());
    }


    // Validate and update observation

    private void validateAndUpdate() {
        String observation = etEditObservation.getText().toString().trim();
        String time = etEditObsTime.getText().toString().trim();
        String comment = etEditObsComment.getText().toString().trim();

        // Validation
        if (observation.isEmpty()) {
            showError("Please enter an observation");
            etEditObservation.requestFocus();
            return;
        }

        if (time.isEmpty()) {
            showError("Please select time of observation");
            etEditObsTime.requestFocus();
            return;
        }

        // Update observation object
        currentObservation.setObservation(observation);
        currentObservation.setTime(time);
        currentObservation.setComment(comment);

        // Update in database
        try {
            int rowsAffected = dbHelper.updateObservation(currentObservation);
            if (rowsAffected > 0) {
                Toast.makeText(this, "Observation updated successfully! ✅",
                        Toast.LENGTH_LONG).show();
                finish();
            } else {
                showError("Failed to update observation. Please try again.");
            }
        } catch (Exception e) {
            showError("Error: " + e.getMessage());
            e.printStackTrace();
        }
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