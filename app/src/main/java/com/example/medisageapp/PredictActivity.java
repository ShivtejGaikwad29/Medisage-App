package com.example.medisageapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.util.HashMap;
import java.util.Map;
import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Color;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PredictActivity extends AppCompatActivity {

    // Added missing fields: etMale, etEdu, etSmoker, etBPMeds, etStroke, etHyp, etDiabetes
    private EditText etAge, etCigs, etChol, etSysBP, etDiaBP, etBMI, etHeartRate, etGlucose;
    private EditText etMale, etEdu, etSmoker, etBPMeds, etStroke, etHyp, etDiabetes;
    private Button btnPredict;
    private TextView tvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_predict);

        // Initialize all 15 views
        etMale = findViewById(R.id.etMale); // 1 for Male, 0 for Female
        etAge = findViewById(R.id.etAge);
        etEdu = findViewById(R.id.etEducation); // 1-4
        etSmoker = findViewById(R.id.etSmoker); // 1 for Yes, 0 for No
        etCigs = findViewById(R.id.etCigs);
        etBPMeds = findViewById(R.id.etBPMeds);
        etStroke = findViewById(R.id.etStroke);
        etHyp = findViewById(R.id.etHyp);
        etDiabetes = findViewById(R.id.etDiabetes);
        etChol = findViewById(R.id.etChol);
        etSysBP = findViewById(R.id.etSysBP);
        etDiaBP = findViewById(R.id.etDiaBP);
        etBMI = findViewById(R.id.etBMI);
        etHeartRate = findViewById(R.id.etHeartRate);
        etGlucose = findViewById(R.id.etGlucose);

        btnPredict = findViewById(R.id.btnPredictRisk);
        tvResult = findViewById(R.id.tvResult);

        btnPredict.setOnClickListener(v -> performPrediction());
    }

    private void performPrediction() {
        try {
            // 1. Capture all 15 inputs from the user
            float male = Float.parseFloat(etMale.getText().toString());
            float age = Float.parseFloat(etAge.getText().toString());
            float education = Float.parseFloat(etEdu.getText().toString());
            float currentSmoker = Float.parseFloat(etSmoker.getText().toString());
            float cigs = Float.parseFloat(etCigs.getText().toString());
            float bpMeds = Float.parseFloat(etBPMeds.getText().toString());
            float stroke = Float.parseFloat(etStroke.getText().toString());
            float hyp = Float.parseFloat(etHyp.getText().toString());
            float diabetes = Float.parseFloat(etDiabetes.getText().toString());
            float chol = Float.parseFloat(etChol.getText().toString());
            float sysBP = Float.parseFloat(etSysBP.getText().toString());
            float diaBP = Float.parseFloat(etDiaBP.getText().toString());
            float bmi = Float.parseFloat(etBMI.getText().toString());
            float heartRate = Float.parseFloat(etHeartRate.getText().toString());
            float glucose = Float.parseFloat(etGlucose.getText().toString());

            // 2. Exact Framingham Order for your Python Scaler
            float[] inputs = {
                    male, age, education, currentSmoker, cigs,
                    bpMeds, stroke, hyp, diabetes, chol,
                    sysBP, diaBP, bmi, heartRate, glucose
            };

            Map<String, Object> body = new HashMap<>();
            body.put("features", inputs);

            tvResult.setText("Analyzing on Server...");
            tvResult.setTextColor(Color.BLACK);

            ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
            Call<PredictionResponse> call = apiService.getPrediction(body);

            call.enqueue(new Callback<PredictionResponse>() {
                @Override
                public void onResponse(Call<PredictionResponse> call, Response<PredictionResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {

                        int result = response.body().getPrediction();

                        // Show result text
                        if (result == 1) {
                            tvResult.setText("Result: High Risk of Heart Disease");
                            tvResult.setTextColor(Color.RED);
                        } else {
                            tvResult.setText("Result: Low Risk of Heart Disease");
                            tvResult.setTextColor(Color.GREEN);
                        }

                        // 👉 Open LifestyleActivity for BOTH cases
                        Intent intent = new Intent(PredictActivity.this, LifestyleActivity.class);
                        intent.putExtra("prediction", result);
                        intent.putExtra("bmi", bmi);
                        intent.putExtra("smoker", currentSmoker);
                        intent.putExtra("sysBP", sysBP);
                        startActivity(intent);

                    } else {
                        tvResult.setText("Server Error: " + response.code());
                    }
                }


                @Override
                public void onFailure(Call<PredictionResponse> call, Throwable t) {
                    tvResult.setText("Connection Error: Check PythonAnywhere status");
                    tvResult.setTextColor(Color.GRAY);
                }
            });

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please fill all 15 fields", Toast.LENGTH_SHORT).show();
        }
    }
}