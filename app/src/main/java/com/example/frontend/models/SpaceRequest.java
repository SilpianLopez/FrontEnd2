package com.example.frontend.models;
public class SpaceRequest {
    private String name;
    private int user_id;

    public SpaceRequest(String name, int user_id) {
        this.name = name;
        this.user_id = user_id;
    }

    // Getter와 Setter가 반드시 있어야 합니다 (Gson 직렬화용)
    public String getName() {
        return name;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }
}
