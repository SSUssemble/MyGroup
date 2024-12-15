package com.example.ssussemble;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.ssussemble.databinding.FragmentHomeBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";
    private FragmentHomeBinding binding;
    private RoomAdapter roomAdapter;
    private DatabaseReference databaseReference;
    private List<Room> roomList;
    private List<Room> allRooms;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);

        ImageView goToMapButton = binding.goToMap;
        goToMapButton.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new MapFragment())
                    .commit();
        });

        initializeViews();
        setupRecyclerView();
        loadRooms();
        setupSearchFilter();
        setupSearchClearButton(); // 검색 초기화 버튼 설정
        return binding.getRoot();
    }

    private void initializeViews() {
        binding.button3.setOnClickListener(v -> navigateToCreateRoom());
    }

    private void setupRecyclerView() {
        allRooms = new ArrayList<>();
        roomList = new ArrayList<>();
        roomAdapter = new RoomAdapter(roomList);
        binding.recyclerViewClassRoom.setLayoutManager(
                new LinearLayoutManager(requireContext()));
        binding.recyclerViewClassRoom.setAdapter(roomAdapter);
    }

    private void loadRooms() {
        databaseReference = FirebaseDatabase.getInstance().getReference("rooms");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                roomList.clear(); // 기존 데이터 초기화
                allRooms.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Room room = snapshot.getValue(Room.class); // Firebase 데이터를 Room 객체로 변환
                    if (room != null) {
                        // participants 필드를 명시적으로 설정
                        DataSnapshot participantsSnapshot = snapshot.child("participants");
                        Map<String, Boolean> participants = (Map<String, Boolean>) participantsSnapshot.getValue();
                        room.setParticipants(participants);

                        roomList.add(room); // roomList에 추가
                        allRooms.add(room);
                    }
                }
                roomAdapter.notifyDataSetChanged(); // RecyclerView 업데이트
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
            }
        });
    }


    private void setupSearchFilter() {
        binding.searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // 검색어 입력 시 필터링
                filterRooms(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
    }

    private void setupSearchClearButton() {
        // searchClearButton 클릭 이벤트 추가
        binding.searchClearButton.setOnClickListener(v -> {
            binding.searchEditText.setText(""); // 검색창 텍스트 초기화
            filterRooms(""); // 전체 방 목록 보여주기
        });
    }

    private void filterRooms(String query) {
        roomList.clear();
        if (query.isEmpty()) {
            // 검색어가 없으면 전체 목록을 보여줌
            roomList.addAll(allRooms);
        } else {
            // 검색어로 필터링
            for (Room room : allRooms) {
                if (room.getName() != null && room.getName().toLowerCase().contains(query.toLowerCase())) {
                    roomList.add(room);
                }
            }
        }
        // 어댑터에 변경 사항 알리기
        roomAdapter.notifyDataSetChanged();
    }

    private void navigateToCreateRoom() {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new CreateRoomFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}