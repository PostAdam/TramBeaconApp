package com.pp.retrofit;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface TramTravelService {
    @POST("trips")
    Call<Void> startTravel(@Body RequestBody params, @Header("Authorization") String token);
    @POST("trips/finish")
    Call<Object> finishTravel(@Body RequestBody params, @Header("Authorization") String token);
}
