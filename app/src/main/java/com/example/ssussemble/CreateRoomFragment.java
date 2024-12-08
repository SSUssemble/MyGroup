package com.example.ssussemble;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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

public class CreateRoomFragment extends Fragment {
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private DatabaseReference chatRoomsRef;
    public String selectedOption;
    public String header;
    public EditText comment;
    private EditText editTextRoomName;
    private Spinner spinnerRoomDescription;
    public EditText userNum;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_room, container, false);

        initializeFirebase();
        initializeViews(view);
        setupSpinner();

        Button buttonCreateRoom = view.findViewById(R.id.button2);
        buttonCreateRoom.setOnClickListener(v -> {
            // SelectLocationFragment로 이동
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new SelectLocationFragment())
                    .addToBackStack(null)
                    .commit();
        });

        getParentFragmentManager().setFragmentResultListener("locationSelected", this, (requestKey, result) -> {
            double latitude = result.getDouble("latitude");
            double longitude = result.getDouble("longitude");

            // 위치 정보 추가 후 방 생성
            createRoomAndChat(latitude, longitude);
        });

        return view;
    }

    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("rooms");
        chatRoomsRef = FirebaseDatabase.getInstance().getReference("chatRooms");
    }

    private void initializeViews(View view) {
        editTextRoomName = view.findViewById(R.id.roomName);
        spinnerRoomDescription = view.findViewById(R.id.spinner);
        comment = view.findViewById(R.id.roomComment);
        userNum = view.findViewById(R.id.editTextNumberDecimal);
    }

    private void setupSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRoomDescription.setAdapter(adapter);

        spinnerRoomDescription.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedOption = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void createRoomAndChat(double latitude, double longitude) {
        String roomName = editTextRoomName.getText().toString();
        String roomDescription = selectedOption;
        String roomComment = comment.getText().toString();
        String roomUserNum = userNum.getText().toString();
        String leaderEmail = MainActivity.Login_id.replace(".", "_dot_").replace("@", "_at_");

        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                String leaderNickname = userSnapshot.child("displayName").getValue(String.class);

                String roomId = databaseReference.push().getKey();

                Room room = new Room(roomId, roomName, roomDescription, roomComment, roomUserNum, leaderEmail);
                room.setLatitude(latitude);
                room.setLongitude(longitude);
                room.setHeader(leaderNickname);

                databaseReference.child(roomId).setValue(room)
                        .addOnSuccessListener(aVoid -> {
                            createChatRoom(roomId, roomName, leaderNickname);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(requireContext(), "방 생성 실패", Toast.LENGTH_SHORT).show();
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "사용자 정보 로드 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void createChatRoom(String roomId, String roomName, String leaderNickname) {
        Map<String, Object> chatRoomMap = new HashMap<>();
        chatRoomMap.put("name", roomName);
        chatRoomMap.put("type", "group");

        Map<String, Boolean> participants = new HashMap<>();
        participants.put(leaderNickname, true);
        chatRoomMap.put("participants", participants);
        chatRoomMap.put("created_at", ServerValue.TIMESTAMP);

        databaseReference.child(roomId).child("participants").child(leaderNickname).setValue(true);

        chatRoomsRef.child(roomId).setValue(chatRoomMap)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(requireContext(), "방 생성 완료", Toast.LENGTH_SHORT).show();
                    getParentFragmentManager().popBackStack();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "채팅방 생성 실패", Toast.LENGTH_SHORT).show();
                });
    }
}