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

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CreateRoomFragment extends Fragment {
    public String selectedOption;public String groupName;
    private EditText editTextRoomName;
    private Spinner spinnerRoomDescription;
    private DatabaseReference databaseReference;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 레이아웃을 확장하여 프래그먼트의 뷰를 만듭니다.
        View view = inflater.inflate(R.layout.fragment_create_room, container, false);
        editTextRoomName = view.findViewById(R.id.editTextText);
        spinnerRoomDescription = view.findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRoomDescription.setAdapter(adapter);

        // Spinner 이벤트 리스너 설정
        spinnerRoomDescription.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedOption = parent.getItemAtPosition(position).toString();
                Toast.makeText(getContext(), "Selected: " + selectedOption, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // 선택이 취소되었을 때의 동작
            }
        });
        Button buttonCreateRoom = view.findViewById(R.id.button2);

        databaseReference = FirebaseDatabase.getInstance().getReference("rooms");
        buttonCreateRoom.setOnClickListener(view1 -> {
            String roomName = editTextRoomName.getText().toString();
            String roomDescription = selectedOption;

            String roomId = databaseReference.push().getKey();
            Room room = new Room(roomId, roomName, roomDescription);
            databaseReference.child(roomId).setValue(room);

            getParentFragmentManager().popBackStack();
        });

        return view;
    }
    public String getEditTextValue(EditText editText) {
        return editText != null ? editText.getText().toString():"";
    }
}
