package com.example.ssussemble;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_MINE = 1;
    private static final int VIEW_TYPE_OTHER = 2;

    private List<ChatData> chatList = new ArrayList<>();

    public ChatAdapter() {
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_MINE) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_mine, parent, false);
            return new MyChatViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_other, parent, false);
            return new OtherChatViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatData chat = chatList.get(position);

        if (holder.getItemViewType() == VIEW_TYPE_MINE) {
            MyChatViewHolder myHolder = (MyChatViewHolder) holder;
            myHolder.bind(chat);
        } else {
            OtherChatViewHolder otherHolder = (OtherChatViewHolder) holder;
            otherHolder.bind(chat);
        }
    }

    @Override
    public int getItemViewType(int position) {
        ChatData chat = chatList.get(position);

        String nickName = MainActivity.Login_id.substring(0, MainActivity.Login_id.indexOf("@"));
        if (chat.getUserName().equals(nickName)) {
            return VIEW_TYPE_MINE;
        } else {
            return VIEW_TYPE_OTHER;
        }
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public void addChat(ChatData chat) {
        chatList.add(chat);
        notifyItemInserted(chatList.size() - 1);
    }

    static class MyChatViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView timeText;

        MyChatViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.text_message);
            timeText = itemView.findViewById(R.id.text_time);
        }

        void bind(ChatData chat) {
            messageText.setText(chat.getMessage());
            if (chat.getTimestamp() != 0) {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                timeText.setText(sdf.format(new Date(chat.getTimestamp())));
            }
        }
    }

    static class OtherChatViewHolder extends RecyclerView.ViewHolder {
        TextView messageText;
        TextView userNameText;
        TextView timeText;

        OtherChatViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.text_message);
            userNameText = itemView.findViewById(R.id.text_username);
            timeText = itemView.findViewById(R.id.text_time);
        }

        void bind(ChatData chat) {
            messageText.setText(chat.getMessage());
            userNameText.setText(chat.getUserName());
            if (chat.getTimestamp() != 0) {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                sdf.setTimeZone(TimeZone.getTimeZone("Asia/Seoul"));
                timeText.setText(sdf.format(new Date(chat.getTimestamp())));
            }
        }
    }
}