package com.example.ssussemble;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.*;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private RecyclerView recyclerViewGroupList;
    private RoomAdapter roomAdapter;
    private DatabaseReference databaseReference;
    private List<Room> roomList;
    private Toolbar toolbar;
    private ArrayList<ChatRoomInfo> groupRoomList;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    public int num;
    public String groupName;
    public String selectedOption;
    public TextView buttonName;

    private String mParam1;
    private String mParam2;

    public HomeFragment(){

    }

    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        int[] colors = {0xFFB1F0F7, 0xFF81BFDA,0xFFF5F0CD , 0xFFFADA7A, Color.CYAN};

        Button button = view.findViewById(R.id.button3);
        button.setOnClickListener(view1 -> {
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container,new CreateRoomFragment())
                    .addToBackStack(null)
                    .commit();
        });
        recyclerViewGroupList = view.findViewById(R.id.recyclerViewClassRoom);
        recyclerViewGroupList.setLayoutManager(new LinearLayoutManager(getContext()));
        roomList = new ArrayList<>();
        roomAdapter = new RoomAdapter(roomList);
        recyclerViewGroupList.setAdapter(roomAdapter);

        databaseReference = FirebaseDatabase.getInstance().getReference("rooms");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                roomList.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Room room = snapshot.getValue(Room.class);
                    roomList.add(room);
                }
                roomAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return view;
    }
}
