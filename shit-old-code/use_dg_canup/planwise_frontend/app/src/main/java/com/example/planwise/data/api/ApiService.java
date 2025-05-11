package com.example.planwise.data.api;

import com.example.planwise.data.model.Schedule;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {
    @GET("api/schedules/")
    Call<List<Schedule>> getAllSchedules();

    @POST("api/sync/")
    Call<List<Schedule>> syncSchedules(@Body List<Schedule> schedules);
}