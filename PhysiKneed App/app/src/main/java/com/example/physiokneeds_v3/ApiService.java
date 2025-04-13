package com.example.physiokneeds_v3;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @GET("/api/routine-configs")
    Call<List<RoutineConfig>> getRoutine();

    @GET("/api/routine-data")
    Call<List<RoutineData>> getRoutineData(@Query("limit") int limit);

    @POST("/api/routine-data/")
    Call<RoutineDataUpload> sendData(@Body RoutineDataUpload data);
}
