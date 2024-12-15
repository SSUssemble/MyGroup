package com.example.ssussemble;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ssussemble.databinding.FragmentMapBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.Marker;

import java.util.ArrayList;
import java.util.List;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private MapView mapView;
    private NaverMap naverMap;
    private EditText searchEditText;
    private Button searchClearButton, createGroupButton;
    private List<Room> roomList = new ArrayList<>();
    private List<Marker> markerList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        ImageView goToHomeButton = view.findViewById(R.id.go_to_home);
        goToHomeButton.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        });

        // Initialize views
        mapView = view.findViewById(R.id.navermap);
        searchEditText = view.findViewById(R.id.search_edit_text);
        searchClearButton = view.findViewById(R.id.searchClearButton);
        createGroupButton = view.findViewById(R.id.button3);

        // Initialize MapView
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // Set up button and search actions
        setupButtonActions();
        setupSearchFunctionality();

        return view;
    }

    private void setupButtonActions() {
        // Clear search field when "x" button is clicked
        searchClearButton.setOnClickListener(v -> searchEditText.setText(""));

        // Navigate to CreateRoomFragment on button click
        createGroupButton.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new CreateRoomFragment())
                    .addToBackStack(null)
                    .commit();
        });
    }

    private void setupSearchFunctionality() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No action needed before text change
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterMarkersByName(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No action needed after text change
            }
        });
    }

    private void filterMarkersByName(String query) {
        if (naverMap == null) return;

        // Clear all markers from the map
        for (Marker marker : markerList) {
            marker.setMap(null);
        }

        if (query.isEmpty()) {
            // If the query is empty, show all markers
            for (Marker marker : markerList) {
                marker.setMap(naverMap);
            }
        } else {
            // Show only markers matching the query
            for (int i = 0; i < roomList.size(); i++) {
                Room room = roomList.get(i);
                Marker marker = markerList.get(i);
                if (room.getName().toLowerCase().contains(query.toLowerCase())) {
                    marker.setMap(naverMap);
                }
            }
        }
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        this.naverMap = naverMap;

        LatLng initialLocation = new LatLng(37.496451, 126.957179); // 숭실대 중심
        CameraUpdate cameraUpdate = CameraUpdate.scrollTo(initialLocation);
        naverMap.moveCamera(cameraUpdate);

        loadRoomData();
    }

    private void loadRoomData() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference roomsRef = database.getReference("rooms");

        roomsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                roomList.clear(); // Clear previous data
                markerList.clear(); // Clear previous markers

                for (DataSnapshot roomSnapshot : snapshot.getChildren()) {
                    Room room = roomSnapshot.getValue(Room.class); // Convert snapshot to Room object
                    if (room != null) {
                        roomList.add(room);

                        // Create and add marker for the room
                        Marker marker = new Marker();
                        marker.setPosition(new LatLng(room.getLatitude(), room.getLongitude()));
                        marker.setCaptionText(room.getName());
                        marker.setMap(naverMap);

                        markerList.add(marker);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "방 정보를 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}
