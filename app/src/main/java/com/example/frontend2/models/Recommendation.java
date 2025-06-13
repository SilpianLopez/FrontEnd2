package com.example.frontend2.models;

public class Recommendation {
    // @SerializedName("recommendation_id")
    private Integer recommendation_id;
    private int user_id;
    private String title;
    private String description;
    private Integer space_id;
    private String type;
    private String content;   // (선택 사항) 파싱 실패 시 원본 저장용
    private String created_at;

    public Recommendation() {}

    // Getters
    public Integer getRecommendation_id() { return recommendation_id; }
    public int getUser_id() { return user_id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Integer getSpace_id() { return space_id; }
    public String getType() { return type; }
    public String getContent() { return content; }
    public String getCreated_at() { return created_at; }
}
