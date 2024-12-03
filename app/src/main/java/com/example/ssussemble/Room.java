package com.example.ssussemble;

public class Room {
    private String name;
    private String description;
    private String id;
    public Room(){

    }

    public Room(String id, String name, String description) {
        this.name = name;
        this.id = id;
        this.description = description;
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
