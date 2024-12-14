package com.example.ssussemble;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class RequestsPagerAdapter extends FragmentStateAdapter {
    public RequestsPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new ReceivedRequestsFragment();
            case 1:
                return new SentRequestsFragment();
            default:
                return new ReceivedRequestsFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}