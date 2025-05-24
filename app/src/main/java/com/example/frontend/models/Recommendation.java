package com.example.frontend.models;

public class Recommendation {
    private int recommendation_id;
    private int user_id;
    private String content;
    private String created_at; // 날짜는 일단 String으로 받아도 충분해요

    // Getter
    public int getRecommendation_id() {
        return recommendation_id;
    }

    public int getUser_id() {
        return user_id;
    }

    public String getContent() {
        return content;
    }

    public String getCreated_at() {
        return created_at;
    }

    // Setter
    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }
}
