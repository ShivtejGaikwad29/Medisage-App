package com.example.medisageapp;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class LifestyleActivity extends AppCompatActivity {

    private TextView tvRiskLevel, tvDiet, tvExercise, tvSleep, tvWarning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lifestyle);

        tvRiskLevel = findViewById(R.id.tvRiskLevel);
        tvDiet = findViewById(R.id.tvDiet);
        tvExercise = findViewById(R.id.tvExercise);
        tvSleep = findViewById(R.id.tvSleep);
        tvWarning = findViewById(R.id.tvWarning);

        // Receive data from PredictActivity
        int prediction = getIntent().getIntExtra("prediction", -1);
        float bmi = getIntent().getFloatExtra("bmi", -1);
        float smoker = getIntent().getFloatExtra("smoker", -1);
        float sysBP = getIntent().getFloatExtra("sysBP", -1);

        // 🔹 If opened directly from Dashboard → show general lifestyle
        if (prediction == -1) {
            showGeneralLifestyle();
        } else {
            generateLifestyle(prediction, bmi, smoker, sysBP);
        }
    }

    // =========================================================
    // 🔹 General healthy lifestyle (no prediction done)
    // =========================================================
    private void showGeneralLifestyle() {

        tvRiskLevel.setText("Healthy Lifestyle & Heart Care Guide");
        tvRiskLevel.setTextColor(Color.parseColor("#0F9D8A"));

        tvDiet.setText(
                "🍎 DIET & NUTRITION\n\n" +
                        "• Eat a balanced diet rich in fruits, vegetables, whole grains, and legumes.\n" +
                        "• Prefer healthy fats such as nuts, seeds, olive oil, and fish.\n" +
                        "• Limit salt intake to control blood pressure.\n" +
                        "• Reduce sugar, soft drinks, and processed or fried foods.\n" +
                        "• Drink enough water throughout the day (2–3 liters unless restricted)."
        );

        tvExercise.setText(
                "🏃 PHYSICAL ACTIVITY\n\n" +
                        "• Perform at least 30 minutes of moderate exercise daily.\n" +
                        "• Aim for 150 minutes of activity per week (walking, cycling, swimming).\n" +
                        "• Include stretching, yoga, or breathing exercises for flexibility and stress relief.\n" +
                        "• Avoid long sitting periods—take short walking breaks every hour."
        );

        tvSleep.setText(
                "😴 SLEEP & MENTAL WELL-BEING\n\n" +
                        "• Maintain 7–8 hours of quality sleep every night.\n" +
                        "• Follow a fixed sleep schedule (sleep and wake at the same time).\n" +
                        "• Reduce screen time before bed and keep the room calm and dark.\n" +
                        "• Manage stress using meditation, deep breathing, or relaxation techniques.\n" +
                        "• Stay socially connected with family and friends for emotional health."
        );

        tvWarning.setText(
                "⚠ PREVENTION & REGULAR CHECKUPS\n\n" +
                        "• Avoid smoking, tobacco, and excessive alcohol consumption.\n" +
                        "• Monitor blood pressure, blood sugar, and cholesterol regularly.\n" +
                        "• Maintain a healthy body weight and waist size.\n" +
                        "• Schedule routine health checkups every 6–12 months.\n" +
                        "• Consult a doctor immediately if you feel chest pain, breathlessness, or unusual fatigue."
        );
    }


    // =========================================================
    // 🔹 Personalized lifestyle based on prediction
    // =========================================================
    private void generateLifestyle(int prediction, float bmi, float smoker, float sysBP) {

        // Risk label
        if (prediction == 1) {
            tvRiskLevel.setText("High Heart Disease Risk");
            tvRiskLevel.setTextColor(Color.RED);
        } else {
            tvRiskLevel.setText("Low Heart Disease Risk");
            tvRiskLevel.setTextColor(Color.parseColor("#16A34A"));
        }

        // Diet advice
        if (bmi > 30) {
            tvDiet.setText(
                    "• Follow low-calorie and low-fat diet.\n" +
                            "• Increase fruits, vegetables, and fiber intake.\n" +
                            "• Avoid fried and processed foods."
            );
        } else {
            tvDiet.setText(
                    "• Maintain balanced diet with whole grains and healthy fats.\n" +
                            "• Keep sugar and salt intake moderate."
            );
        }

        // Exercise advice
        if (prediction == 1) {
            tvExercise.setText(
                    "• Do 30 minutes brisk walking daily.\n" +
                            "• Add light cardio and breathing exercises."
            );
        } else {
            tvExercise.setText(
                    "• Maintain regular physical activity (150 min/week).\n" +
                            "• Include stretching or yoga."
            );
        }

        // Sleep advice (same for all)
        tvSleep.setText(
                "• Ensure 7–8 hours of quality sleep daily.\n" +
                        "• Keep a fixed sleep schedule."
        );

        // Warning / prevention
        if (smoker == 1 || sysBP > 140) {
            tvWarning.setText(
                    "⚠ Quit smoking and control blood pressure.\n" +
                            "Consult a doctor for proper evaluation."
            );
            tvWarning.setTextColor(Color.RED);
        } else {
            tvWarning.setText(
                    "• Keep regular health checkups every 6 months.\n" +
                            "• Continue maintaining healthy lifestyle."
            );
            tvWarning.setTextColor(Color.parseColor("#B00020"));
        }
    }
}
