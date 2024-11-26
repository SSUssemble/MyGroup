package com.example.ssussemble;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ssussemble.databinding.ActivityDarkModeSettingBinding;

public class DarkModeSetting extends AppCompatActivity {
    private SharedPreferences sharedPreferences;
    private Boolean isDarkmode;

    private ActivityDarkModeSettingBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityDarkModeSettingBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        sharedPreferences = getSharedPreferences("app_settings", MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();

        int themeMode = sharedPreferences.getInt("themeMode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        if(themeMode == AppCompatDelegate.MODE_NIGHT_NO){
            binding.radioOFF.setChecked(true);
        } else if(themeMode ==AppCompatDelegate.MODE_NIGHT_YES){
            binding.radioON.setChecked(true);
        } else{
            binding.radioAuto.setChecked(true);
        }

        AppCompatDelegate.setDefaultNightMode(themeMode);

        binding.radioGroup.setOnCheckedChangeListener(((radioGroup, i) -> {
            int selectMode;
            if(i == R.id.radioON){
                selectMode = AppCompatDelegate.MODE_NIGHT_NO;
            } else if (i == R.id.radioOFF) {
                selectMode = AppCompatDelegate.MODE_NIGHT_YES;
            } else {
                selectMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
            }

            editor.putInt("themeMode", selectMode)
                    .apply();
            AppCompatDelegate.setDefaultNightMode(selectMode);
        }));
    }
}