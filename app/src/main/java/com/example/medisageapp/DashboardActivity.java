package com.example.medisageapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

import pl.droidsonroids.gif.GifImageView;

public class DashboardActivity extends AppCompatActivity {

    private View qaPredict, qaReport, qaReminder, qaLifestyle;
    private MaterialButton btnLogout;
    private GifImageView gifChatbot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Initialize views
        qaPredict = findViewById(R.id.qaPredict);
        qaReport = findViewById(R.id.qaReport);
        qaReminder = findViewById(R.id.qaReminder);
        qaLifestyle = findViewById(R.id.qaLifestyle);
        btnLogout = findViewById(R.id.btnLogout);
        gifChatbot = findViewById(R.id.gifChatbot);

        // Navigation
        qaPredict.setOnClickListener(v ->
                startActivity(new Intent(this, PredictActivity.class)));

        qaReport.setOnClickListener(v ->
                startActivity(new Intent(this, ReportsActivity.class)));

        qaReminder.setOnClickListener(v ->
                startActivity(new Intent(this, ReminderActivity.class)));

        qaLifestyle.setOnClickListener(v ->
                startActivity(new Intent(this, LifestyleActivity.class)));

        gifChatbot.setOnClickListener(v ->
                startActivity(new Intent(this, ChatActivity.class)));

        // Logout
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}
