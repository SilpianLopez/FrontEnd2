package com.example.frontend2.models;

public class SpaceRequest {
    private String name;
    private int user_id;
    private String type;
    private String furniture;

    // 생성자 (name, user_id만 받는 버전)
    public SpaceRequest(String name, int user_id) {
        this.name = name;
        this.user_id = user_id;
    }

    // 생성자 (전체 필드를 받는 버전)
    public SpaceRequest(String name, int user_id, String type, String furniture) {
        this.name = name;
        this.user_id = user_id;
        this.type = type;
        this.furniture = furniture;
    }

    // Getter
    public String getName() {
        return name;
    }

    public int getUser_id() {
        return user_id;
    }

    public String getType() {
        return type;
    }

    public String getFurniture() {
        return furniture;
    }

    // Setter
    public void setName(String name) {
        this.name = name;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setFurniture(String furniture) {
        this.furniture = furniture;
    }
}
