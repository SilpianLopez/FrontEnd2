package com.example.frontend2.api;

import com.example.frontend2.models.LoginRequest;
import com.example.frontend2.models.RegisterRequest;
import com.example.frontend2.models.User;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface UserApi {

    @POST("users/login")
    Call<User> login(@Body LoginRequest request);

    @POST("users")
    Call<User> register(@Body RegisterRequest request); // ✅ 여기 수정!
}
