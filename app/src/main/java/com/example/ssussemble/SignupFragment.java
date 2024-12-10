package com.example.ssussemble;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignupFragment extends Fragment {

    private TextView idInput, pwdInput, pwdInput2, emailInput, authNumInput;
    private Button checkDupBtn, sendAuthBtn, registerBtn;
    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;
    private String generatedAuthCode;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup, container, false);

        // Firebase 초기화
        FirebaseApp.initializeApp(requireContext());
        firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // View 초기화
        idInput = view.findViewById(R.id.register_id);
        pwdInput = view.findViewById(R.id.register_pwd);
        pwdInput2 = view.findViewById(R.id.register_pwd2);
        emailInput = view.findViewById(R.id.register_email);
        authNumInput = view.findViewById(R.id.register_authNum);
        checkDupBtn = view.findViewById(R.id.checkDup);
        sendAuthBtn = view.findViewById(R.id.checkDup2);
        registerBtn = view.findViewById(R.id.btn_register2);

        // ID 중복 확인 버튼
        checkDupBtn.setOnClickListener(view1 -> checkDuplicateId());

        // 인증 번호 전송 버튼
        sendAuthBtn.setOnClickListener(view1 -> sendAuthCode());

        // 회원가입 버튼
        registerBtn.setOnClickListener(view1 -> registerUser());

        return view;
    }

    private void checkDuplicateId() {
        String userId = idInput.getText().toString().trim();

        if (TextUtils.isEmpty(userId)) {
            Toast.makeText(requireContext(), "아이디를 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        firestore.collection("users")
                .whereEqualTo("idToken", userId) // 아이디 중복 체크
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        Toast.makeText(requireContext(), "이미 사용 중인 아이디입니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "사용 가능한 아이디입니다.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendAuthCode() {
        String email = emailInput.getText().toString().trim();

        if (!email.endsWith("@soongsil.ac.kr")) {
            Toast.makeText(requireContext(), "숭실대학교 이메일(@soongsil.ac.kr)만 사용 가능합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(requireContext(), "이메일을 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            SendMail mail = new SendMail();
            mail.sendSecurityCode(requireContext(), email);
            generatedAuthCode = mail.gMailSender.getEmailCode(); // 인증번호 가져오기
            Log.d("SendAuthCode", "Email sent successfully: " + generatedAuthCode);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "인증번호 전송 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private void registerUser() {
        String userId = idInput.getText().toString().trim();
        String password = pwdInput.getText().toString();
        String confirmPassword = pwdInput2.getText().toString();
        String email = emailInput.getText().toString().trim();
        String authCode = authNumInput.getText().toString();

        if (TextUtils.isEmpty(userId) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)
                || TextUtils.isEmpty(email) || TextUtils.isEmpty(authCode)) {
            Toast.makeText(requireContext(), "모든 필드를 채워주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(requireContext(), "비밀번호가 다릅니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!authCode.equals(generatedAuthCode)) {
            Toast.makeText(requireContext(), "인증번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Firebase Authentication에 사용자 추가
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String uid = firebaseUser.getUid();

                            // Firestore에 유저 정보 저장
                            UserData userAccount = new UserData();
                            userAccount.setIdToken(userId);// 입력한 사용자 ID를 저장
                            Toast.makeText(this.getContext(), userId, Toast.LENGTH_SHORT).show();
                            Toast.makeText(this.getContext(), userAccount.getIdToken(), Toast.LENGTH_SHORT).show();
                            userAccount.setEmail(email);
                            userAccount.setUid(uid); // UID는 별도로 저장

                            firestore.collection("users")
                                    .document(uid) // UID를 문서 ID로 사용
                                    .set(userAccount)
                                    .addOnCompleteListener(firestoreTask -> {
                                        if (firestoreTask.isSuccessful()) {
                                            Toast.makeText(requireContext(), "회원가입 완료", Toast.LENGTH_SHORT).show();
                                            Fragment profileSetupFragment = new ProfileSetupFragment();
                                            getParentFragmentManager().beginTransaction()
                                                    .replace(R.id.fragment_container, profileSetupFragment)
                                                    .commit();
                                        } else {
                                            Toast.makeText(requireContext(), "회원가입 실패: Firestore 저장 오류", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(requireContext(), "회원가입 실패: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
