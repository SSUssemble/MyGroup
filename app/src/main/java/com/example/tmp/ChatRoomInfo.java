package com.example.tmp;

import android.graphics.Bitmap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ChatRoomInfo implements Serializable {
    Bitmap bitmap;
    String id1;
    String id2;
    String Chatting_room_id;
    String Chatting_room_name;
    String last_message_time;
    String last_message;
    String last_message_id;
    boolean isthis_chatroom_group;
    ArrayList<String> group_id;
    private List<String> participantTokens;

    ChatRoomInfo() {
        this.id1 = null;
        this.id2 = null;
        this.last_message_time = null;
        this.last_message = null;
        this.last_message_id = null;
        this.isthis_chatroom_group = false;
        this.group_id = new ArrayList<>();
        this.bitmap = null;
        this.participantTokens = new ArrayList<>();
    }

    ChatRoomInfo(ArrayList<String> group_id) {
        this.isthis_chatroom_group = true;
        this.last_message_time = null;
        this.last_message = null;
        this.last_message_id = null;
        this.group_id = group_id;
        this.bitmap = null;
        this.Chatting_room_id = "room";
        this.participantTokens = new ArrayList<>();
    }

    public void addParticipantToken(String token) {
        if (!participantTokens.contains(token)) {
            participantTokens.add(token);
        }
    }
}