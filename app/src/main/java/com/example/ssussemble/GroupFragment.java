package com.example.ssussemble;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
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

        toolbar.inflateMenu(R.menu.menu_group);
        toolbar.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.create_chat) {
                showCreateChatDialog();
                return true;
            } else if (itemId == R.id.create_group_chat) {
                showCreateGroupChatDialog();
                return true;
            } else if (itemId == R.id.bell) {
                Toast.makeText(getContext(), "알림 클릭", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
    }

    private void showCreateChatDialog() {
        databaseReference.child("users")
                .orderByChild("email")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ArrayList<String> users = new ArrayList<>();
                        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                            String email = userSnapshot.child("email").getValue(String.class);
                            if (email != null && !email.equals(MainActivity.Login_id)) {
                                users.add(email);
                            }
                        }

                        if (users.isEmpty()) {
                            Toast.makeText(getContext(), "대화 가능한 사용자가 없습니다.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        new AlertDialog.Builder(requireContext())
                                .setTitle("대화상대 선택")
                                .setItems(users.toArray(new String[0]), (dialog, which) -> {
                                    String selectedUser = users.get(which);
                                    createOneToOneChatRoom(selectedUser);
                                })
                                .show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "사용자 목록 로드 실패", error.toException());
                    }
                });
    }

    private void createOneToOneChatRoom(String otherUserEmail) {
        String currentUserEmail = MainActivity.Login_id.replace(".", "_dot_").replace("@", "_at_");
        String otherUserEmailKey = otherUserEmail.replace(".", "_dot_").replace("@", "_at_");
        String roomId = "chat_" + currentUserEmail + "_" + otherUserEmailKey;

        databaseReference.child("chatRooms").child(roomId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            Map<String, Object> roomData = new HashMap<>();
                            Map<String, Boolean> participants = new HashMap<>();

                            participants.put(currentUserEmail, true);
                            participants.put(otherUserEmailKey, true);

                            roomData.put("type", "one_to_one");
                            roomData.put("participants", participants);
                            roomData.put("created_at", ServerValue.TIMESTAMP);

                            databaseReference.child("chatRooms").child(roomId)
                                    .setValue(roomData)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "1:1 채팅방 생성 성공");
                                        if (isAdded()) {
                                            navigateToChatRoom(roomId);
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "1:1 채팅방 생성 실패", e);
                                        if (isAdded()) {
                                            Toast.makeText(getContext(), "채팅방 생성에 실패했습니다.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            if (isAdded()) {
                                navigateToChatRoom(roomId);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "채팅방 확인 실패", error.toException());
                    }
                });
    }

    private void createGroupChatRoom(ArrayList<String> participants) {
        String currentUserEmail = MainActivity.Login_id.replace(".", "_dot_").replace("@", "_at_");

        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(currentUserEmail);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String currentUserNickname = snapshot.child("displayName").getValue(String.class);
                String roomId = "group_" + System.currentTimeMillis();
                String roomName = "그룹 채팅방 " + participants.size() + "명";

                Map<String, Object> roomData = new HashMap<>();
                Map<String, Boolean> participantsMap = new HashMap<>();

                participantsMap.put(currentUserNickname, true);

                for (String participantEmail : participants) {
                    String participantId = participantEmail.replace(".", "_dot_").replace("@", "_at_");
                    DatabaseReference participantRef = FirebaseDatabase.getInstance()
                            .getReference("users")
                            .child(participantId);

                    participantRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot participantSnapshot) {
                            String participantNickname = participantSnapshot.child("displayName").getValue(String.class);
                            participantsMap.put(participantNickname, true);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.e(TAG, "참가자 정보 로드 실패", error.toException());
                        }
                    });
                }

                roomData.put("type", "group");
                roomData.put("participants", participantsMap);
                roomData.put("created_at", ServerValue.TIMESTAMP);
                roomData.put("name", roomName);

                databaseReference.child("chatRooms").child(roomId)
                        .setValue(roomData)
                        .addOnSuccessListener(aVoid -> {
                            navigateToChatRoom(roomId);
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "사용자 정보 로드 실패", error.toException());
            }
        });
    }

    private void navigateToChatRoom(String roomId) {
        Fragment chatFragment = ChattingWindowFragment.newInstance(roomId);
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, chatFragment)
                .addToBackStack(null)
                .commit();
    }

    private void showCreateGroupChatDialog() {
        databaseReference.child("users")
                .orderByChild("email")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ArrayList<String> users = new ArrayList<>();
                        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                            String email = userSnapshot.child("email").getValue(String.class);
                            if (email != null && !email.equals(MainActivity.Login_id)) {
                                users.add(email);
                            }
                        }

                        if (users.isEmpty()) {
                            Toast.makeText(getContext(), "초대할 사용자가 없습니다.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        boolean[] checkedItems = new boolean[users.size()];
                        new AlertDialog.Builder(requireContext())
                                .setTitle("그룹 채팅방 만들기")
                                .setMultiChoiceItems(users.toArray(new String[0]), checkedItems, (dialog, which, isChecked) -> {
                                    checkedItems[which] = isChecked;
                                })
                                .setPositiveButton("만들기", (dialog, which) -> {
                                    ArrayList<String> selectedUsers = new ArrayList<>();
                                    selectedUsers.add(MainActivity.Login_id);
                                    for (int i = 0; i < checkedItems.length; i++) {
                                        if (checkedItems[i]) {
                                            selectedUsers.add(users.get(i));
                                        }
                                    }
                                    if (selectedUsers.size() > 1) {
                                        createGroupChatRoom(selectedUsers);
                                    }
                                })
                                .setNegativeButton("취소", null)
                                .show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "사용자 목록 로드 실패", error.toException());
                    }
                });
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
                                } else {
                                    chatRoom.isthis_chatroom_group = false;
                                    for (DataSnapshot participantSnapshot : participantsSnapshot.getChildren()) {
                                        String participantNickname = participantSnapshot.getKey();
                                        if (!participantNickname.equals(currentUserNickname)) {
                                            chatRoom.id2 = participantNickname;
                                            break;
                                        }
                                    }
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