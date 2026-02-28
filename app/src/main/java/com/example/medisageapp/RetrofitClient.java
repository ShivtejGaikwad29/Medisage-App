package com.example.medisageapp;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static Retrofit retrofit;

    // REPLACE THIS with your Computer's IP Address
    // Use http://10.0.2.2:5000/ if you are using the Android Emulator
    // WRONG: private static final String BASE_URL = "http:// 10.83.149.38/";
// CORRECT (assuming your Flask port is 5000):
    private static final String BASE_URL = "https://medisage26.pythonanywhere.com/";
    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}