package com.example.frontend2.models; // ★ 실제 프로젝트의 패키지 경로로 수정하세요.

// import com.google.gson.annotations.SerializedName; // JSON 키와 필드명이 다를 경우 사용

public class RecommendationRequest {
    private int userId;      // 추천을 요청하는 사용자의 ID (필수)
    private Integer spaceId; // 특정 공간에 대한 추천을 원할 경우 해당 공간의 ID (선택 사항이므로 Integer)

    /**
     * 생성자: 사용자 ID와 특정 공간 ID를 모두 받는 경우
     * @param userId 사용자 ID
     * @param spaceId 특정 공간 ID (null 가능)
     */
    public RecommendationRequest(int userId, Integer spaceId) {
        this.userId = userId;
        this.spaceId = spaceId;
    }

    /**
     * 생성자: 사용자 ID만 받는 경우 (전체 추천 요청 시)
     * @param userId 사용자 ID
     */
    public RecommendationRequest(int userId) {
        this.userId = userId;
        this.spaceId = null; // spaceId는 명시적으로 null로 설정
    }

    // Getters (Gson이 직렬화를 위해 필요할 수 있음, 또는 디버깅용)
    public int getUserId() {
        return userId;
    }

    public Integer getSpaceId() {
        return spaceId;
    }

    // Setters (객체 생성 후 값을 설정해야 할 경우)
    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setSpaceId(Integer spaceId) {
        this.spaceId = spaceId;
    }
}