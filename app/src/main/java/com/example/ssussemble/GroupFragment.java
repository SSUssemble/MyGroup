package com.example.ssussemble;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.*;

public class GroupFragment extends Fragment {
    private static final String TAG = "GroupFragment";
    private RecyclerView recyclerViewChatRooms;
    private Toolbar toolbar;
    private ChatRoomAdapter adapter;
    private ArrayList<ChatRoomInfo> chattingRoomList;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_group, container, false);

        initializeFirebase();
        setupToolbar(view);
        setupRecyclerView(view);
        loadChatRooms();

        return view;
    }

    private void initializeFirebase() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
    }

    private void setupToolbar(View view) {
        toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitleTextAppearance(getContext(), R.style.BoldFont);
    }

    private void setupRecyclerView(View view) {
        recyclerViewChatRooms = view.findViewById(R.id.recyclerViewChatRooms);
        recyclerViewChatRooms.setLayoutManager(new LinearLayoutManager(getContext()));
        chattingRoomList = new ArrayList<>();
        adapter = new ChatRoomAdapter(chattingRoomList);
        recyclerViewChatRooms.setAdapter(adapter);
        setupChatRoomClickListener();
    }

    private void loadChatRooms() {
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                String currentUserNickname = userSnapshot.child("displayName").getValue(String.class);

                databaseReference.child("chatRooms").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        chattingRoomList.clear();
                        for (DataSnapshot roomSnapshot : snapshot.getChildren()) {
                            DataSnapshot participantsSnapshot = roomSnapshot.child("participants");
                            if (participantsSnapshot.hasChild(currentUserNickname)) {
                                String roomId = roomSnapshot.getKey();
                                ChatRoomInfo chatRoom = new ChatRoomInfo();
                                chatRoom.Chatting_room_id = roomId;

                                if (roomSnapshot.child("type").getValue(String.class).equals("group")) {
                                    chatRoom.isthis_chatroom_group = true;
                                    chatRoom.group_id = new ArrayList<>();
                                    for (DataSnapshot participantSnapshot : participantsSnapshot.getChildren()) {
                                        chatRoom.group_id.add(participantSnapshot.getKey());
                                    }
                                    chatRoom.Chatting_room_id = roomSnapshot.child("name").getValue(String.class);
                                } else if (roomSnapshot.child("type").getValue(String.class).equals("sub")) {
                                    continue;
                                }

                                chattingRoomList.add(chatRoom);
                                setupLastMessageListener(chatRoom);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "채팅방 로드 실패", error.toException());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "사용자 정보 로드 실패", error.toException());
            }
        });
    }

    private void setupLastMessageListener(ChatRoomInfo chatRoom) {
        databaseReference.child("chatRooms")
                .child(chatRoom.Chatting_room_id)
                .child("chats")
                .limitToLast(1)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot messageSnapshot : snapshot.getChildren()) {
                                ChatData chatData = messageSnapshot.getValue(ChatData.class);
                                if (chatData != null) {
                                    chatRoom.last_message = chatData.getMessage();
                                    chatRoom.last_message_id = chatData.getUserName();
                                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                                    chatRoom.last_message_time = sdf.format(new Date(chatData.getTimestamp()));
                                    adapter.notifyDataSetChanged();
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Database error: " + error.getMessage());
                    }
                });
    }

    private void setupChatRoomClickListener() {
        recyclerViewChatRooms.addOnItemTouchListener(new RecyclerTouchListener(
                requireContext(),
                recyclerViewChatRooms,
                new ClickListener() {
                    @Override
                    public void onClick(View view, int position) {
                        ChatRoomInfo selectedRoom = chattingRoomList.get(position);
                        NavigationManager.getInstance().setCurrentRoomId(selectedRoom.Chatting_room_id);

                        Fragment chatFragment = ChattingWindowFragment.newInstance(
                                selectedRoom.Chatting_room_id);

                        getParentFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, chatFragment)
                                .addToBackStack(null)
                                .commit();
                    }

                    @Override
                    public void onLongClick(View view, int position) {
                    }
                }));
    }

    public interface ClickListener {
        void onClick(View view, int position);
        void onLongClick(View view, int position);
    }

    public static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {
        private final GestureDetector gestureDetector;
        private final ClickListener clickListener;

        public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final ClickListener clickListener) {
            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && clickListener != null) {
                        clickListener.onLongClick(child, recyclerView.getChildAdapterPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                clickListener.onClick(child, rv.getChildAdapterPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {}

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {}
    }
}