package com.example.tmp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.tmp.databinding.FragmentProfileSetupBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ProfileSetupFragment extends Fragment {
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;
    private FragmentProfileSetupBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileSetupBinding.inflate(inflater, container, false);

        mAuth = FirebaseAuth.getInstance();
        usersRef = FirebaseDatabase.getInstance().getReference("users");

        binding.completeButton.setOnClickListener(v -> saveUserProfile());

        return binding.getRoot();
    }

    private void saveUserProfile() {
        String nickname = binding.nicknameInput.getText().toString().trim();
        String department = binding.departmentInput.getText().toString().trim();
        String grade = binding.gradeInput.getText().toString().trim();

        if (validateInput(nickname, department, grade)) {
            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null) {
                UserData userData = new UserData(user);
                userData.setDisplayName(nickname);
                userData.setDepartment(department);
                userData.setGrade(grade);

                usersRef.child(user.getUid()).setValue(userData)
                        .addOnSuccessListener(aVoid -> {
                            ((MainActivity)requireActivity()).onLoginSuccess();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(requireContext(), "프로필 저장 실패", Toast.LENGTH_SHORT).show()
                        );
            }
        }
    }

    private boolean validateInput(String nickname, String department, String grade) {
        if (nickname.isEmpty()) {
            binding.nicknameInput.setError("닉네임을 입력하세요");
            return false;
        }
        if (department.isEmpty()) {
            binding.departmentInput.setError("학과를 입력하세요");
            return false;
        }
        if (grade.isEmpty()) {
            binding.gradeInput.setError("학년을 입력하세요");
            return false;
        }
        return true;
    }
}