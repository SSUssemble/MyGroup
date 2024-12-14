package com.example.ssussemble;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ParticipantProfileFragment extends Fragment {
    private static final String ARG_USER_ID = "user_id";
    private static final String TAG = "ParticipantProfile";
    private DatabaseReference databaseReference;
    private String userId;

    public static ParticipantProfileFragment newInstance(String userId) {
        ParticipantProfileFragment fragment = new ParticipantProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_USER_ID, userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_participant_profile, container, false);

        if (getArguments() != null) {
            userId = getArguments().getString(ARG_USER_ID);
            loadParticipantProfile();
        }

        return view;
    }

    private void loadParticipantProfile() {
        databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("users").child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String displayName = snapshot.child("displayName").getValue(String.class);
                        String department = snapshot.child("department").getValue(String.class);
                        String grade = snapshot.child("grade").getValue(String.class);

                        updateUI(displayName, department, grade);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "사용자 정보 로드 실패", error.toException());
                    }
                });
    }

    private void updateUI(String displayName, String department, String grade) {
        TextView nameTextView = requireView().findViewById(R.id.participantName);
        TextView departmentTextView = requireView().findViewById(R.id.participantDepartment);
        TextView gradeTextView = requireView().findViewById(R.id.participantGrade);

        nameTextView.setText(displayName);
        departmentTextView.setText(department);
        gradeTextView.setText(grade + "학년");
    }
}
