package com.example.ssussemble;

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

public class ReceivedRequestsFragment extends Fragment {
    private RecyclerView recyclerView;
    private RequestAdapter adapter;
    private List<JoinRequest> requestList;
    private View rootView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_requests_list, container, false);
        recyclerView = rootView.findViewById(R.id.requestsRecyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        requestList = new ArrayList<>();
        adapter = new RequestAdapter(requestList, true, requireContext());
        recyclerView.setAdapter(adapter);

        loadReceivedRequests();

        return rootView;
    }

    private void loadReceivedRequests() {
        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot userSnapshot) {
                String currentUserNickname = userSnapshot.child("displayName").getValue(String.class);
                requestList.clear();

                FirebaseDatabase.getInstance().getReference("rooms")
                        .orderByChild("header")
                        .equalTo(currentUserNickname)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot roomsSnapshot) {
                                for (DataSnapshot roomSnapshot : roomsSnapshot.getChildren()) {
                                    String roomId = roomSnapshot.getKey();
                                    loadRequestsForRoom(roomId);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e("ReceivedRequests", "방 정보 로드 실패", error.toException());
                            }
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ReceivedRequests", "사용자 정보 로드 실패", error.toException());
            }
        });
    }

    private void loadRequestsForRoom(String roomId) {
        FirebaseDatabase.getInstance().getReference("joinRequests")
                .orderByChild("roomId")
                .equalTo(roomId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot requestSnapshot : snapshot.getChildren()) {
                            JoinRequest request = requestSnapshot.getValue(JoinRequest.class);
                            if (request != null && "pending".equals(request.getStatus())) {
                                requestList.add(request);
                            }
                        }
                        if (requestList.isEmpty()) {
                            rootView.findViewById(R.id.emptyView).setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        } else {
                            rootView.findViewById(R.id.emptyView).setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("ReceivedRequests", "요청 로드 실패", error.toException());
                    }
                });
    }
}