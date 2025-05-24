package com.example.frontend.models;

public class User {
    private int user_id;
    private String name;
    private String email;
    private int family_count;
    private boolean has_pet;
    private String password;

    // 🔸 Getter (필수)
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

    public String getPassword() {
        return password;
    }

    // 🔸 Setter (회원가입 시 필요할 수도 있음)
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
}
