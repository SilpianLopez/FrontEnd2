package com.example.frontend2.api;


import com.example.frontend2.models.RecommendationRoutineRequest;

import com.example.frontend2.CleaningList;
import com.example.frontend2.models.CompleteRoutineRequest;
import com.example.frontend2.models.RoutineRequest;
import com.example.frontend2.models.CleaningRoutine;
import com.example.frontend2.models.CleaningLogRecommendation;
import com.google.gson.JsonObject;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface CleaningRoutineApi {

    /**
     * 루틴 생성
     */
    @POST("routines")
    Call<CleaningRoutine> createRoutine(@Body RoutineRequest request);

    /**
     * 특정 공간의 루틴 목록 조회
     */
    @GET("routines/space/{spaceId}")
    Call<List<CleaningList>> getRoutinesBySpace(@Path("spaceId") int spaceId);

    /**
     * 오늘의 루틴 조회
     */
    @GET("routines/today")
    Call<List<CleaningRoutine>> getTodaysRoutines(@Query("user_id") int userId);

    /**
     * 루틴 삭제
     */
    @DELETE("routines/{routine_id}")
    Call<Void> deleteRoutine(@Path("routine_id") int routineId);

    /**
     * 루틴 수정
     */
    @PUT("routines/{routine_id}")
    Call<CleaningRoutine> updateRoutine(
            @Path("routine_id") int routineId,
            @Body RoutineRequest request
    );

    /**
     * 특정 날짜(date)의 next_due_date에 맞는 루틴만 조회
     * GET /routines/by-date/{userId}/{date}
     * @param userId 로그인된 사용자 ID
     * @param date   "YYYY-MM-DD" 포맷의 날짜
     */
    @GET("routines/by-date/{userId}/{date}")
    Call<List<CleaningRoutine>> getRoutinesByDate(
            @Path("userId") int userId,
            @Path("date")   String date
    );

    @POST("routines/toggle-complete") // 경로도 백엔드와 일치하게
    Call<Void> toggleRoutineComplete(@Body CompleteRoutineRequest request);

    /**
     * 다음 알림 예정 루틴 조회 (예: 가장 임박한 next_due_date 루틴 하나 조회)
     * GET /routines/next-alarm/{userId}
     */
    /**
     * 다음 알림 예정 루틴 조회 (예: 가장 임박한 next_due_date 루틴 하나 조회)
     * GET /routines/next-alarm/{userId}
     */
    @GET("routines/next-alarm/{userId}")
    Call<CleaningRoutine> getNextAlarmRoutine(@Path("userId") int userId);
    @GET("routines/user/{userId}")
    Call<List<CleaningRoutine>> getRoutinesByUser(@Path("userId") int userId);

    @POST("cleaning-routine")
    Call<Void> createRecommendationRoutine(@Body RecommendationRoutineRequest request);

    @GET("routine/completed/{userId}/{date}")
    Call<List<CleaningRoutine>> getCompletedRoutinesByDate(
            @Path("userId") int userId,
            @Path("date") String date
    );

}