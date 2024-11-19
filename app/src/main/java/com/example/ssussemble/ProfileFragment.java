package com.example.ssussemble;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.*;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;

// 임시로 로그아웃 버튼 추가해놓음
public class ProfileFragment extends Fragment {
    private FirebaseAuth mAuth;
    private Button logoutButton; // 추가

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();

        logoutButton = new Button(requireContext());
        logoutButton.setText("로그아웃");
        logoutButton.setOnClickListener(v -> performLogout());

        LinearLayout layout = view.findViewById(R.id.profileLayout);
        layout.addView(logoutButton);

        return view;
    }

    private void performLogout() {
        mAuth.signOut();

        SharedPreferences mPref = requireActivity().getSharedPreferences("LoginData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mPref.edit();
        editor.remove("LoginId");
        editor.remove("LoginPassword");
        editor.apply();

        MainActivity.Login_id = null;
        MainActivity.Login_password = null;

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).onLogout();
        }
    }
}