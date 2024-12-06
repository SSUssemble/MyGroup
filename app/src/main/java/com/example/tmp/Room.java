package com.example.tmp;
//roomId, roomName, roomDescription,roomComment,roomUserNum,header);
public class Room {
    private String name;
    private String description;
    private String id;
    private String comment;
    private String  userNumMax;
    private String header;
    public Room(){

    }

    public Room(String id, String name, String description, String comment,String userNumMax,String header) {
        this.name = name;
        this.id = id;
        this.description = description;
        this.header = header;
        this.comment = comment;
        this.userNumMax = userNumMax;
    }

    public String getComment(){return comment;}

    public String  getUserNumMax(){return userNumMax;}

    public String getHeader(){return header;}

    public String getId(){
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setComment(String comment){
        this.comment = comment;
    }

    public void setUserNumMax(String userNumMax){
        this.userNumMax = userNumMax;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public void setName(String name) {
        this.name = name;
    }
}
