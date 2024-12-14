package com.example.ssussemble;

import java.util.Map;

public class Room {
    private String id; // 방 ID
    private String name; // 방 이름
    private String description; // 방 설명
    private String comment; // 방 코멘트
    private Map<String, Boolean> participants;
    private String userNumMax; // 최대 인원
    private String header; // 방 리더 정보
    private double latitude; // 방 위치 (위도)
    private double longitude; // 방 위치 (경도)

    // 기본 생성자
    public Room() {
    }

    // 모든 필드를 포함한 생성자
    public Room(String id, String name, String description, String comment, String userNumMax, String header) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.comment = comment;
        this.userNumMax = userNumMax;
        this.header = header;
    }

    public int getCurrentParticipants() {
        if (participants == null) {
            return 0;
        }
        return participants.size();
    }

    public void setParticipants(Map<String, Boolean> participants) {
        this.participants = participants;
    }

    // Getter와 Setter
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUserNumMax() {
        return userNumMax;
    }

    public void setUserNumMax(String userNumMax) {
        this.userNumMax = userNumMax;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}