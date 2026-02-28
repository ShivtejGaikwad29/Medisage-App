package com.example.medisageapp;

import java.util.ArrayList;
import java.util.List;

public class MedicineInfo {
    public String name;
    public String time;
    public String foodInstruction;
    public String dosePattern;

    public MedicineInfo(String name, String time, String foodInstruction, String dosePattern) {
        this.name = name;
        this.time = time;
        this.foodInstruction = foodInstruction;
        this.dosePattern = dosePattern;
    }

    public List<Integer> getReminderHours() {
        List<Integer> hours = new ArrayList<>();

        // Convert everything to lowercase for easier matching
        String combinedText = (dosePattern + " " + foodInstruction).toLowerCase();

        // 🌙 Check for Bedtime/Night
        if (combinedText.contains("bedtime") || combinedText.contains("night") || combinedText.contains("dinner")) {
            hours.add(21); // 9 PM
        }

        // 🌅 Check for Morning
        if (combinedText.contains("morning") || combinedText.contains("breakfast")) {
            hours.add(8);  // 8 AM
        }

        // ☀️ Check for Evening/Afternoon
        if (combinedText.contains("evening") || combinedText.contains("afternoon")) {
            hours.add(18); // 6 PM
        }

        // 🔢 Fallback to the 1-0-1 pattern if no words were found
        if (hours.isEmpty() && dosePattern.contains("-")) {
            String[] parts = dosePattern.split("-");
            if (parts.length >= 1 && parts[0].equals("1")) hours.add(8);
            if (parts.length >= 2 && parts[1].equals("1")) hours.add(14);
            if (parts.length >= 3 && parts[2].equals("1")) hours.add(21);
        }

        return hours;
    }
}