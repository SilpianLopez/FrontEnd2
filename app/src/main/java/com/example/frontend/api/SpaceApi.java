package com.example.frontend.api;

import com.example.frontend.models.Space;
import com.example.frontend.models.SpaceRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface SpaceApi {
    @GET("spaces/users/{userId}/spaces")
    Call<List<Space>> getSpacesByUser(@Path("userId") int userId);

    @POST("spaces")
    Call<Space> createSpace(@Body SpaceRequest space);
}
