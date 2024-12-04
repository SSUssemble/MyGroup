package com.example.ssussemble;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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
import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;
import java.util.Random;

public class SignupFragment extends Fragment {
    private static final String SENDER_EMAIL = "cnysjlee2@gmail.com";
    private static final String SENDER_PASSWORD = "itae mrnn icji icjg";
    private boolean isVerified = false;

    private FirebaseAuth mAuth;
    private EditText emailInput, passwordInput, verificationCodeInput;
    private Button sendVerificationButton, verifyCodeButton, signupButton;
    private ProgressBar progressBar;
    private String verificationCode;
    private SharedPreferences mPref;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signup, container, false);

        mAuth = FirebaseAuth.getInstance();
        mPref = requireActivity().getSharedPreferences("LoginData", Context.MODE_PRIVATE);
        initializeViews(view);
        setupListeners();

        signupButton.setEnabled(false);
        verificationCodeInput.setEnabled(false);
        verifyCodeButton.setEnabled(false);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        verificationCodeInput.setEnabled(false);
        verifyCodeButton.setEnabled(false);
        signupButton.setEnabled(false);
    }

    private void initializeViews(View view) {
        emailInput = view.findViewById(R.id.emailInput);
        passwordInput = view.findViewById(R.id.passwordInput);
        verificationCodeInput = view.findViewById(R.id.verificationCodeInput);
        sendVerificationButton = view.findViewById(R.id.sendVerificationButton);
        verifyCodeButton = view.findViewById(R.id.verifyCodeButton);
        signupButton = view.findViewById(R.id.signupButton);
        progressBar = view.findViewById(R.id.progressBar);

        signupButton.setEnabled(false);
    }

    private void setupListeners() {
        sendVerificationButton.setOnClickListener(v -> sendVerificationEmail());
        verifyCodeButton.setOnClickListener(v -> verifyCode());
        signupButton.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        if (!isVerified) {
            showError("이메일 인증이 필요합니다");
            return;
        }

        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (validateInput(email, password)) {
            showLoading(true);
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {
                        FirebaseUser user = authResult.getUser();
                        if (user != null) {
                            Fragment profileSetupFragment = new ProfileSetupFragment();
                            getParentFragmentManager().beginTransaction()
                                    .replace(R.id.fragment_container, profileSetupFragment)
                                    .commit();
                        }
                    })
                    .addOnFailureListener(e -> {
                        showLoading(false);
                        showError("회원가입 실패: " + e.getMessage());
                    });
        }

    }

    private void sendVerificationEmail() {
        String email = emailInput.getText().toString().trim();
        if (!email.endsWith("@soongsil.ac.kr")) {
            showError("숭실대학교 이메일(@soongsil.ac.kr)만 사용 가능합니다.");
            showLoading(false);
            return;
        }

        showLoading(true);
        verificationCode = generateVerificationCode();

        verificationCodeInput.setEnabled(false);
        verifyCodeButton.setEnabled(false);
        signupButton.setEnabled(false);

        new Thread(() -> {
            try {
                Properties props = new Properties();
                props.put("mail.smtp.auth", "true");
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.host", "smtp.gmail.com");
                props.put("mail.smtp.port", "587");

                Session session = Session.getInstance(props, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
                    }
                });

                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(SENDER_EMAIL));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));
                message.setSubject("[SSUsemble] 회원가입 인증번호");
                message.setText("SSUsemble 회원가입 인증번호는 " + verificationCode + " 입니다.");

                Transport.send(message);

                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(),
                            "인증번호가 발송되었습니다.",
                            Toast.LENGTH_SHORT).show();
                    showLoading(false);
                    verificationCodeInput.setEnabled(true);
                    verifyCodeButton.setEnabled(true);
                });

            } catch (MessagingException e) {
                requireActivity().runOnUiThread(() -> {
                    showLoading(false);
                    showError("이메일 발송 실패: " + e.getMessage());
                });
            }
        }).start();
    }

    private String generateVerificationCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    private void verifyCode() {
        String inputCode = verificationCodeInput.getText().toString().trim();
        if (inputCode.isEmpty()) {
            showError("인증번호를 입력하세요");
            return;
        }

        if (inputCode.equals(verificationCode)) {
            isVerified = true;
            signupButton.setEnabled(true);
            Toast.makeText(requireContext(), "인증이 완료되었습니다", Toast.LENGTH_SHORT).show();
        } else {
            isVerified = false;
            signupButton.setEnabled(false);
            Toast.makeText(requireContext(), "인증번호가 일치하지 않습니다", Toast.LENGTH_SHORT).show();
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        emailInput.setEnabled(!show);
        passwordInput.setEnabled(!show);
        sendVerificationButton.setEnabled(!show);
        verifyCodeButton.setEnabled(!show && verificationCode != null);
        signupButton.setEnabled(!show && verificationCode != null);
    }

    private void showError(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
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
}