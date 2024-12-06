package com.example.ssussemble;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ssussemble.databinding.FragmentProfileSetupBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileSetupFragment extends Fragment {
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private FragmentProfileSetupBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileSetupBinding.inflate(inflater, container, false);

        // Firebase 초기화
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // 완료 버튼 클릭 리스너
        binding.completeButton.setOnClickListener(v -> saveUserProfile());

        return binding.getRoot();
    }

    private void saveUserProfile() {
        String nickname = binding.nicknameInput.getText().toString().trim();
        String department = binding.departmentInput.getText().toString().trim();
        String grade = binding.gradeInput.getText().toString().trim();

        // 입력값 유효성 검사
        if (!validateInput(nickname, department, grade)) return;

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String uid = user.getUid();

            // Firestore에 저장할 데이터 생성
            UserData userData = new UserData();
            userData.setDisplayName(nickname);
            userData.setDepartment(department);
            userData.setGrade(grade);

            // Firestore users 컬렉션에 데이터 추가 (기존 데이터에 덮어쓰지 않고 업데이트)
            firestore.collection("users")
                    .document(uid)
                    .update(   // update() 사용하여 기존 데이터에 추가
                            "displayName", nickname,
                            "department", department,
                            "grade", grade
                    )
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(requireContext(), "프로필 저장 완료", Toast.LENGTH_SHORT).show();
                        // 메인 화면으로 이동
                        ((MainActivity) requireActivity()).onLoginSuccess();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(), "프로필 저장 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(requireContext(), "로그인된 사용자 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
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
