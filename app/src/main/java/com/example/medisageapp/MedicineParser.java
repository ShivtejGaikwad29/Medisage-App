package com.example.medisageapp;

import java.util.ArrayList;
import java.util.List;

public class MedicineParser {

    public static List<MedicineInfo> parse(String text) {

        List<MedicineInfo> medicines = new ArrayList<>();

        String[] lines = text.split("\n");

        for (String line : lines) {

            line = line.toLowerCase().trim();

            if (line.matches(".*(tab|cap|syrup).*"))
            {
                String name = extractMedicineName(line);
                String time = detectTime(line);
                String food = detectFoodInstruction(line);

                String dose = detectDosePattern(line);
                medicines.add(new MedicineInfo(name, time, food, dose));

            }
        }

        return medicines;
    }

    private static String extractMedicineName(String line) {

        String[] words = line.split(" ");

        for (int i = 0; i < words.length; i++) {

            String w = words[i].replaceAll("[^a-zA-Z]", "");

            // Detect Tab / Cap / Syrup keyword
            if (w.equals("tab") || w.equals("cap") || w.equals("syrup")) {

                // Next word is usually medicine name
                if (i + 1 < words.length) {
                    return words[i + 1].replaceAll("[^a-zA-Z]", "");
                }
            }
        }

        return "Unknown"; // instead of number
    }


    private static String detectTime(String line) {

        if (line.contains("morning")) return "Morning";
        if (line.contains("night")) return "Night";
        if (line.contains("afternoon")) return "Afternoon";

        return "---";
    }
    private static String detectDosePattern(String line) {

        if (line.contains("1-1-1")) return "1-1-1";
        if (line.contains("1-0-1")) return "1-0-1";
        if (line.contains("1-0-0")) return "1-0-0";
        if (line.contains("0-1-0")) return "0-1-0";
        if (line.contains("0-0-1")) return "0-0-1";
        if (line.contains("sos")) return "SOS";

        return "---";
    }


    private static String detectFoodInstruction(String line) {

        if (line.contains("before")) return "Before Meal";
        if (line.contains("after")) return "After Meal";

        return "---";
    }

}
