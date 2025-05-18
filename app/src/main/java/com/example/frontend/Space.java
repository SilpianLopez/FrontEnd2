package com.example.frontend;

public class Space {
    private String name;
    private String type;
    private String furniture;

    public Space(String name, String type, String furniture) {
        this.name = name;
        this.type = type;
        this.furniture = furniture;
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
}
