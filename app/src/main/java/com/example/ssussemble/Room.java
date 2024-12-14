package com.example.ssussemble;
public class Room {

    public Room(){
    }

    public Room(String id, String name, String description, String comment,String userNumMax,String header) {
        this.id = id;
        this.description = description;
        this.comment = comment;
        this.userNumMax = userNumMax;
    }

    public String getId(){
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
