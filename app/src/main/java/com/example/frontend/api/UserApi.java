package com.example.frontend.api;

import com.example.frontend.models.LoginRequest;
import com.example.frontend.models.RegisterRequest;
import com.example.frontend.models.User;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface UserApi {

    @POST("users/login")
    Call<User> login(@Body LoginRequest request);

    @POST("users")
    Call<User> register(@Body RegisterRequest request); // ✅ 여기 수정!
}
