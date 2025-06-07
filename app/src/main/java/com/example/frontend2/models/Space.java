package com.example.frontend2.models;

public class Space {
    private int space_id;
    private int user_id;
    private String name;
    private String type;
    private String furniture;

    // 기본 생성자
    public Space() {}

    // 전체 필드를 받는 생성자
    public Space(int space_id, String name, int user_id, String type, String furniture) {
        this.space_id = space_id;
        this.user_id = user_id;
        this.name = name;
        this.type = type;
        this.furniture = furniture;
    }

    // Getter
    public int getSpace_id() {
        return space_id;
    }

    public int getUser_id() {
        return user_id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getFurniture() {
        return furniture;
    }

    // Setter
    public void setSpace_id(int space_id) {
        this.space_id = space_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setFurniture(String furniture) {
        this.furniture = furniture;
    }
}
