package com.example.frontend.api;

import com.example.frontend.CleaningList;
import com.example.frontend.models.RoutineRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface CleaningRoutineApi {


    @POST("routines")
    Call<CleaningList> createRoutine(@Body RoutineRequest request);


    // ✅ 공간 ID를 기준으로 해당 공간의 루틴 목록 가져오기
    @GET("routines/space/{spaceId}")
    Call<List<CleaningList>> getRoutinesBySpace(@Path("spaceId") int spaceId);

}
