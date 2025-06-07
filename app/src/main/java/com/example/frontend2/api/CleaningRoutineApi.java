package com.example.frontend2.api;

import com.example.frontend2.CleaningList;
import com.example.frontend2.models.RoutineRequest;
import com.example.frontend2.models.CleaningRoutine;

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


    @POST("routines")
    Call<CleaningList> createRoutine(@Body RoutineRequest request);


    // ✅ 공간 ID를 기준으로 해당 공간의 루틴 목록 가져오기
    @GET("routines/space/{spaceId}")
    Call<List<CleaningList>> getRoutinesBySpace(@Path("spaceId") int spaceId);


        @GET("/routines/today")
        Call<List<CleaningRoutine>> getTodaysRoutines(@Query("user_id") int userId);

    @DELETE("/routines/{routine_id}")
    Call<Void> deleteRoutine(@Path("routine_id") int routineId);

    @PUT("routines/{routine_id}")
    Call<CleaningRoutine> updateRoutine(
            @Path("routine_id") int routineId,
            @Body RoutineRequest request
    );


}
