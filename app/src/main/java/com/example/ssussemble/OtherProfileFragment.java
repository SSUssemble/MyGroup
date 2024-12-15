package com.example.ssussemble;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class OtherProfileFragment extends Fragment {
    private static final String ARG_USER_ID = "user_id";
    private static final String TAG = "OtherProfileFragment";

    private String userId; // 평가받는 유저의 ID
    private String currentUserId; // 평가하는 유저의 ID
    private ImageView profileImageView, timeTableView, reputationImage;
    private TextView userNameText, userEmailText, userDepartmentText, groupCountText, myRateText;
    private ImageView backButton, veryGood, good, soso, bad, veryBad;

    private int selectedIndex = -1; // 현재 선택된 이미지의 인덱스
    private final int[] imagesUnselected = {
            R.drawable.very_good, R.drawable.good,
            R.drawable.soso, R.drawable.bad, R.drawable.very_bad
    };

    private final int[] imagesSelected = {
            R.drawable.very_good_color, R.drawable.good_color,
            R.drawable.soso_color, R.drawable.bad_color, R.drawable.very_bad_color
    };

    private final int[] scores = {100, 75, 50, 25, 0};
    private ImageView[] rateImages;

    private DatabaseReference databaseReference;
    private DatabaseReference evaluationsReference; // 유저별 평가 저장소

    public static OtherProfileFragment newInstance(String userId) {
        OtherProfileFragment fragment = new OtherProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USER_ID, userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            userId = getArguments().getString(ARG_USER_ID); // 평가받는 유저의 ID
        }

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            currentUserId = auth.getCurrentUser().getUid();
        }

        databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId);
        evaluationsReference = FirebaseDatabase.getInstance().getReference("evaluations").child(userId);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_other_profile, container, false);

        // View 초기화
        myRateText = view.findViewById(R.id.myRate);
        profileImageView = view.findViewById(R.id.profileImageView);
        timeTableView = view.findViewById(R.id.timeTableView);
        userNameText = view.findViewById(R.id.userName);
        userEmailText = view.findViewById(R.id.userEmail);
        userDepartmentText = view.findViewById(R.id.userDepartment);
        groupCountText = view.findViewById(R.id.groupCount);
        backButton = view.findViewById(R.id.back_button_image);
        veryGood = view.findViewById(R.id.very_good);
        good = view.findViewById(R.id.good);
        soso = view.findViewById(R.id.soso);
        bad = view.findViewById(R.id.bad);
        veryBad = view.findViewById(R.id.very_bad);
        reputationImage = view.findViewById(R.id.reputation_image);

        rateImages = new ImageView[]{veryGood, good, soso, bad, veryBad};

        // 뒤로가기 버튼 클릭 이벤트
        backButton.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        setupImageClickListeners();
        initializeReputation();
        loadUserInfo(); // 사용자 정보 로드 및 이미지 표시
        setupLastSelectedImage();

        return view;
    }

    private void initializeReputation() {
        databaseReference.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Map<String, Object> initialData = new HashMap<>();
                    initialData.put("reputation", 50); // 초기 평판 점수
                    initialData.put("totalScore", 50); // 총점 기본값
                    initialData.put("count", 1); // 카운트 기본값
                    databaseReference.updateChildren(initialData).addOnSuccessListener(aVoid -> {
                        setHighlightedImage(2); // 기본적으로 soso 강조
                        updateReputationDisplay();
                    }).addOnFailureListener(e ->
                            Log.e(TAG, "Failed to initialize reputation", e)
                    );
                } else {
                    updateReputationDisplay();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "initializeReputation failed: " + error.getMessage());
            }
        });
    }

    private void setupLastSelectedImage() {
        evaluationsReference.child(currentUserId).addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int lastSelectedScore = snapshot.getValue(Integer.class);
                    selectedIndex = getIndexFromScore(lastSelectedScore);
                    if (selectedIndex != -1) {
                        setHighlightedImage(selectedIndex); // 마지막 선택한 이미지를 강조
                    }
                } else {
                    setHighlightedImage(2); // 기본적으로 soso 강조
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load last selected image: " + error.getMessage());
            }
        });
    }

    private int getIndexFromScore(int score) {
        for (int i = 0; i < scores.length; i++) {
            if (scores[i] == score) {
                return i;
            }
        }
        return -1;
    }

    private void setupImageClickListeners() {
        for (int i = 0; i < rateImages.length; i++) {
            int index = i;
            rateImages[i].setOnClickListener(v -> {
                if (selectedIndex != -1 && selectedIndex == index) {
                    Toast.makeText(getContext(), "이미 선택한 평점입니다.", Toast.LENGTH_SHORT).show();
                } else {
                    updateReputation(index);
                }
            });
        }
    }

    private void updateReputation(int index) {
        if (selectedIndex != -1) {
            resetImage(selectedIndex);
        }
        setHighlightedImage(index);
        selectedIndex = index;
        int newScore = scores[index];

        evaluationsReference.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                evaluationsReference.child(currentUserId).setValue(newScore)
                        .addOnSuccessListener(aVoid -> {
                            calculateReputation();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(getContext(), "평가 업데이트 실패", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "평가 로드 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateReputation() {
        evaluationsReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long totalScore = 0;
                long count = 0;

                for (DataSnapshot evaluationSnapshot : snapshot.getChildren()) {
                    Integer score = evaluationSnapshot.getValue(Integer.class);
                    if (score != null) {
                        totalScore += score;
                        count++;
                    }
                }

                double newReputation = count > 0 ? (double) totalScore / count : 50.0;

                Map<String, Object> updates = new HashMap<>();
                updates.put("totalScore", totalScore);
                updates.put("count", count);
                updates.put("reputation", newReputation);

                databaseReference.updateChildren(updates)
                        .addOnSuccessListener(aVoid -> updateReputationDisplay())
                        .addOnFailureListener(e ->
                                Toast.makeText(getContext(), "평판 업데이트 실패", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "평판 계산 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void updateReputationDisplay() {
        databaseReference.child("reputation").addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                double reputation = snapshot.exists() ? snapshot.getValue(Double.class) : 50;
                int roundedReputation = (int) Math.round(reputation);

                if (roundedReputation >= 90) {
                    reputationImage.setImageResource(R.drawable.very_good_color);
                } else if (roundedReputation >= 60) {
                    reputationImage.setImageResource(R.drawable.good_color);
                } else if (roundedReputation >= 40) {
                    reputationImage.setImageResource(R.drawable.soso_color);
                } else if (roundedReputation >= 10) {
                    reputationImage.setImageResource(R.drawable.bad_color);
                } else {
                    reputationImage.setImageResource(R.drawable.very_bad_color);
                }

                myRateText.setText("평가: " + roundedReputation);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "평판 로드 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setHighlightedImage(int index) {
        rateImages[index].setImageResource(imagesSelected[index]);
    }

    private void resetImage(int index) {
        rateImages[index].setImageResource(imagesUnselected[index]);
    }

    private void loadUserInfo() {
        databaseReference.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    userNameText.setText(snapshot.child("displayName").getValue(String.class));
                    userEmailText.setText(snapshot.child("email").getValue(String.class));
                    userDepartmentText.setText(snapshot.child("department").getValue(String.class));

                    // 프로필 이미지 로드
                    String profileImageUrl = snapshot.child("profileImageUrl").getValue(String.class);
                    if (profileImageUrl != null) {
                        Glide.with(OtherProfileFragment.this)
                                .load(profileImageUrl)
                                .circleCrop()
                                .placeholder(R.drawable.default_profile)
                                .into(profileImageView);
                    }

                    // 시간표 이미지 로드
                    String timeTableImageUrl = snapshot.child("scheduleUrl").getValue(String.class);
                    if (timeTableImageUrl != null) {
                        Glide.with(OtherProfileFragment.this)
                                .load(timeTableImageUrl)
                                .placeholder(R.drawable.baseline_image_24)
                                .into(timeTableView);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "사용자 정보 로드 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }
}