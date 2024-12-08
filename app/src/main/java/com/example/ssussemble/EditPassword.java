package com.example.ssussemble;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class EditPassword extends AppCompatActivity {
    private EditText currentPasswordInput, newPasswordInput, confirmPasswordInput;
    private Button changePasswordButton;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_password);

        mAuth = FirebaseAuth.getInstance();

        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        currentPasswordInput = findViewById(R.id.currentPasswordInput);
        newPasswordInput = findViewById(R.id.newPasswordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        changePasswordButton = findViewById(R.id.changePasswordButton);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        changePasswordButton.setOnClickListener(v -> changePassword());
    }

    private void changePassword() {
        String currentPassword = currentPasswordInput.getText().toString().trim();
        String newPassword = newPasswordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        if (!validateInput(currentPassword, newPassword, confirmPassword)) {
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null && user.getEmail() != null) {
            // Re-authenticate the user
            mAuth.signInWithEmailAndPassword(user.getEmail(), currentPassword)
                    .addOnSuccessListener(authResult -> {
                        // Update the password
                        user.updatePassword(newPassword)
                                .addOnSuccessListener(aVoid -> {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(EditPassword.this, "비밀번호가 변경되었습니다.", Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    progressBar.setVisibility(View.GONE);
                                    showError("비밀번호 변경 실패: " + e.getMessage());
                                });
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        showError("현재 비밀번호가 일치하지 않습니다.");
                    });
        } else {
            progressBar.setVisibility(View.GONE);
            showError("사용자 인증 실패. 다시 로그인하세요.");
        }
    }

    private boolean validateInput(String currentPassword, String newPassword, String confirmPassword) {
        if (TextUtils.isEmpty(currentPassword)) {
            currentPasswordInput.setError("현재 비밀번호를 입력하세요.");
            return false;
        }
        if (TextUtils.isEmpty(newPassword)) {
            newPasswordInput.setError("새 비밀번호를 입력하세요.");
            return false;
        }
        if (newPassword.length() < 6) {
            newPasswordInput.setError("비밀번호는 6자 이상이어야 합니다.");
            return false;
        }
        if (!newPassword.equals(confirmPassword)) {
            confirmPasswordInput.setError("새 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
            return false;
        }
        return true;
    }

    private void showError(String message) {
        Toast.makeText(EditPassword.this, message, Toast.LENGTH_SHORT).show();
    }
}