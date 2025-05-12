// com/example/planwise/data/api/ApiService.java
package com.example.planwise.data.api;

import com.example.planwise.data.model.Schedule;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Headers;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {
    // 在ApiService中确保正确指定Content-Type
    @Headers("Content-Type: application/json")
    @POST("api/sync-to-cloud/")
    Call<List<Schedule>> uploadAllSchedules(@Body List<Schedule> schedules);

    @GET("api/sync-from-cloud/")
    Call<List<Schedule>> getAllSchedules();

    // 添加新的AI建议API方法
    @Headers("Content-Type: application/json")
    @POST("api/ai-suggestion/")
    Call<Map<String, String>> getAiSuggestion(@Body Map<String, Object> scheduleInfo);
}