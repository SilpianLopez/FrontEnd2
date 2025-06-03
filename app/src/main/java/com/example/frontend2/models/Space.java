// models/Space.java
package com.example.frontend2.models;

public class Space {
    private Integer space_id;
    private String name;
    private int user_id;
    private String type;
    private String furniture;

    public Space() {}

    public Space(int space_id, String name, int user_id, String type, String furniture) {
        this.space_id = space_id;
        this.name = name;
        this.user_id = user_id;
        this.type = type;
        this.furniture = furniture;
    }

    public int getSpace_id() { return space_id; }
    public String getName() { return name; }
    public int getUser_id() { return user_id; }
    public String getType() { return type; }
    public String getFurniture() { return furniture; }

    public void setSpace_id(int space_id) { this.space_id = space_id; }
    public void setName(String name) { this.name = name; }
    public void setUser_id(int user_id) { this.user_id = user_id; }
    public void setType(String type) { this.type = type; }
    public void setFurniture(String furniture) { this.furniture = furniture; }

}
