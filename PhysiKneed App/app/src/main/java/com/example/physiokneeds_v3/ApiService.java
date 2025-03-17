package com.example.physiokneeds_v3;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ApiService {
    @GET("/api/routine-configs")
    Call<List<RoutineConfig>> getRoutine();

    @POST("/api/routine-data/")
    Call<RoutineDataUpload> sendData(@Body RoutineDataUpload data);
}
