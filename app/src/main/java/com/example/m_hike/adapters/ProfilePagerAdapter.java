package com.example.m_hike.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.m_hike.fragments.SettingsFragment;
import com.example.m_hike.fragments.StatisticsFragment;

public class ProfilePagerAdapter extends FragmentStateAdapter {
    public ProfilePagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new StatisticsFragment();
            case 1:
                return new SettingsFragment();
            default:
                return new StatisticsFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
