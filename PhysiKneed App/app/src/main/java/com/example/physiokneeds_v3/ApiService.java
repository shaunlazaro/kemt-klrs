package com.example.physiokneeds_v3;

import com.google.android.gms.common.internal.safeparcel.SafeParcelable;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @GET("/api/routine-configs")
    Call<List<RoutineConfig>> getRoutine(@Header("Authorization") String token);

    @GET("/api/routine-data/app")
    Call<List<RoutineData>> getRoutineData(@Header("Authorization") String token);

    @GET("/api/auth/google/android/")
    Call<Token> getTokenId(@Query("code")  String googleIdToken);

    @POST("/api/routine-data/")
    Call<RoutineDataUpload> sendData(@Header("Authorization") String token, @Body RoutineDataUpload data);
}
