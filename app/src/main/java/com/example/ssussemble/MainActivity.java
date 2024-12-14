package com.example.ssussemble;

import static android.Manifest.permission.POST_NOTIFICATIONS;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private ImageButton goToMapButton;
    public static String Login_id = null;
    public static String Login_password = null;
    private static final int NOTIFICATION_PERMISSION_CODE = 100;
    private static final String TAG = "FCM_Service";

    private List<Fragment> fragmentList = new ArrayList<>();
    private FragmentManager fragmentManager;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{POST_NOTIFICATIONS}, 1);
            }
        }

        initializeComponents();
        goToMapButton = findViewById(R.id.go_to_map);
        goToMapButton.setVisibility(View.GONE);

        setupFragments();
        setupBottomNavigation();
        setupGoToMapButton();
        handleLoginData();
        checkLoginStatus();
        initializeFCM();

        if (getIntent().hasExtra("chatRoomId")) {
            String chatRoomId = getIntent().getStringExtra("chatRoomId");
            navigateToChatRoom(chatRoomId);
        }
    }

    private void initializeFCM() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "FCM 토큰 가져오기 실패", task.getException());
                        return;
                    }

                    String token = task.getResult();
                    Log.d(TAG, "FCM 토큰: " + token);

                    if (Login_id != null) {
                        String userKey = Login_id.replace(".", "_dot_").replace("@", "_at_");
                        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                                .child("users")
                                .child(userKey)
                                .child("fcmToken");

                        userRef.setValue(token)
                                .addOnSuccessListener(aVoid -> Log.d(TAG, "FCM 토큰 저장 성공"))
                                .addOnFailureListener(e -> Log.e(TAG, "FCM 토큰 저장 실패", e));
                    } else {
                        Log.e(TAG, "사용자가 로그인하지 않았습니다.");
                    }
                });
    }

    private void checkLoginStatus() {
        if (mAuth.getCurrentUser() == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new LoginFragment())
                    .commit();
            bottomNavigationView.setVisibility(View.GONE);
        } else {
            Login_id = mAuth.getCurrentUser().getEmail();
            showHomeFragment();
            bottomNavigationView.setVisibility(View.VISIBLE);
        }
    }

    public void onLoginSuccess() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            Login_id = user.getEmail();
            initializeFCM();
            bottomNavigationView.setVisibility(View.VISIBLE);
            showHomeFragment();
            bottomNavigationView.setSelectedItemId(R.id.navigation_home);
        }
    }

    public void onLogout() {
        mAuth.signOut();
        Login_id = null;
        Login_password = null;
        bottomNavigationView.setVisibility(View.GONE);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new LoginFragment())
                .commit();
    }

    private void initializeComponents() {
        fragmentManager = getSupportFragmentManager();
        bottomNavigationView = findViewById(R.id.bottom_navigation);
    }

    private void setupFragments() {
        initializeFragmentList();
        showInitialFragment();
    }

    private void initializeFragmentList() {
        fragmentList.add(new GroupFragment());   // index 0
        fragmentList.add(new HomeFragment());    // index 1
        fragmentList.add(new ProfileFragment()); // index 2
    }

    private void showInitialFragment() {
        replaceFragment(fragmentList.get(1));
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.navigation_home);
        bottomNavigationView.setOnItemSelectedListener(item -> handleBottomNavigationItemSelected(item));
    }

    private void setupGoToMapButton() {
        goToMapButton.setOnClickListener(view -> {
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

            if (currentFragment instanceof MapFragment) {
                // 현재 MapFragment가 보이는 경우 -> HomeFragment로 전환
                replaceFragment(new HomeFragment());
            } else {
                // 현재 HomeFragment 또는 다른 Fragment가 보이는 경우 -> MapFragment로 전환
                replaceFragment(new MapFragment());
            }
        });
    }

    private boolean handleBottomNavigationItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.navigation_group) {
            replaceFragment(fragmentList.get(0));
            return true;
        } else if (itemId == R.id.navigation_home) {
            replaceFragment(fragmentList.get(1));
            return true;
        } else if (itemId == R.id.navigation_profile) {
            replaceFragment(fragmentList.get(2));
            return true;
        }
        return false;
    }

    private void handleLoginData() {
        SharedPreferences mPref = getSharedPreferences("LoginData", MODE_PRIVATE);
        if(mPref != null) {
            Login_id = mPref.getString("LoginId", null);
            Login_password = mPref.getString("LoginPassword", Login_password);
            setTitle(Login_id + "님 안녕하세요!");
        }
    }

    private void showHomeFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new HomeFragment())
                .commit();
    }

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();

        if (goToMapButton != null) {
            if (fragment instanceof HomeFragment || fragment instanceof MapFragment) {
                goToMapButton.setVisibility(View.VISIBLE);
            } else {
                goToMapButton.setVisibility(View.GONE);
            }
        }
    }

    public void navigateToChatRoom(String roomId) {
        Fragment chatFragment = ChattingWindowFragment.newInstance(roomId);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, chatFragment)
                .addToBackStack(null)
                .commit();
    }
}