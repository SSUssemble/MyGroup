package com.example.ssussemble;

import android.os.Bundle;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class MyRoomFragment extends Fragment {
    private static final String ARG_ROOM_ID = "room_id";
    private static final String ARG_ROOM_NAME = "room_name";
    private static final String ARG_ROOM_DESCRIPTION = "room_description";
    private static final String ARG_ROOM_COMMENT = "room_comment";
    private static final String TAG = "RoomDetailFragment";

    private DatabaseReference databaseReference;
    private String roomId;
    private String roomName;
    private RecyclerView recyclerViewParticipants;
    private ParticipantAdapter participantAdapter;

    public static RoomDetailFragment newInstance(String roomId, String roomName, String roomDescription, String roomComment) {
        RoomDetailFragment fragment = new RoomDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ROOM_ID, roomId);
        args.putString(ARG_ROOM_NAME, roomName);
        args.putString(ARG_ROOM_DESCRIPTION, roomDescription);
        args.putString(ARG_ROOM_COMMENT, roomComment);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_room_detail, container, false);

        TextView textViewRoomName = view.findViewById(R.id.textViewRoomName);
        TextView textViewRoomDescription = view.findViewById(R.id.textViewRoomDescription);
        TextView textViewRoomComment = view.findViewById(R.id.textViewRoomComment);
        Button exitRoomButton = view.findViewById(R.id.exitRoom);
        recyclerViewParticipants = view.findViewById(R.id.recyclerViewParticipants);

        databaseReference = FirebaseDatabase.getInstance().getReference("rooms");

        if (getArguments() != null) {
            roomId = getArguments().getString(ARG_ROOM_ID);
            roomName = getArguments().getString(ARG_ROOM_NAME);
            String roomDescription = getArguments().getString(ARG_ROOM_DESCRIPTION);
            String roomComment = getArguments().getString(ARG_ROOM_COMMENT);

            textViewRoomName.setText(roomName);
            textViewRoomDescription.setText(roomDescription);
            textViewRoomComment.setText(roomComment);

            setupRecyclerView();
            loadParticipants();
            setupExitButton(exitRoomButton);
        }

        return view;
    }

    private void setupRecyclerView() {
        recyclerViewParticipants.setLayoutManager(new LinearLayoutManager(getContext()));
        participantAdapter = new ParticipantAdapter(this::onParticipantClick);
        recyclerViewParticipants.setAdapter(participantAdapter);
    }

    private void loadParticipants() {
        databaseReference.child(roomId).child("participants")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Map<String, String> participants = new HashMap<>();
                        for (DataSnapshot participantSnapshot : snapshot.getChildren()) {
                            String nickname = participantSnapshot.getKey();
                            String uid = participantSnapshot.getValue(String.class);
                            participants.put(nickname, uid);
                        }
                        participantAdapter.updateParticipants(participants);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "참가자 목록 로드 실패", error.toException());
                    }
                });
    }

    private void setupExitButton(Button exitRoomButton) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        databaseReference.child(roomId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String header = snapshot.child("header").getValue(String.class);
                boolean isLeader = currentUserId.equals(snapshot.child("participants").child(header).getValue(String.class));
                boolean isParticipant = snapshot.child("participants").hasChild(currentUserId);

                exitRoomButton.setEnabled(isParticipant);
                exitRoomButton.setOnClickListener(v -> {
                    if (isLeader) {
                        deleteRoom();
                    } else {
                        leaveRoom(currentUserId);
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "방 정보 로드 실패", error.toException());
            }
        });
    }

    private void deleteRoom() {
        databaseReference.child(roomId).removeValue()
                .addOnSuccessListener(aVoid -> {
                    FirebaseDatabase.getInstance().getReference("chatRooms").child(roomId).removeValue();
                    Toast.makeText(getContext(), "방이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                    getParentFragmentManager().popBackStack();
                });
    }

    private void leaveRoom(String userId) {
        databaseReference.child(roomId).child("participants").child(userId).removeValue()
                .addOnSuccessListener(aVoid -> {
                    FirebaseDatabase.getInstance().getReference("chatRooms")
                            .child(roomId).child("participants").child(userId).removeValue();
                    Toast.makeText(getContext(), "그룹에서 나갔습니다.", Toast.LENGTH_SHORT).show();
                    getParentFragmentManager().popBackStack();
                });
    }

    private void onParticipantClick(String participantNickname, String participantUid) {
        OtherProfileFragment otherProfileFragment = OtherProfileFragment.newInstance(participantUid);
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, otherProfileFragment)
                .addToBackStack(null)
                .commit();
    }
}