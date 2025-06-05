


package com.example.frontend2.models;


public class Space {
    private int space_id;       // 🔸 추가
    private int user_id;        // 🔸 추가
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

    // ✅ Getter
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

    // ✅ Setter
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
