package com.example.ssussemble;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.ssussemble.databinding.FragmentChattingWindowBinding;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.TimeZone;

public class ChattingWindowFragment extends Fragment {
    private static final String ARG_TITLE = "title";
    private static final String TAG = "FCM";
    private String title;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private FragmentChattingWindowBinding binding;
    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private LinearLayoutManager layoutManager;
    private String roomId;
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();


    public static ChattingWindowFragment newInstance(String roomId) {
        ChattingWindowFragment fragment = new ChattingWindowFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, roomId);
        args.putString("ROOM_ID", roomId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentChattingWindowBinding.inflate(inflater, container, false);

        if (getArguments() != null) {
            roomId = getArguments().getString("ROOM_ID");
            title = getArguments().getString("title", roomId);

            Toolbar toolbar = binding.cwToolbar;
            toolbar.setTitle(title);
            drawerLayout = binding.drawerLayout;
            toolbar.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.setting) {
                    drawerLayout.openDrawer(Gravity.RIGHT);
                    return true;
                }
                return false;
            });

            binding.navigationView.setNavigationItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.menu_drawer_time) {
                    // 회의 시간 정하기 구현
                    drawerLayout.closeDrawer(Gravity.RIGHT);
                    return true;
                }
                else if (itemId == R.id.menu_drawer_sub) {
                    // 서브 채팅방 프래그먼트로 이동
                    Fragment subChatFragment = SubChatRoomFragment.newInstance(roomId);
                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, subChatFragment)
                            .addToBackStack(null)
                            .commit();
                    drawerLayout.closeDrawer(Gravity.RIGHT);
                    return true;
                }
 //               else if (itemId == R.id.menu_drawer_rate) {
//                    // Navigate to RateFragment
//                    Fragment rateFragment = rateFragment.newInstance();
//                    getParentFragmentManager().beginTransaction()
//                            .replace(R.id.fragment_container, rateFragment)
//                            .addToBackStack(null)
//                            .commit();
//                    drawerLayout.closeDrawer(Gravity.RIGHT);
//                    return true;
//                }
                return false;
            });

            recyclerView = binding.ChattingRecycler;
            layoutManager = new LinearLayoutManager(getContext());
            layoutManager.setStackFromEnd(true);
            recyclerView.setLayoutManager(layoutManager);
            chatAdapter = new ChatAdapter();
            recyclerView.setAdapter(chatAdapter);

            initializeMessageListener();

            binding.ChattingSend.setOnClickListener((view) -> {
                String message = binding.ChattingMessage.getText().toString();
                sendMessage(message);
            });
        }
        return binding.getRoot();
    }

    private void sendMessage(String message) {
        if (!message.trim().isEmpty()) {
            long timeStamp = System.currentTimeMillis() + TimeZone.getTimeZone("Asia/Seoul").getRawOffset();
            ChatData chatData = new ChatData(MainActivity.Login_id, message, timeStamp);

            DatabaseReference messageRef = databaseReference.child("chatRooms")
                    .child(roomId)
                    .child("chats")
                    .push();

            messageRef.setValue(chatData).addOnSuccessListener(aVoid -> {
                sendFCMNotification(message);
                binding.ChattingMessage.setText("");
            });
        }
    }

    private void sendFCMNotification(String message) {
        String FCM_SERVER_URL = "https://mathematical-olwen-ssu-91f12aef.koyeb.app/send-notification";

        DatabaseReference roomRef = databaseReference.child("chatRooms").child(roomId);
        roomRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String roomName = snapshot.child("name").exists() ?
                        snapshot.child("name").getValue(String.class) :
                        "채팅방";

                for (DataSnapshot participantSnapshot : snapshot.child("participants").getChildren()) {
                    String participantId = participantSnapshot.getKey();
                    if (!participantId.equals(MainActivity.Login_id)) {
                        DatabaseReference fcmRef = databaseReference.child("users")
                                .child(participantId)
                                .child("fcmToken");

                        fcmRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                String token = snapshot.getValue(String.class);
                                if (token != null) {
                                    RequestQueue queue = Volley.newRequestQueue(requireContext());
                                    JSONObject requestBody = new JSONObject();
                                    try {
                                        requestBody.put("token", token);
                                        requestBody.put("title", roomName);
                                        requestBody.put("body", message);

                                        JSONObject data = new JSONObject();
                                        data.put("chatRoomId", roomId);
                                        data.put("senderId", MainActivity.Login_id);
                                        data.put("message", message);
                                        requestBody.put("data", data);

                                        JsonObjectRequest request = new JsonObjectRequest(
                                                Request.Method.POST,
                                                FCM_SERVER_URL,
                                                requestBody,
                                                response -> Log.d(TAG, "FCM 전송 성공: " + response.toString()),
                                                error -> Log.e(TAG, "FCM 전송 실패", error)
                                        );

                                        queue.add(request);
                                    } catch (JSONException e) {
                                        Log.e(TAG, "JSON 생성 실패", e);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e(TAG, "FCM 토큰 가져오기 실패", error.toException());
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "채팅방 정보 가져오기 실패", error.toException());
            }
        });
    }

    private void initializeMessageListener() {
        databaseReference.child("chatRooms")
                .child(roomId)
                .child("chats")
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        ChatData chatData = snapshot.getValue(ChatData.class);
                        if (chatData != null) {
                            chatAdapter.addChat(chatData);
                            recyclerView.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
                        }
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {}

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (roomId != null) {
            NavigationManager.getInstance().setCurrentRoomId(roomId);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        NavigationManager.getInstance().setCurrentRoomId(null);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}