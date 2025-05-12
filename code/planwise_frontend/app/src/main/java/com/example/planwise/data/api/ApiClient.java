// com/example/planwise/data/api/ApiClient.java
package com.example.planwise.data.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String BASE_URL = "http://192.168.1.2:8000/"; // 替换为你电脑的局域网IP
    private static Retrofit retrofit = null;
    private static ApiService apiService = null;

    public static ApiService getApiService() {
        if (apiService == null) {
            // 创建 OkHttpClient 并设置超时时间
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)  // 连接超时时间
                    .readTimeout(60, TimeUnit.SECONDS)     // 读取超时时间
                    .writeTimeout(60, TimeUnit.SECONDS)    // 写入超时时间
                    .build();

            Gson gson = new GsonBuilder()
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                    .create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)  // 设置客户端
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();

            apiService = retrofit.create(ApiService.class);
        }
        return apiService;
    }

    public static void setBaseUrl(String baseUrl) {
        if (!baseUrl.endsWith("/")) {
            baseUrl = baseUrl + "/";
        }

        // 创建 OkHttpClient 并设置超时时间
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)  // 连接超时时间
                .readTimeout(60, TimeUnit.SECONDS)     // 读取超时时间
                .writeTimeout(60, TimeUnit.SECONDS)    // 写入超时时间
                .build();

        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
                .create();

        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)  // 设置客户端
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        apiService = retrofit.create(ApiService.class);
    }
}