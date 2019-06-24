package com.pp.retrofit;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface SensorsReadingService {
    @POST("sensorreadings/multiple-new")
    Call<Void> postMultipleReadings(@Body RequestBody params, @Header("Authorization") String token);
    @POST("sensorreadings/amiintram")
    Call<Object> amIInTram(@Body RequestBody params, @Header("Authorization") String token);
}
