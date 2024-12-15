package com.example.ssussemble;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ProfileFragment extends Fragment {
        private FirebaseAuth mAuth;
        private static final int PICK_IMAGE_REQUEST = 1;
        private ImageView profileImageView;
        private ImageView timeTableView;
        private FragmentProfileBinding binding;
        private TextView groupCountText;
        private DatabaseReference databaseReference;
        private TextView mySchedule;
        private ActivityResultLauncher<Intent> imagePickerLauncher;
        private String currentImageType; // To differentiate between profile and timetable image

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            imagePickerLauncher = registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                            Uri imageUri = result.getData().getData();
                            saveImageToInternalStorage(imageUri, currentImageType); // Save the correct type
                            if ("profile".equals(currentImageType)) {

                                Glide.with(this)
                                        .load(imageUri)
                                        .circleCrop()
                                        .placeholder(R.drawable.default_profile)
                                        .into(profileImageView);
                            } else if ("time_table".equals(currentImageType)) {
                                Glide.with(this)
                                        .load(imageUri)
                                        .placeholder(R.drawable.baseline_image_24)
                                        .into(timeTableView);
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
            timeTableView = binding.timeTableView;
            databaseReference = FirebaseDatabase.getInstance().getReference();


            profileImageView.setOnClickListener(view1 -> {
                currentImageType = "profile";
                openImagePicker();
            });


            timeTableView.setOnClickListener(view1 -> {
                currentImageType = "time_table";
                openImagePicker();
            });

            // Navigate to app settings
            ImageView toSetting = binding.settingButton;
            toSetting.setOnClickListener(view1 -> {
                Intent intent = new Intent(getActivity(), AppSetting.class);
                startActivity(intent);
            });

            loadTimeTable();
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
                            Toast.makeText(requireContext(),
                                    "그룹 데이터를 불러오는 중 오류가 발생했습니다.",
                                    Toast.LENGTH_SHORT).show();
                            Log.e("ProfileFragment", "그룹 수 로드 실패", error.toException());
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(requireContext(),
                            "사용자 데이터를 불러오는 중 오류가 발생했습니다.",
                            Toast.LENGTH_SHORT).show();
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
                                "사용자 데이터를 불러오는 중 오류가 발생했습니다.",
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

        private void loadTimeTable() {
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
            String imageFileName = sharedPreferences.getString("time_table_uri", null);

            if (imageFileName != null) {
                File timeTableFile = new File(getActivity().getFilesDir(), imageFileName);

                if (timeTableFile.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(timeTableFile.getAbsolutePath());
                    if (bitmap != null) {
                        timeTableView.setImageBitmap(bitmap);
                    } else {
                        timeTableView.setImageResource(R.drawable.baseline_image_24);
                    }
                }
            }
        }

        private void openImagePicker() {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            imagePickerLauncher.launch(Intent.createChooser(intent, "Select Picture"));
        }

        private void saveImageToInternalStorage(Uri imageUri, String imageType) {
            try {
                InputStream inputStream = getActivity().getContentResolver().openInputStream(imageUri);
                String fileName = imageType.equals("profile") ? "profile_image.jpg" : "time_table_image.jpg";
                FileOutputStream fileOutputStream = getActivity().openFileOutput(fileName, Context.MODE_PRIVATE);

                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    fileOutputStream.write(buffer, 0, length);
                }

                fileOutputStream.close();
                inputStream.close();

                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                if ("profile".equals(imageType)) {
                    editor.putString("profile_image_uri", fileName);
                } else if ("time_table".equals(imageType)) {
                    editor.putString("time_table_uri", fileName);
                }
                editor.apply();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }