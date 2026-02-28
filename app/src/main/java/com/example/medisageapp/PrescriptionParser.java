package com.example.medisageapp;

import java.util.ArrayList;
import java.util.List;

public class PrescriptionParser {

    public static class MedicineInfo {
        public String name;
        public String dosagePattern; // like 1-0-1
        public String foodInstruction;
    }

    public static List<MedicineInfo> parse(String text) {

        List<MedicineInfo> medicines = new ArrayList<>();

        String[] lines = text.split("\n");

        for (String line : lines) {

            line = line.toLowerCase();

            // detect dosage pattern like 1-0-1 or 0-1-0
            if (line.matches(".*\\d-\\d-\\d.*")) {

                MedicineInfo info = new MedicineInfo();

                // simple medicine name extraction (first word)
                String[] words = line.split(" ");
                info.name = words[0];

                info.dosagePattern = line.replaceAll(".*?(\\d-\\d-\\d).*", "$1");

                medicines.add(info);
            }
        }

        return medicines;
    }
}
