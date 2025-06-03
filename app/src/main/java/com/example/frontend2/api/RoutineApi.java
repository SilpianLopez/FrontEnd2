package com.example.frontend2.api;

import com.example.frontend2.models.CleaningRoutine;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET; // 루틴 조회용
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface RoutineApi {

    /**
     * 새 청소 루틴 생성
     * 요청 본문: CleaningRoutine 객체 (space_id, user_id, title, description, repeat_unit, repeat_interval, first_due_date 등 포함)
     * 응답 본문: 생성된 CleaningRoutine 객체 (routine_id, last_cleaned_at, next_due_date 등 서버에서 설정된 값 포함)
     */
    @POST("routines")
    Call<CleaningRoutine> createRoutine(@Body CleaningRoutine routine);

    /**
     * 특정 청소 루틴 수정
     * 요청 본문: CleaningRoutine 객체 (수정할 필드만 포함 가능, 백엔드 구현에 따라 다름)
     * 응답 본문: 수정된 CleaningRoutine 객체
     */
    @PUT("routines/{routine_id}") // 백엔드 라우트의 파라미터 이름 (예: :id, :routineId 등)과 일치
    Call<CleaningRoutine> updateRoutine(@Path("routine_id") int routineId, @Body CleaningRoutine routine);

    /**
     * 특정 청소 루틴 삭제
     * 응답 본문: 없음 (성공/실패만 확인)
     */
    @DELETE("routines/{routine_id}")
    Call<Void> deleteRoutine(@Path("routine_id") int routineId);

    /**
     * 특정 사용자의 모든 루틴 조회 (예시 추가)
     */
    @GET("routines/user/{userId}")
    Call<List<CleaningRoutine>> getRoutinesByUser(@Path("userId") int userId);

    /**
     * 특정 루틴 상세 조회 (예시 추가)
     */
    @GET("routines/{routine_id}")
    Call<CleaningRoutine> getRoutineById(@Path("routine_id") int routineId);

}