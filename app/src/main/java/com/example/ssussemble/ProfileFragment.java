package com.example.ssussemble;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.ssussemble.databinding.FragmentProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ProfileFragment extends Fragment {
    private FirebaseAuth mAuth;
    private static final String TAG = "ProfileFragment";
    private ImageView profileImageView;
    private ImageView schedule;
    private FragmentProfileBinding binding;
    private TextView groupCountText;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;

    // ActivityResultLauncher 초기화
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<Intent> schedulePickerLauncher;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ActivityResultLauncher 설정
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            uploadImageToFirebase(imageUri);
                        }
                    }
                }
        );

        schedulePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            uploadScheduleToFirebase(imageUri);
                        }
                    }
                }
        );

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        groupCountText = view.findViewById(R.id.groupCount);
        profileImageView = binding.profileImageView;
        schedule = binding.timeTableView;
        databaseReference = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference("profile_images");

        // 이미지 선택 기능 연결
        profileImageView.setOnClickListener(v -> openProfileImagePicker());
        schedule.setOnClickListener(v -> openScheduleImagePicker());

        // 설정 페이지 이동
        ImageView toSetting = binding.settingButton;
        toSetting.setOnClickListener(view1 -> {
            Intent intent = new Intent(getActivity(), AppSetting.class);
            startActivity(intent);
        });

        loadProfileImage();
        loadUserInfo();
        setupNotificationButton();
        loadGroupCount();
        setupGroupClickListener();

        return view;
    }

    private void loadGroupCount() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = databaseReference.child("users").child(currentUserId);

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                String userNickname = userSnapshot.child("displayName").getValue(String.class);

                databaseReference.child("rooms").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int groupCount = 0;
                        for (DataSnapshot roomSnapshot : snapshot.getChildren()) {
                            if (roomSnapshot.child("participants").hasChild(userNickname) ||
                                    userNickname.equals(roomSnapshot.child("header").getValue(String.class))) {
                                groupCount++;
                            }
                        }
                        groupCountText.setText("내가 참가한 그룹 : " + groupCount);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "그룹 수 로드 실패", error.toException());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "사용자 정보 로드 실패", error.toException());
            }
        });
    }

    private void setupGroupClickListener() {
        groupCountText.setOnClickListener(v -> {
            Fragment myGroupsFragment = new MyGroupsFragment();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, myGroupsFragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

    private void loadUserInfo() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            DatabaseReference userRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(currentUser.getUid());

            mAuth = FirebaseAuth.getInstance();
            if (mAuth.getCurrentUser() != null) {
                String currentUserEmail = mAuth.getCurrentUser().getEmail();
                binding.userEmail.setText(currentUserEmail);
            }

            userRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    UserData userData = snapshot.getValue(UserData.class);
                    if (userData != null) {
                        String department = userData.getDepartment() + " " + userData.getGrade() + "학년";
                        binding.userName.setText(userData.getDisplayName());
                        binding.userDepartment.setText(department);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(requireContext(),
                            "사용자 정보 로드 실패",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void setupNotificationButton() {
        binding.notificationButton.setOnClickListener(v -> {
            Fragment requestsFragment = new RequestsFragment();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, requestsFragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

    private void loadProfileImage() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "User not logged in");
            return;
        }

        String userId = currentUser.getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

        userRef.child("profileImageUrl").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String profileImageUrl = snapshot.getValue(String.class);
                if (profileImageUrl != null) {
                    Glide.with(ProfileFragment.this)
                            .load(profileImageUrl)
                            .circleCrop()
                            .placeholder(R.drawable.default_profile)
                            .into(profileImageView);
                } else {
                    Log.e(TAG, "Profile image URL is null");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load profile image", error.toException());
            }
        });
    }


    private void openProfileImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        imagePickerLauncher.launch(Intent.createChooser(intent, "Select Picture"));
    }

    private void openScheduleImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        schedulePickerLauncher.launch(Intent.createChooser(intent, "Select Schedule Picture"));
    }

    private void uploadImageToFirebase(Uri imageUri) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        StorageReference userImageRef = storageReference.child(userId + ".jpg");

        userImageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
            userImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                saveProfileImageUriToDatabase(uri.toString());
                Glide.with(this)
                        .load(uri)
                        .circleCrop()
                        .placeholder(R.drawable.default_profile)
                        .into(profileImageView);
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "이미지 업로드 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void uploadScheduleToFirebase(Uri imageUri) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        StorageReference scheduleRef = storageReference.child("schedule_images").child(userId + "_schedule.jpg"); // schedule_images 경로로 설정

        scheduleRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
            scheduleRef.getDownloadUrl().addOnSuccessListener(uri -> {
                saveScheduleImageUriToDatabase(uri.toString());
                Glide.with(this)
                        .load(uri)
                        .placeholder(R.drawable.baseline_image_24)
                        .into(schedule);
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "시간표 이미지 업로드 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void saveProfileImageUriToDatabase(String imageUrl) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        userRef.child("profileImageUrl").setValue(imageUrl)
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "프로필 이미지 업데이트 성공", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "프로필 이미지 업데이트 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void saveScheduleImageUriToDatabase(String imageUrl) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        userRef.child("scheduleUrl").setValue(imageUrl)
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "시간표 이미지 업데이트 성공", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "시간표 이미지 업데이트 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}