package com.example.ssussemble;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParticipantAdapter extends RecyclerView.Adapter<ParticipantAdapter.ViewHolder> {
    private Map<String, String> participants;
    private OnParticipantClickListener listener;

    public interface OnParticipantClickListener {
        void onParticipantClick(String nickname, String uid);
    }

    public ParticipantAdapter(OnParticipantClickListener listener) {
        this.participants = new HashMap<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_participant, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String nickname = new ArrayList<>(participants.keySet()).get(position);
        String uid = participants.get(nickname);
        holder.bind(nickname, uid);
    }

    @Override
    public int getItemCount() {
        return participants.size();
    }

    public void updateParticipants(Map<String, String> newParticipants) {
        this.participants = newParticipants;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView profileImage;
        private TextView participantId;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.participantProfileImage);
            participantId = itemView.findViewById(R.id.participantId);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    String nickname = new ArrayList<>(participants.keySet()).get(position);
                    String uid = participants.get(nickname);
                    listener.onParticipantClick(nickname, uid);
                }
            });
        }

        void bind(String nickname, String uid) {
            participantId.setText(nickname);

            profileImage.setImageResource(R.drawable.default_profile);

            DatabaseReference userRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(uid);

            userRef.child("profileImageUrl").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String imageUrl = snapshot.getValue(String.class);
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        Glide.with(itemView.getContext())
                                .load(imageUrl)
                                .placeholder(R.drawable.default_profile)
                                .error(R.drawable.default_profile)
                                .into(profileImage);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("ParticipantAdapter", "프로필 이미지 로드 실패", error.toException());
                }
            });
        }
    }
}
