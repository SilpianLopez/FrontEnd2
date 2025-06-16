package com.example.frontend2.api;

import com.example.frontend2.models.CleaningRoutine;
import com.example.frontend2.models.RoutineRequest;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RoutineApi {

    /**
     * 새 청소 루틴 생성
     * 백엔드 API 경로: POST /routines
     * 요청 본문: RoutineRequest DTO
     * 응답 본문: 생성된 CleaningRoutine 객체
     */
    @POST("routines")
    Call<CleaningRoutine> createRoutine(@Body RoutineRequest routineRequest);

    /**
     * 특정 청소 루틴 수정
     * 백엔드 API 경로: PUT /routines/{routine_id}
     * 요청 본문: RoutineRequest DTO
     * 응답 본문: 수정된 CleaningRoutine 객체
     */
    @PUT("routines/{routine_id}")
    Call<CleaningRoutine> updateRoutine(@Path("routine_id") int routineId, @Body RoutineRequest routineRequest);

    /**
     * 특정 청소 루틴 삭제
     * 백엔드 API 경로: DELETE /routines/{routine_id}
     */
    @DELETE("routines/{routine_id}")
    Call<Void> deleteRoutine(@Path("routine_id") int routineId);

    /**
     * 특정 사용자의 특정 공간에 대한 모든 루틴 조회
     * 백엔드 API 경로: GET /routines/user/{userId}/space/{spaceId}
     * (ApiClient의 BASE_URL이 http://10.0.2.2:3002/ 라고 가정)
     */
    @GET("routines/user/{userId}/space/{spaceId}") // ◀◀◀ 이 경로를 백엔드와 일치하도록 수정!
    Call<List<CleaningRoutine>> getRoutinesByUserAndSpace(@Path("userId") int userId, @Path("spaceId") int spaceId);

    /**
     * (선택 사항) 특정 사용자의 모든 루틴 조회 (모든 공간 포함)
     * 백엔드 API 경로: GET /routines/user/{userId}
     */
    @GET("routines/user/{userId}")
    Call<List<CleaningRoutine>> getAllRoutinesByUserId(@Path("userId") int userId);

    /**
     * (선택 사항) 특정 루틴 ID로 상세 조회
     * 백엔드 API 경로: GET /routines/{routine_id}
     */
    @GET("routines/{routine_id}")
    Call<CleaningRoutine> getRoutineById(@Path("routine_id") int routineId);

    /**
     * (선택 사항) 특정 사용자의 오늘 할 일 루틴 조회
     * 백엔드 API 경로: GET /routines/today?user_id={userId}
     */
    @GET("routines/today") // ❗️ 백엔드 경로가 /routines/today 인지 /today 인지 확인 필요
    Call<List<CleaningRoutine>> getTodaysRoutines(@Query("user_id") int userId); // 백엔드에서 req.query.user_id로 받음
}