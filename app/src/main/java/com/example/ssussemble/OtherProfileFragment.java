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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class OtherProfileFragment extends Fragment {
    private static final String ARG_USER_ID = "user_id";
    private static final String TAG = "OtherProfileFragment";

    private String userId; // 클릭된 유저의 ID
    private ImageView profileImageView, timeTableView;
    private TextView userNameText, userEmailText, userDepartmentText, groupCountText;
    private ImageView backButton;

    private DatabaseReference databaseReference;

    // **newInstance 메서드**
    public static OtherProfileFragment newInstance(String userId) {
        OtherProfileFragment fragment = new OtherProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USER_ID, userId); // 전달받은 userId를 ARG_USER_ID에 저장
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getString(ARG_USER_ID); // 전달된 userId를 변수에 저장
        }
        databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_other_profile, container, false);

        // XML 레이아웃에서 View 초기화
        profileImageView = view.findViewById(R.id.profileImageView);
        timeTableView = view.findViewById(R.id.timeTableView);
        userNameText = view.findViewById(R.id.userName);
        userEmailText = view.findViewById(R.id.userEmail);
        userDepartmentText = view.findViewById(R.id.userDepartment);
        groupCountText = view.findViewById(R.id.groupCount);
        backButton = view.findViewById(R.id.back_button_image);

        backButton.setOnClickListener(view1 -> {
                getParentFragmentManager().popBackStack();
        });


        loadUserInfo();
        return view;
    }

    private void loadUserInfo() {
        if (userId == null) {
            Log.e(TAG, "User ID is null");
            Toast.makeText(getContext(), "사용자 정보를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        databaseReference.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String displayName = snapshot.child("displayName").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);
                    String department = snapshot.child("department").getValue(String.class);
                    String grade = snapshot.child("grade").getValue(String.class);
                    String profileImageUrl = snapshot.child("profileImageUrl").getValue(String.class);
                    String scheduleImageUrl = snapshot.child("scheduleUrl").getValue(String.class);

                    userNameText.setText(displayName != null ? displayName : "이름 없음");
                    userEmailText.setText(email != null ? email : "이메일 없음");
                    userDepartmentText.setText(department + " " + grade + "학년");

                    // 프로필 이미지 로드
                    if (profileImageUrl != null) {
                        Glide.with(OtherProfileFragment.this)
                                .load(profileImageUrl)
                                .circleCrop()
                                .placeholder(R.drawable.default_profile)
                                .into(profileImageView);
                    }

                    // 시간표 이미지 로드
                    if (scheduleImageUrl != null) {
                        Glide.with(OtherProfileFragment.this)
                                .load(scheduleImageUrl)
                                .placeholder(R.drawable.default_schedule)
                                .into(timeTableView);
                    }

                    loadGroupCount(displayName);
                } else {
                    Toast.makeText(getContext(), "사용자 정보가 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "사용자 정보 로드 실패", error.toException());
                Toast.makeText(getContext(), "사용자 정보 로드 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadGroupCount(String displayName) {
        if (displayName == null) return;

        DatabaseReference roomsRef = FirebaseDatabase.getInstance().getReference("rooms");

        roomsRef.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int groupCount = 0;
                for (DataSnapshot roomSnapshot : snapshot.getChildren()) {
                    if (roomSnapshot.child("participants").hasChild(displayName) ||
                            displayName.equals(roomSnapshot.child("header").getValue(String.class))) {
                        groupCount++;
                    }
                }
                groupCountText.setText("참여한 그룹 수: " + groupCount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "그룹 수 로드 실패", error.toException());
            }
        });
    }
}
