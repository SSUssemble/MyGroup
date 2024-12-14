package com.example.ssussemble;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.*;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ProfileFragment extends Fragment {
    private FirebaseAuth mAuth;
    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView profileImageView;
    private FragmentProfileBinding binding;
    private TextView groupCountText;
    private DatabaseReference databaseReference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        groupCountText = view.findViewById(R.id.groupCount);
        profileImageView = binding.profileImageView;
        databaseReference = FirebaseDatabase.getInstance().getReference();

        // Handle profile image click
        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImagePicker();
            }
        });

        // Navigate to app settings
        ImageView toSetting = binding.settingButton;
        toSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), AppSetting.class);
                startActivity(intent);
            }
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
                        Log.e("ProfileFragment", "그룹 수 로드 실패", error.toException());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ProfileFragment", "사용자 정보 로드 실패", error.toException());
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
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        String imageFileName = sharedPreferences.getString("profile_image_uri", null);

        if (imageFileName != null) {
            File profileImageFile = new File(getActivity().getFilesDir(), imageFileName);

            if (profileImageFile.exists()) {
                Uri imageUri = Uri.fromFile(profileImageFile);

                Glide.with(this)
                        .load(imageUri)
                        .circleCrop()
                        .placeholder(R.drawable.default_profile)
                        .into(profileImageView);
            }
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            saveImageToInternalStorage(imageUri);
            Glide.with(this)
                    .load(imageUri)
                    .circleCrop()
                    .placeholder(R.drawable.default_profile)
                    .into(profileImageView);
        }
    }

    private void saveImageToInternalStorage(Uri imageUri) {
        try {
            InputStream inputStream = getActivity().getContentResolver().openInputStream(imageUri);
            FileOutputStream fileOutputStream = getActivity().openFileOutput("profile_image.jpg", Context.MODE_PRIVATE);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                fileOutputStream.write(buffer, 0, length);
            }

            fileOutputStream.close();
            inputStream.close();

            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("profile_image_uri", "profile_image.jpg"); // Save the filename
            editor.apply();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
