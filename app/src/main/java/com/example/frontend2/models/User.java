package com.example.frontend2.models;

public class User {
    private int user_id;
    private String name;
    private String email;
    private int family_count;
    private boolean has_pet;
    private int pet_count;  // 🔹 추가된 필드
    private String password;

    // 🔸 Getter
    public int getUser_id() {
        return user_id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public int getFamily_count() {
        return family_count;
    }

    public boolean isHas_pet() {
        return has_pet;
    }

    public int getPet_count() {  // 🔹 추가
        return pet_count;
    }

    public String getPassword() {
        return password;
    }

    // 🔸 Setter
    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setFamily_count(int family_count) {
        this.family_count = family_count;
    }

    public void setHas_pet(boolean has_pet) {
        this.has_pet = has_pet;
    }

    public void setPet_count(int pet_count) {  // 🔹 추가
        this.pet_count = pet_count;
    }
}
