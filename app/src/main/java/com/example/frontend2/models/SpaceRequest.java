package com.example.frontend2.models;
public class SpaceRequest {
    private String name;
    private int user_id;
    private String type;
    private String furniture;


    public SpaceRequest(String name, int user_id, String type, String furniture) {
        this.name = name;
        this.user_id = user_id;
        this.type = type;
        this.furniture = furniture;
    }

    // Getter와 Setter가 반드시 있어야 합니다 (Gson 직렬화용)
    public String getName() {
        return name;
    }

    public int getUser_id() {
        return user_id;
    }
    public String getType() { return type; }
    public String getFurniture() { return furniture; }

    public void setName(String name) {
        this.name = name;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }
    public void setType(String type) {  this.type = type; }
    public void setFurniture(String furniture) {  this.furniture = furniture; }
}
