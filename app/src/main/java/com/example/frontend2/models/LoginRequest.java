package com.example.frontend2.models;

public class LoginRequest {
    private String email;
    private String password;

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // Getter (선택)
    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
