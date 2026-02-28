package com.example.medisageapp;

public class PredictionResponse {
    // This variable name must match the key used in your Python Flask 'jsonify' response
    private int prediction;

    // Getter method to retrieve the value in your Activity
    public int getPrediction() {
        return prediction;
    }

    public void setPrediction(int prediction) {
        this.prediction = prediction;
    }
}