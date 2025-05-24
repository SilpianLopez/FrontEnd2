package com.example.frontend.models;

public class Space {
    private int space_id;
    private String name;
    private int user_id;

    // Getter
    public int getSpace_id() {
        return space_id;
    }

    public String getName() {
        return name;
    }

    public int getUser_id() {
        return user_id;
    }

    // Setter
    public void setName(String name) {
        this.name = name;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }
}
