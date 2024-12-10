package com.example.ssussemble;

import com.google.firebase.auth.FirebaseUser;

public class UserData {
    private String uid;
    private String email;
    private String displayName;
    private long createdAt;
    private String profileImageUrl;
    private String department;
    private String grade;

    public UserData() {
    }

    public UserData(FirebaseUser user) {
        this.uid = user.getUid();
        this.email = user.getEmail();
        this.displayName = user.getDisplayName();
        this.createdAt = System.currentTimeMillis();
        this.department = "";
        this.grade = "";
        if (user.getPhotoUrl() != null) {
            this.profileImageUrl = user.getPhotoUrl().toString();
        }
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }
}