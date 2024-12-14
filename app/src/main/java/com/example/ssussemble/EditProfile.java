package com.example.ssussemble;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditProfile extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    private EditText nicknameInput, departmentInput, gradeInput;
    private Button saveButton;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        initializeViews();
        setupListeners();
        loadUserProfile();
    }

    private void initializeViews() {
        nicknameInput = findViewById(R.id.nicknameInput);
        departmentInput = findViewById(R.id.departmentInput);
        gradeInput = findViewById(R.id.gradeInput);
        saveButton = findViewById(R.id.completeButton);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        saveButton.setOnClickListener(v -> saveUserProfile());
    }

    private void loadUserProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            usersRef.child(userId).get().addOnSuccessListener(snapshot -> {
                if (snapshot.exists()) {
                    String nickname = snapshot.child("displayName").getValue(String.class);
                    String department = snapshot.child("department").getValue(String.class);
                    String grade = snapshot.child("grade").getValue(String.class);

                    nicknameInput.setText(nickname);
                    departmentInput.setText(department);
                    gradeInput.setText(grade);
                } else {
                    showError("프로필 데이터를 찾을 수 없습니다.");
                }
            }).addOnFailureListener(e -> showError("프로필 로드 실패: " + e.getMessage()));
        }
    }

    private void saveUserProfile() {
        String nickname = nicknameInput.getText().toString().trim();
        String department = departmentInput.getText().toString().trim();
        String grade = gradeInput.getText().toString().trim();

        if (validateInput(nickname, department, grade)) {
            showLoading(true);

            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null) {
                String userId = user.getUid();
                usersRef.child(userId).child("displayName").setValue(nickname);
                usersRef.child(userId).child("department").setValue(department);
                usersRef.child(userId).child("grade").setValue(grade)
                        .addOnSuccessListener(aVoid -> {
                            showLoading(false);
                            Toast.makeText(this, "프로필이 업데이트되었습니다.", Toast.LENGTH_SHORT).show();
                            finish(); // Close the activity after saving
                        })
                        .addOnFailureListener(e -> {
                            showLoading(false);
                            showError("프로필 업데이트 실패: " + e.getMessage());
                        });
            }
        }
    }

    private boolean validateInput(String nickname, String department, String grade) {
        if (nickname.isEmpty()) {
            nicknameInput.setError("닉네임을 입력하세요");
            return false;
        }
        if (department.isEmpty()) {
            departmentInput.setError("학과를 입력하세요");
            return false;
        }
        if (grade.isEmpty()) {
            gradeInput.setError("학년을 입력하세요");
            return false;
        }
        return true;
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? ProgressBar.VISIBLE : ProgressBar.GONE);
        saveButton.setEnabled(!show);
        nicknameInput.setEnabled(!show);
        departmentInput.setEnabled(!show);
        gradeInput.setEnabled(!show);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}