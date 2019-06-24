package com.pp.retrofit;

import com.pp.model.BeaconToken;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface BeaconTokenService {
    @GET("beacontoken")
    Call<List<BeaconToken>> getAllBeaconTokens(@Header("Authorization") String token);
}
