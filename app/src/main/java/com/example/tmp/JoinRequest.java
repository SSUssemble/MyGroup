package com.example.tmp;

public class JoinRequest {
    private String requestId;
    private String roomId;
    private String roomName;
    private String requesterId;
    private String requesterNickname;
    private String leaderId;
    private String status;
    private long timestamp;

    public JoinRequest() {}

    public JoinRequest(String roomId, String roomName, String requesterId,
                       String requesterNickname, String leaderId) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.requesterId = requesterId;
        this.requesterNickname = requesterNickname;
        this.leaderId = leaderId;
        this.status = "pending";
        this.timestamp = System.currentTimeMillis();
    }

    public String getRequesterId() {
        return requesterId;
    }

    public String getRequestId() {
        return requestId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getRequesterNickname() {
        return requesterNickname;
    }

    public String getLeaderId() {
        return leaderId;
    }

    public void setRequesterId(String requesterId) {
        this.requesterId = requesterId;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getRoomName() {
        return roomName;
    }

    public String getStatus() {
        return status;
    }

    public void setRequesterNickname(String requesterNickname) {
        this.requesterNickname = requesterNickname;
    }

    public void setLeaderId(String leaderId) {
        this.leaderId = leaderId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}