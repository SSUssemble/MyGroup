package com.example.ssussemble;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;


public class ChatRoomAdapter extends RecyclerView.Adapter<ChatRoomAdapter.ViewHolder> {


    private final ArrayList<ChatRoomInfo> chatting_room_list;

    public ChatRoomAdapter(ArrayList<ChatRoomInfo> chatting_room_list) {
        this.chatting_room_list = chatting_room_list;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_group_line, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        ChatRoomInfo chatRoom = chatting_room_list.get(position);

        if(chatRoom.isthis_chatroom_group) {
            if (chatRoom.group_id != null && !chatRoom.group_id.isEmpty()) {
                String id = chatRoom.group_id.get(0);
                for(int j = 1; j < chatRoom.group_id.size(); j++) {
                    id += ", " + chatRoom.group_id.get(j);
                }
                holder.friend_id.setText(chatRoom.Chatting_room_id);
            } else {
                holder.friend_id.setText(chatRoom.Chatting_room_id);
            }
        }

        holder.last_text.setText(chatRoom.last_message != null ? chatRoom.last_message : "");
        holder.last_time.setText(chatRoom.last_message_time != null ? chatRoom.last_message_time : "");
        if (chatRoom.bitmap != null) {
            holder.iv.setImageBitmap(chatRoom.bitmap);
        }
    }

    @Override
    public int getItemCount() {
        return chatting_room_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView friend_id, last_time, last_text;
        ImageView iv;

        public ViewHolder(View view) {
            super(view);

            friend_id = view.findViewById(R.id.cr_friend_id);
            last_time = view.findViewById(R.id.cr_last_time);
            last_text = view.findViewById(R.id.cr_last_text);
            iv = view.findViewById(R.id.cr_profile_image);
        }


    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}