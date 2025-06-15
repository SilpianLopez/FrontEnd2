package com.example.frontend2.api;

import com.example.frontend2.models.MonthlyLogStat;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface CleaningLogApi {
    @GET("logs/stats/monthly/{userId}")
    Call<List<MonthlyLogStat>> getMonthlyLogs(@Path("userId") int userId);
}
