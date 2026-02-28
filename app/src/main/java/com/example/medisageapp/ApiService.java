package com.example.medisageapp;

import java.util.Map;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    // This must match the @app.route('/predict') in your Python code
    @POST("/predict")
    Call<PredictionResponse> getPrediction(@Body Map<String, Object> body);
}