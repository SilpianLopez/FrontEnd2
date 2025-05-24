package com.example.frontend.models;

public class RegisterRequest {
    private String name;
    private String email;
    private String password;
    private int family_count;
    private boolean has_pet;

    public RegisterRequest(String name, String email, String password, int family_count, boolean has_pet) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.family_count = family_count;
        this.has_pet = has_pet;
    }

    // Getter (선택)
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public int getFamily_count() { return family_count; }
    public boolean isHas_pet() { return has_pet; }
}
