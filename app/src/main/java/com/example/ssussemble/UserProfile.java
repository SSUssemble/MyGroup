package com.example.ssussemble;

public class UserProfile {
    private String name;
    private String profileImageUrl;

    public UserProfile(String name, String profileImageUrl) {
        this.name = name;
        this.profileImageUrl = profileImageUrl;
    }

    public String getName() {
        return name;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }
}
