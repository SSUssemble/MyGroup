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
    public String selectedOption;
    public String header;
    public EditText comment;
    private EditText editTextRoomName;
    private Spinner spinnerRoomDescription;
    public EditText userNum;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_room, container, false);
        spinnerRoomDescription = view.findViewById(R.id.spinner);
        userNum = view.findViewById(R.id.editTextNumberDecimal);
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

            String roomName = editTextRoomName.getText().toString();
            String roomDescription = selectedOption;
            String roomComment = comment.getText().toString();
            String roomUserNum = userNum.getText().toString();

            getParentFragmentManager().popBackStack();
        });
    }
    }