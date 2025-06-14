package com.example.frontend2.api;

import com.example.frontend2.models.StatsResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import java.util.List;

public interface StatsApi {
    @GET("/api/statistics/space-cleaning-counts")
    Call<List<StatsResponse>> getSpaceCleaningCounts();
}
