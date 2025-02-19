package com.example.physiokneeds_v3;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ApiService {
    @GET("/api/poses/{id}")
    Call<Pose> getRoutine(@Path("id") Integer id);
}
