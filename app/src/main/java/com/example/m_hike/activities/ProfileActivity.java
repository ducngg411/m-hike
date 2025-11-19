package com.example.m_hike.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.m_hike.R;
import com.example.m_hike.adapters.ProfilePagerAdapter;
import com.example.m_hike.fragments.StatisticsFragment;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvUserName;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private ProfilePagerAdapter pagerAdapter;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Setup toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("Profile");
            }
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("MHikePrefs", Context.MODE_PRIVATE);

        // Initialize views
        initializeViews();

        // Load user name
        loadUserName();

        // Setup ViewPager2 and TabLayout
        setupViewPager();
    }

    private void initializeViews() {
        tvUserName = findViewById(R.id.tvUserName);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
    }

    private void loadUserName() {
        String userName = sharedPreferences.getString("user_name", "Hiker");
        tvUserName.setText(userName);
    }

    private void setupViewPager() {
        // Create adapter
        pagerAdapter = new ProfilePagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        // Connect TabLayout with ViewPager2
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("Statistics");
                            tab.setIcon(R.drawable.ic_statistics);
                            break;
                        case 1:
                            tab.setText("Settings");
                            tab.setIcon(R.drawable.ic_settings);
                            break;
                    }
                }
        ).attach();
    }

    // Called from SettingsFragment to update user name
    public void updateUserName(String name) {
        tvUserName.setText(name);
    }

    // Called from SettingsFragment after clearing data
    public void refreshStatistics() {
        // Get current fragment
        if (pagerAdapter != null) {
            // Switch to statistics tab to show empty state
            viewPager.setCurrentItem(0, true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload user name in case it was changed
        loadUserName();
    }
}