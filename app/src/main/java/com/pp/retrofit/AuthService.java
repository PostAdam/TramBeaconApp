package com.pp.retrofit;

import org.json.JSONObject;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthService {

    @POST("users/login")
    Call<String> login(@Body RequestBody params);
    @POST("users/register")
    Call<Void> register(@Body RequestBody params);
}
