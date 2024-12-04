package com.example.ssussemble;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.ssussemble.databinding.ActivityDarkModeSettingBinding;

public class DarkModeSetting extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private ActivityDarkModeSettingBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityDarkModeSettingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sharedPreferences = getSharedPreferences("app_settings", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();


        int themeMode = sharedPreferences.getInt("themeMode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);


        if (themeMode == AppCompatDelegate.MODE_NIGHT_NO) {
            binding.radioOFF.setChecked(true);
        } else if (themeMode == AppCompatDelegate.MODE_NIGHT_YES) {
            binding.radioON.setChecked(true);
        } else {
            binding.radioAuto.setChecked(true);
        }


        binding.radioGroup.setOnCheckedChangeListener((radioGroup, i) -> {
            int selectedMode;

            if (i == R.id.radioON) {
                selectedMode = AppCompatDelegate.MODE_NIGHT_YES; 
            } else if (i == R.id.radioOFF) {
                selectedMode = AppCompatDelegate.MODE_NIGHT_NO;
            } else {
                selectedMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
            }


            if (selectedMode != themeMode) {
                editor.putInt("themeMode", selectedMode).apply();
                AppCompatDelegate.setDefaultNightMode(selectedMode);
            }
        });
    }
}