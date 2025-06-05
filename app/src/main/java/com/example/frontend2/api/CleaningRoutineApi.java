package com.example.frontend2.api;

import com.example.frontend2.models.CleaningRoutine; // ❗️ 응답 및 목록 조회에 사용할 주된 루틴 모델
import com.example.frontend2.models.RoutineRequest;  // ❗️ 루틴 생성 및 수정 요청 시 사용할 DTO

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE; // 삭제 기능을 넣는다면 필요
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query; // Query 파라미터 사용 시

public interface CleaningRoutineApi { // 또는 RoutineApi 등 일관된 이름 사용

    /**
     * 새 청소 루틴 생성
     * 요청 본문: RoutineRequest DTO (space_id, user_id, title, description, repeat_unit, repeat_interval, first_due_date)
     * 응답 본문: 생성된 CleaningRoutine 객체 (routine_id, next_due_date 등 서버에서 생성된 값 포함)
     */
    @POST("routines") // 백엔드 라우트: POST /routines
    Call<CleaningRoutine> createRoutine(@Body RoutineRequest routineRequest);

    /**
     * 특정 청소 루틴 수정
     * 요청 본문: RoutineRequest DTO (변경할 필드 포함)
     * 응답 본문: 수정된 CleaningRoutine 객체
     */
    @PUT("routines/{routine_id}") // 백엔드 라우트 파라미터 이름과 일치해야 함 (예: :id, :routineId)
    Call<CleaningRoutine> updateRoutine(@Path("routine_id") int routineId, @Body RoutineRequest routineRequest);

    /**
     * 특정 청소 루틴 삭제
     * 응답 본문: 없음 (성공/실패만 확인)
     */
    @DELETE("routines/{routine_id}")
    Call<Void> deleteRoutine(@Path("routine_id") int routineId);

    /**
     * 특정 사용자의 특정 공간에 대한 모든 루틴 조회
     * 예시 백엔드 API 경로: GET /users/{userId}/spaces/{spaceId}/routines
     * 또는 GET /routines?userId={userId}&spaceId={spaceId} 등 실제 경로에 맞게 수정
     */
    @GET("users/{userId}/spaces/{spaceId}/routines") // ★ 실제 백엔드 엔드포인트 경로로 반드시 수정!
    Call<List<CleaningRoutine>> getRoutinesByUserAndSpace(@Path("userId") int userId, @Path("spaceId") int spaceId);

    /**
     * (선택 사항) 특정 사용자의 모든 루틴 조회 (모든 공간 포함)
     * 예시 백엔드 API 경로: GET /routines/user/{userId}
     */
    @GET("routines/user/{userId}")
    Call<List<CleaningRoutine>> getAllRoutinesByUserId(@Path("userId") int userId);

    /**
     * (선택 사항) 특정 루틴 상세 조회
     * 예시 백엔드 API 경로: GET /routines/{routine_id}
     */
    @GET("routines/{routine_id}")
    Call<CleaningRoutine> getRoutineById(@Path("routine_id") int routineId);

    /**
     * (선택 사항) 오늘 할 일 루틴 조회
     * 예시 백엔드 API 경로: GET /routines/today?user_id={userId}
     */
    @GET("routines/today") // 경로가 "/routines/today"인지, 아니면 "today"인지 백엔드 확인 필요
    Call<List<CleaningRoutine>> getTodaysRoutines(@Query("user_id") int userId);

}