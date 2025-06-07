package com.example.frontend2.api;

//import com.example.frontend.data.model.Recommendation;        // ★ 실제 경로로 변경
//import com.example.frontend.data.model.RecommendationRequest; // ★ 실제 경로로 변경

import com.example.frontend2.models.Recommendation;
import com.example.frontend2.models.RecommendationRequest;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE; // 추천 삭제 기능을 넣는다면 필요
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface AiRoutineApi {

    /**
     * Gemini AI 추천 루틴 생성 요청
     * 백엔드 엔드포인트: POST /recommendations/generate
     * 요청 본문: RecommendationRequest 객체 (userId, 선택적으로 spaceId 포함)
     * 응답 본문: 생성된 Recommendation 객체 목록
     */
    @POST("recommendations/generate")
    Call<List<Recommendation>> generateAiRoutineRecommendations(@Body RecommendationRequest request);

    /**
     * 특정 사용자의 모든 AI 추천 루틴 조회
     * 백엔드 엔드포인트: GET /recommendations/user/:userId
     */
    @GET("recommendations/user/{userId}")
    Call<List<Recommendation>> getUserAiRecommendations(@Path("userId") int userId);

    /**
     * (선택 사항) 특정 AI 추천 루틴 삭제
     * 백엔드 엔드포인트: DELETE /recommendations/:recommendationId
     */
    @DELETE("recommendations/{recommendationId}")
    Call<Void> deleteAiRecommendation(@Path("recommendationId") int recommendationId);

}