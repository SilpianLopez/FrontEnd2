// models/Space.java
package com.example.frontend2.models;

public class Space {
    private String name;
    private String type;
    private String furniture;

    public Space(String name, String type, String furniture) {
        this.name = name;
        this.type = type;
        this.furniture = furniture;
    }

    // 기본 생성자 (서버 응답 처리용)
    public Space() {}

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getFurniture() {
        return furniture;
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
