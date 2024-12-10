package com.example.ssussemble;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.MapView;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;

public class SelectLocationFragment extends Fragment implements OnMapReadyCallback {
    private MapView mapView;
    private NaverMap naverMap;
    private LatLng selectedLocation; // 선택된 위치
    private Marker centerMarker; // 고정된 마커

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_select_location, container, false);

        mapView = view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        Button confirmButton = view.findViewById(R.id.confirmLocationButton);
        confirmButton.setOnClickListener(v -> {
            if (selectedLocation != null) {
                Bundle result = new Bundle();
                result.putDouble("latitude", selectedLocation.latitude);
                result.putDouble("longitude", selectedLocation.longitude);

                // 이전 Fragment에 데이터 전달
                getParentFragmentManager().setFragmentResult("locationSelected", result);
                getParentFragmentManager().popBackStack(); // 이전 화면으로 돌아가기
            } else {
                Toast.makeText(requireContext(), "위치를 선택하세요.", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        this.naverMap = naverMap;

        LatLng initialLocation = new LatLng(37.496451, 126.957179); // 숭실대 중심
        CameraUpdate cameraUpdate = CameraUpdate.scrollTo(initialLocation);
        naverMap.moveCamera(cameraUpdate);

        // 마커 초기화
        centerMarker = new Marker();
        centerMarker.setPosition(initialLocation); // 초기 위치에 마커 배치
        centerMarker.setMap(naverMap); // 마커를 지도에 추가

        // 카메라 이동 리스너로 마커 위치 동기화
        naverMap.addOnCameraChangeListener((reason, animated) -> {
            selectedLocation = naverMap.getCameraPosition().target;
            centerMarker.setPosition(selectedLocation); // 마커를 카메라 중심으로 이동
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
