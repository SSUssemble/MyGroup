package com.example.ssussemble;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {
    private List<JoinRequest> requests;
    private boolean isReceivedRequest;
    private final WeakReference<Context> contextRef;

    public RequestAdapter(List<JoinRequest> requests, boolean isReceivedRequest, Context context) {
        this.requests = requests;
        this.isReceivedRequest = isReceivedRequest;
        this.contextRef = new WeakReference<>(context);
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_request, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        JoinRequest request = requests.get(position);
        holder.roomNameText.setText(request.getRoomName());
        holder.senderName.setText(request.getRequesterNickname());
        if (isReceivedRequest) {
            holder.buttonLayout.setVisibility(View.VISIBLE);
            holder.senderName.setOnClickListener(v -> {
                Context context = contextRef.get();
                if (context != null) {
                    Fragment participantProfileFragment = OtherProfileFragment.newInstance(request.getRequesterId());
                    ((MainActivity) context).getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, participantProfileFragment)
                            .addToBackStack(null)
                            .commit();
                }
            });
            holder.acceptButton.setOnClickListener(v -> handleAccept(request, holder.getAdapterPosition()));
            holder.rejectButton.setOnClickListener(v -> handleReject(request, holder.getAdapterPosition()));
        } else {
            holder.senderName.setText("수락 대기중"); // 굳이 별도의 텍스트 만들 필요 없으므로 보낸 요청일시 수락 대기중으로 설정
            holder.buttonLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    static class RequestViewHolder extends RecyclerView.ViewHolder {
        TextView roomNameText;
        LinearLayout buttonLayout;
        Button acceptButton;
        Button rejectButton;
        TextView senderName;

        RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            roomNameText = itemView.findViewById(R.id.roomNameText);
            senderName = itemView.findViewById(R.id.senderName);
            buttonLayout = itemView.findViewById(R.id.buttonLayout);
            acceptButton = itemView.findViewById(R.id.acceptButton);
            rejectButton = itemView.findViewById(R.id.rejectButton);
        }
    }

    private void handleAccept(JoinRequest request, int position) {
        Context context = contextRef.get();
        if (context == null) return;

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        Map<String, Object> updates = new HashMap<>();
        updates.put("rooms/" + request.getRoomId() + "/participants/" + request.getRequesterNickname(), request.getRequesterId());
        updates.put("chatRooms/" + request.getRoomName() + "/participants/" + request.getRequesterNickname(), request.getRequesterId());

        rootRef.updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    DatabaseReference requestRef = FirebaseDatabase.getInstance()
                            .getReference("joinRequests")
                            .child(request.getRequestId());
                    requestRef.removeValue()
                            .addOnSuccessListener(aVoid2 -> {
                                Toast.makeText(context, "참가 요청이 수락되었습니다.", Toast.LENGTH_SHORT).show();
                                requests.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, requests.size());
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "참가자 추가 실패", Toast.LENGTH_SHORT).show();
                });
    }

    private void handleReject(JoinRequest request, int position) {
        Context context = contextRef.get();
        if (context == null) return;

        DatabaseReference requestRef = FirebaseDatabase.getInstance()
                .getReference("joinRequests")
                .child(request.getRequestId());

        requestRef.removeValue()
                .addOnSuccessListener(aVoid -> {
                    requests.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, requests.size());
                    Toast.makeText(context, "참가 요청이 거절되었습니다.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "요청 거절 실패", Toast.LENGTH_SHORT).show();
                });
    }
}
