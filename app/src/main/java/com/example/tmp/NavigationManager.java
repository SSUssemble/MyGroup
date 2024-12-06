package com.example.tmp;

public class NavigationManager {
    private static NavigationManager instance;
    private String currentRoomId;

    private NavigationManager() {}

    public static NavigationManager getInstance() {
        if (instance == null) {
            instance = new NavigationManager();
        }
        return instance;
    }

    public void setCurrentRoomId(String roomId) {
        this.currentRoomId = roomId;
    }

    public String getCurrentRoomId() {
        return currentRoomId;
    }
}