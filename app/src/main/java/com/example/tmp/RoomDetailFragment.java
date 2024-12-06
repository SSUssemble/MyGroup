package com.example.tmp;

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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;


public class RoomDetailFragment extends Fragment {

    private static final String ARG_ROOM_ID = "room_id";
    private static final String ARG_ROOM_NAME = "room_name";
    private static final String ARG_ROOM_DESCRIPTION = "room_description";
    private static final String ARG_ROOM_COMMENT = "room_comment";
    private static final String TAG = "RoomDetailFragment";

    private DatabaseReference databaseReference;

    private Button joinRequestButton;
    private String roomId;
    private String roomName;

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
        joinRequestButton = view.findViewById(R.id.joinRequestButton);

        databaseReference = FirebaseDatabase.getInstance().getReference("rooms");

        if (getArguments() != null) {
            roomId = getArguments().getString(ARG_ROOM_ID);
            roomName = getArguments().getString(ARG_ROOM_NAME);
            String roomDescription = getArguments().getString(ARG_ROOM_DESCRIPTION);
            String roomComment = getArguments().getString(ARG_ROOM_COMMENT);

            textViewRoomName.setText(roomName);
            textViewRoomDescription.setText(roomDescription);
            textViewRoomComment.setText(roomComment);

            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(currentUserId);

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                    String userNickname = userSnapshot.child("displayName").getValue(String.class);

                    databaseReference.child(roomId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot roomSnapshot) {
                            String header = roomSnapshot.child("header").getValue(String.class);
                            boolean isLeader = userNickname.equals(header);
                            boolean isParticipant = roomSnapshot.child("participants").hasChild(userNickname) || isLeader;

                            exitRoomButton.setEnabled(isParticipant);
                            joinRequestButton.setEnabled(!isParticipant);

                            exitRoomButton.setOnClickListener(view1 -> {
                                if (isLeader) {
                                    databaseReference.child(roomId).removeValue()
                                            .addOnSuccessListener(aVoid -> {
                                                FirebaseDatabase.getInstance()
                                                        .getReference("chatRooms")
                                                        .child(roomId)
                                                        .removeValue();
                                                Toast.makeText(getContext(), "방이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                                                getParentFragmentManager().popBackStack();
                                            });
                                } else {
                                    databaseReference.child(roomId).child("participants")
                                            .child(userNickname)
                                            .removeValue()
                                            .addOnSuccessListener(aVoid -> {
                                                FirebaseDatabase.getInstance()
                                                        .getReference("chatRooms")
                                                        .child(roomId)
                                                        .child("participants")
                                                        .child(userNickname)
                                                        .removeValue();
                                                Toast.makeText(getContext(), "그룹에서 나갔습니다.", Toast.LENGTH_SHORT).show();
                                                getParentFragmentManager().popBackStack();
                                            });
                                }
                            });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e(TAG, "Database error: " + error.getMessage());
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Database error: " + error.getMessage());
                }
            });
        }

        joinRequestButton.setOnClickListener(v -> sendJoinRequest());

        return view;
    }

    private void sendJoinRequest() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(currentUserId);
        DatabaseReference roomRef = FirebaseDatabase.getInstance().getReference("rooms").child(roomId);

        roomRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot roomSnapshot) {
                String leaderId = roomSnapshot.child("header").getValue(String.class);

                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                        String requesterNickname = userSnapshot.child("displayName").getValue(String.class);

                        // 참가 요청 데이터 생성
                        DatabaseReference requestsRef = FirebaseDatabase.getInstance().getReference("joinRequests");
                        String requestId = requestsRef.push().getKey();

                        Map<String, Object> requestData = new HashMap<>();
                        requestData.put("requestId", requestId);
                        requestData.put("roomId", roomId);
                        requestData.put("roomName", roomName);
                        requestData.put("requesterId", currentUserId);
                        requestData.put("requesterNickname", requesterNickname);
                        requestData.put("leaderId", leaderId);
                        requestData.put("status", "pending");
                        requestData.put("timestamp", ServerValue.TIMESTAMP);

                        requestsRef.child(requestId).setValue(requestData)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(requireContext(), "참가 신청이 전송되었습니다.", Toast.LENGTH_SHORT).show();
                                    joinRequestButton.setEnabled(false);
                                    joinRequestButton.setText("신청 완료");
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(requireContext(), "참가 신청 실패", Toast.LENGTH_SHORT).show();
                                    Log.e(TAG, "참가 신청 실패", e);
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(requireContext(), "사용자 정보 로드 실패", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "사용자 정보 로드 실패", error.toException());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "방 정보 로드 실패", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "방 정보 로드 실패", error.toException());
            }
        });
    }
}