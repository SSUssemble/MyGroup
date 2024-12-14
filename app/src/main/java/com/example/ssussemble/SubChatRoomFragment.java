package com.example.ssussemble;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import java.text.SimpleDateFormat;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SubChatRoomFragment extends Fragment {
    private static final String TAG = "SubChatRoomFragment";
    private RecyclerView recyclerView;
    private ChatRoomAdapter adapter;
    private ArrayList<ChatRoomInfo> subChatRoomList;
    private String parentRoomId;
    private DatabaseReference databaseReference;

    public static SubChatRoomFragment newInstance(String parentRoomId) {
        SubChatRoomFragment fragment = new SubChatRoomFragment();
        Bundle args = new Bundle();
        args.putString("PARENT_ROOM_ID", parentRoomId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sub_chat_rooms, container, false);

        parentRoomId = getArguments().getString("PARENT_ROOM_ID");
        databaseReference = FirebaseDatabase.getInstance().getReference();

        setupToolbar(view);
        setupRecyclerView(view);
        setupCreateButton(view);
        loadSubChatRooms();

        return view;
    }

    private void setupToolbar(View view) {
        Toolbar toolbar = view.findViewById(R.id.toolbar_sub_chat);
        toolbar.setTitle("서브 채팅방");
    }

    private void setupRecyclerView(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewSubChatRooms);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        subChatRoomList = new ArrayList<>();
        adapter = new ChatRoomAdapter(subChatRoomList);
        recyclerView.setAdapter(adapter);
        setupClickListener();
    }

    private void setupClickListener() {
        recyclerView.addOnItemTouchListener(new GroupFragment.RecyclerTouchListener(
                requireContext(),
                recyclerView,
                new GroupFragment.ClickListener() {
                    @Override
                    public void onClick(View view, int position) {
                        ChatRoomInfo selectedRoom = subChatRoomList.get(position);
                        NavigationManager.getInstance().setCurrentRoomId(selectedRoom.Chatting_room_id);
                        Fragment chatFragment = ChattingWindowFragment.newInstance(selectedRoom.Chatting_room_id);
                        Bundle args = new Bundle();
                        args.putString("ROOM_ID", selectedRoom.Chatting_room_id);
                        args.putString("title", selectedRoom.Chatting_room_name);
                        chatFragment.setArguments(args);

                        getParentFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, chatFragment)
                                .addToBackStack(null)
                                .commit();
                    }

                    @Override
                    public void onLongClick(View view, int position) {}
                }));
    }

    private void setupCreateButton(View view) {
        view.findViewById(R.id.btnCreateSubChat).setOnClickListener(v ->
                showCreateSubChatDialog());
    }

    private void showCreateSubChatDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_create_sub_chat, null);
        EditText input = dialogView.findViewById(R.id.etSubChatRoomName);

        new AlertDialog.Builder(requireContext())
                .setTitle("서브채팅방 만들기")
                .setView(dialogView)
                .setPositiveButton("만들기", (dialog, which) -> {
                    String title = input.getText().toString();
                    if (!title.isEmpty()) {
                        createSubChatRoom(title);
                    }
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private void createSubChatRoom(String title) {
        String subRoomId = "sub_" + parentRoomId + "_" + System.currentTimeMillis();

        databaseReference.child("chatRooms").child(subRoomId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            databaseReference.child("chatRooms").child(parentRoomId)
                                    .child("participants")
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot participantsSnapshot) {
                                            Map<String, Object> roomData = new HashMap<>();
                                            roomData.put("type", "sub");
                                            roomData.put("parentRoomId", parentRoomId);
                                            roomData.put("name", title);
                                            roomData.put("participants", participantsSnapshot.getValue());
                                            roomData.put("created_at", ServerValue.TIMESTAMP);

                                            databaseReference.child("chatRooms").child(subRoomId)
                                                    .setValue(roomData)
                                                    .addOnSuccessListener(aVoid -> {
                                                        Fragment chatFragment = ChattingWindowFragment.newInstance(subRoomId);
                                                        Bundle args = new Bundle();
                                                        args.putString("ROOM_ID", subRoomId);
                                                        args.putString("title", title);
                                                        chatFragment.setArguments(args);

                                                        getParentFragmentManager().beginTransaction()
                                                                .replace(R.id.fragment_container, chatFragment)
                                                                .addToBackStack(null)
                                                                .commit();
                                                    });
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Log.e(TAG, "참가자 정보 가져오기 실패", error.toException());
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "채팅방 확인 실패", error.toException());
                    }
                });
    }

    private void loadSubChatRooms() {
        databaseReference.child("chatRooms")
                .orderByChild("parentRoomId")
                .equalTo(parentRoomId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        subChatRoomList.clear();
                        for (DataSnapshot roomSnapshot : snapshot.getChildren()) {
                            try {
                                if (!"sub".equals(roomSnapshot.child("type").getValue(String.class))) {
                                    continue;
                                }

                                ChatRoomInfo chatRoom = new ChatRoomInfo();
                                chatRoom.Chatting_room_id = roomSnapshot.getKey();
                                chatRoom.isthis_chatroom_group = true;

                                if (roomSnapshot.child("name").exists()) {
                                    String displayName = roomSnapshot.child("name").getValue(String.class);
                                    chatRoom.Chatting_room_name = displayName;
                                    chatRoom.Chatting_room_id = roomSnapshot.getKey();
                                }

                                subChatRoomList.add(chatRoom);
                                setupLastMessageListener(chatRoom);
                            } catch (Exception e) {
                                Log.e(TAG, "서브채팅방 정보 처리 중 오류", e);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "서브채팅방 목록 로드 실패", error.toException());
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
}