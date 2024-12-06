package com.example.tmp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MyGroupsFragment extends Fragment {
    private RecyclerView recyclerView;
    private MyGroupsAdapter adapter;
    private List<Room> groupList;
    private DatabaseReference databaseReference;
    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_my_groups, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewMyGroups);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        groupList = new ArrayList<>();
        adapter = new MyGroupsAdapter(groupList, requireContext());
        recyclerView.setAdapter(adapter);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        loadMyGroups();

        return view;
    }

    private void loadMyGroups() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRef = databaseReference.child("users").child(currentUserId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                String userNickname = userSnapshot.child("displayName").getValue(String.class);

                databaseReference.child("rooms").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        groupList.clear();
                        for (DataSnapshot roomSnapshot : snapshot.getChildren()) {
                            if (roomSnapshot.child("participants").hasChild(userNickname) ||
                                    userNickname.equals(roomSnapshot.child("header").getValue(String.class))) {
                                Room room = roomSnapshot.getValue(Room.class);
                                if (room != null) {
                                    groupList.add(room);
                                }
                            }
                        }
                        adapter.notifyDataSetChanged();

                        if (groupList.isEmpty()) {
                            recyclerView.setVisibility(View.GONE);
                            view.findViewById(R.id.emptyGroupsView).setVisibility(View.VISIBLE);
                        } else {
                            recyclerView.setVisibility(View.VISIBLE);
                            view.findViewById(R.id.emptyGroupsView).setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("MyGroupsFragment", "그룹 로드 실패", error.toException());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("MyGroupsFragment", "사용자 정보 로드 실패", error.toException());
            }
        });
    }
}