//package com.example.ssussemble;
//
//import android.nfc.Tag;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.fragment.app.Fragment;
//
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//
//public class RoomDetailFragment extends Fragment {
//
//    private static final String ARG_ROOM_ID = "room_id";
//    private static final String ARG_ROOM_NAME = "room_name";
//    private static final String ARG_ROOM_DESCRIPTION = "room_description";
//
//    private DatabaseReference databaseReference;
//
//    public static RoomDetailFragment newInstance(String roomId, String roomName, String roomDescription) {
//        RoomDetailFragment fragment = new RoomDetailFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_ROOM_ID, roomId);
//        args.putString(ARG_ROOM_NAME, roomName);
//        args.putString(ARG_ROOM_DESCRIPTION, roomDescription);
//        fragment.setArguments(args);
//        return fragment;
//    }
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.fragment_room_detail, container, false);
//
//        TextView textViewRoomName = view.findViewById(R.id.textViewRoomName);
//        TextView textViewRoomDescription = view.findViewById(R.id.textViewRoomDescription);
//        Button exitRoomButton = view.findViewById(R.id.exitRoom);
//
//        // Firebase 데이터베이스 참조 초기화
//        databaseReference = FirebaseDatabase.getInstance().getReference("rooms");
//
//        // 방 정보 설정
//        if (getArguments() != null) {
//            String roomId = getArguments().getString(ARG_ROOM_ID);
//            String roomName = getArguments().getString(ARG_ROOM_NAME);
//            String roomDescription = getArguments().getString(ARG_ROOM_DESCRIPTION);
//            textViewRoomName.setText(roomName);
//            textViewRoomDescription.setText(roomDescription);
//
//            exitRoomButton.setOnClickListener(view1 -> {
//                Log.d("ych", "onCreateView: ");
//                // 해당 방의 데이터베이스 삭제
//                if (roomId != null) {
//                    databaseReference.child(roomId).removeValue()
//                            .addOnSuccessListener(aVoid -> {
//                                Toast.makeText(getContext(), "방이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
//                                // 현재 프래그먼트 닫기
//                                getParentFragmentManager().popBackStack();
//                            })
//                            .addOnFailureListener(e -> {
//                                Toast.makeText(getContext(), "방 삭제에 실패했습니다.", Toast.LENGTH_SHORT).show();
//                            });
//                }
//            });
//        }
//
//        return view;
//    }
//}
package com.example.ssussemble;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RoomDetailFragment extends Fragment {

    private static final String ARG_ROOM_ID = "room_id";
    private static final String ARG_ROOM_NAME = "room_name";
    private static final String ARG_ROOM_DESCRIPTION = "room_description";
    private static final String TAG = "RoomDetailFragment";

    private DatabaseReference databaseReference;

    public static RoomDetailFragment newInstance(String roomId, String roomName, String roomDescription) {
        RoomDetailFragment fragment = new RoomDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ROOM_ID, roomId);
        args.putString(ARG_ROOM_NAME, roomName);
        args.putString(ARG_ROOM_DESCRIPTION, roomDescription);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_room_detail, container, false);

        TextView textViewRoomName = view.findViewById(R.id.textViewRoomName);
        TextView textViewRoomDescription = view.findViewById(R.id.textViewRoomDescription);
        Button exitRoomButton = view.findViewById(R.id.exitRoom);

        // Firebase 데이터베이스 참조 초기화
        databaseReference = FirebaseDatabase.getInstance().getReference("rooms");

        // 방 정보 설정
        if (getArguments() != null) {
            String roomId = getArguments().getString(ARG_ROOM_ID);
            String roomName = getArguments().getString(ARG_ROOM_NAME);
            String roomDescription = getArguments().getString(ARG_ROOM_DESCRIPTION);
            textViewRoomName.setText(roomName);
            textViewRoomDescription.setText(roomDescription);

            exitRoomButton.setOnClickListener(view1 -> {
                Log.d(TAG, "exitRoomButton clicked");
                // 해당 방의 데이터베이스 삭제
                if (roomId != null) {
                    databaseReference.child(roomId).removeValue()
                            .addOnSuccessListener(aVoid -> {
                                Log.d(TAG, "Room deletion successful");
                                Toast.makeText(getContext(), "방이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                                // 현재 프래그먼트 닫기
                                getParentFragmentManager().popBackStack();
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Room deletion failed", e);
                                Toast.makeText(getContext(), "방 삭제에 실패했습니다.", Toast.LENGTH_SHORT).show();
                            });
                } else {
                    Log.e(TAG, "Room ID is null");
                }
            });
        } else {
            Log.e(TAG, "Arguments are null");
        }

        return view;
    }
}
