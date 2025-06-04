package com.example.frontend2.api;

import com.example.frontend2.models.LoginRequest;
import com.example.frontend2.models.RegisterRequest;
import com.example.frontend2.models.User;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface UserApi {

    @POST("users/login")
    Call<User> login(@Body LoginRequest request);

    @POST("users")
    Call<User> register(@Body RegisterRequest request); // ✅ 여기 수정!
    // 🔹 사용자 정보 조회
    @GET("users/{id}")
    Call<User> getUserById(@Path("id") int userId);

    // 🔹 사용자 정보 수정
    @PUT("users/{id}")
    Call<User> updateUser(@Path("id") int userId, @Body User user);
}
