package com.example.ssussemble;

public class ChatData {
    private String userName;
    private String message;
    private long timestamp;

    public ChatData() {}

    public ChatData(String userName, String message, long timestamp) {
        this.userName = userName;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}