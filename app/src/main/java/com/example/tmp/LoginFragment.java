package com.example.tmp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginFragment extends Fragment {
    private FirebaseAuth mAuth;
    private EditText emailInput, passwordInput;
    private Button loginButton, signupButton;
    private ProgressBar progressBar;
    private SharedPreferences mPref;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        mAuth = FirebaseAuth.getInstance();
        mPref = requireActivity().getSharedPreferences("LoginData", Context.MODE_PRIVATE);
        initializeViews(view);
        setupListeners();

        return view;
    }

    private void initializeViews(View view) {
        emailInput = view.findViewById(R.id.emailInput);
        passwordInput = view.findViewById(R.id.passwordInput);
        loginButton = view.findViewById(R.id.loginButton);
        signupButton = view.findViewById(R.id.signupButton);
        progressBar = view.findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        loginButton.setOnClickListener(v -> loginUser());
        signupButton.setOnClickListener(v -> {
            Fragment signupFragment = new SignupFragment();
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, signupFragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

    private void loginUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (validateInput(email, password)) {
            showLoading(true);
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        saveLoginData(email, password);
                        showLoading(false);
                        ((MainActivity)requireActivity()).onLoginSuccess();
                    })
                    .addOnFailureListener(e -> {
                        showLoading(false);
                        showError("로그인 실패: " + e.getMessage());
                    });
        }
    }

    private void registerUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (validateInput(email, password)) {
            showLoading(true);
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        FirebaseUser user = authResult.getUser();
                        if (user != null) {
                            saveUserToDatabase(user);
                            saveLoginData(email, password);
                        }
                        showLoading(false);
                        ((MainActivity)requireActivity()).onLoginSuccess();
                    })
                    .addOnFailureListener(e -> {
                        showLoading(false);
                        showError("회원가입 실패: " + e.getMessage());
                    });
        }
    }

    private boolean validateInput(String email, String password) {
        if (email.isEmpty()) {
            emailInput.setError("이메일을 입력하세요");
            return false;
        }
        if (password.isEmpty()) {
            passwordInput.setError("비밀번호를 입력하세요");
            return false;
        }
        if (password.length() < 6) {
            passwordInput.setError("비밀번호는 6자 이상이어야 합니다");
            return false;
        }
        return true;
    }

    private void saveUserToDatabase(FirebaseUser user) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance()
                .getReference("users");
        UserData userData = new UserData(user);
        usersRef.child(user.getUid()).setValue(userData);
    }

    private void saveLoginData(String email, String password) {
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString("LoginId", email);
        editor.putString("LoginPassword", password);
        editor.apply();

        MainActivity.Login_id = email;
        MainActivity.Login_password = password;
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        loginButton.setEnabled(!show);
        signupButton.setEnabled(!show);
    }

    private void showError(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
}