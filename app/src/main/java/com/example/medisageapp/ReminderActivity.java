package com.example.medisageapp;

import android.app.AlarmManager;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.OutputStream;
import java.util.Scanner;
import org.json.JSONArray;
import org.json.JSONObject;
import android.util.Log;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReminderActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 101;
    private Button btnScan;
    private TextView tvExtractedData;
    private String currentPhotoPath; // To store the high-res file path

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder);

        btnScan = findViewById(R.id.btnScanPrescription);
        tvExtractedData = findViewById(R.id.tvExtractedData);

        createNotificationChannel();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
        }

        btnScan.setOnClickListener(v -> {
            if (checkSelfPermission(android.Manifest.permission.CAMERA) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.CAMERA}, 100);
            } else {
                dispatchTakePictureIntent();
            }
        });
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            Toast.makeText(this, "Error creating file", Toast.LENGTH_SHORT).show();
        }

        if (photoFile != null) {
            Uri photoURI = FileProvider.getUriForFile(this,
                    "com.example.medisageapp.fileprovider",
                    photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI); // Force HD!
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // 1. Load the original Bitmap
            Bitmap fullSizeBitmap = BitmapFactory.decodeFile(currentPhotoPath);

            // 2. ⚡ COMPRESS: Scale down to a max width of 1024px
            int width = fullSizeBitmap.getWidth();
            int height = fullSizeBitmap.getHeight();
            float ratio = (float) width / (float) height;

            int newWidth = 1024;
            int newHeight = (int) (newWidth / ratio);

            Bitmap scaledBitmap = Bitmap.createScaledBitmap(fullSizeBitmap, newWidth, newHeight, true);

            // 3. Process the smaller, faster image
            processPrescription(scaledBitmap);
        }
    }

    private void processPrescription(Bitmap bitmap) {
        InputImage image = InputImage.fromBitmap(bitmap, 0);
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        tvExtractedData.setText("AI is analyzing your prescription...");

        recognizer.process(image)
                .addOnSuccessListener(visionText -> {
                    String rawText = visionText.getText();
                    // NEW: Instead of basic parser, we use AI
                    extractMedsWithAI(rawText);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "OCR Failed", Toast.LENGTH_SHORT).show());
    }
    private void setAlarmsFromPattern(MedicineInfo med) {

        // If explicit time exists → use that
        if ("Morning".equals(med.time)) setMedicineAlarm(8, 0, med.name);
        if ("Afternoon".equals(med.time)) setMedicineAlarm(14, 0, med.name);
        if ("Night".equals(med.time)) setMedicineAlarm(20, 0, med.name);

        // If time NOT mentioned → use dose pattern
        if ("---".equals(med.time)) {

            switch (med.dosePattern) {

                case "1-0-0":
                    setMedicineAlarm(8, 0, med.name);
                    break;

                case "0-1-0":
                    setMedicineAlarm(14, 0, med.name);
                    break;

                case "0-0-1":
                    setMedicineAlarm(20, 0, med.name);
                    break;

                case "1-0-1":
                    setMedicineAlarm(8, 0, med.name);
                    setMedicineAlarm(20, 0, med.name);
                    break;

                case "1-1-1":
                    setMedicineAlarm(8, 0, med.name);
                    setMedicineAlarm(14, 0, med.name);
                    setMedicineAlarm(20, 0, med.name);
                    break;

                case "SOS":
                    // No automatic alarm
                    break;
            }
        }
    }



    private void setAlarmsFromPattern(PrescriptionParser.MedicineInfo med) {

        if (med == null || med.dosagePattern == null) return;

        String[] parts = med.dosagePattern.split("-");
        if (parts.length != 3) return;

        // Default meal times
        int breakfast = 8;
        int lunch = 14;
        int dinner = 21;

        int offset = 0;

        // ⭐ SAFE NULL CHECK
        String food = med.foodInstruction != null ? med.foodInstruction : "----";

        if (food.equalsIgnoreCase("Before Meal")) offset = -30;
        else if (food.equalsIgnoreCase("After Meal")) offset = +30;

        // Morning
        if (parts[0].equals("1"))
            scheduleAdjustedAlarm(breakfast, offset, med.name + " (Morning)");

        // Afternoon
        if (parts[1].equals("1"))
            scheduleAdjustedAlarm(lunch, offset, med.name + " (Afternoon)");

        // Night
        if (parts[2].equals("1"))
            scheduleAdjustedAlarm(dinner, offset, med.name + " (Night)");
    }


    private void scheduleAdjustedAlarm(int baseHour, int minuteOffset, String medName) {

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, baseHour);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);

        // apply before/after meal offset
        cal.add(Calendar.MINUTE, minuteOffset);

        setExactAlarm(cal, medName);
    }

    private void setExactAlarm(Calendar calendar, String medicineName) {

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(this, AlarmReceiveer.class);
        intent.putExtra("MED_NAME", medicineName);

        int requestCode = (int) System.currentTimeMillis();

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // If time already passed → schedule tomorrow
        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DATE, 1);
        }

        if (alarmManager != null) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                    !alarmManager.canScheduleExactAlarms()) {

                startActivity(new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM));

            } else {

                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        pendingIntent
                );

                Toast.makeText(this,
                        "Alarm set for " + medicineName + " at "
                                + calendar.get(Calendar.HOUR_OF_DAY) + ":"
                                + String.format("%02d", calendar.get(Calendar.MINUTE)),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Change the signature to accept 3 arguments
    private void setMedicineAlarm(int hour, int minute, String medicineName) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(this, AlarmReceiveer.class);
        // Put the medicine name into the intent so the notification can show it
        intent.putExtra("MED_NAME", medicineName);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,
                hour + minute, // Use a unique ID for each alarm type (morning vs night)
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        // If the time has already passed today, set it for tomorrow
        if (calendar.before(Calendar.getInstance())) {
            calendar.add(Calendar.DATE, 1);
        }

        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                startActivity(new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM));
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(), pendingIntent);
                Toast.makeText(this, "Reminder set for " + medicineName + " at " + hour + ":" + minute, Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void extractMedsWithAI(String rawOcrText) {
        new Thread(() -> {
            try {
                // 1. Setup Connection to Hugging Face Router
                URL url = new URL("https://router.huggingface.co/v1/chat/completions");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");

                // Your Active Token
//                place your hugging face token
                conn.setRequestProperty("Authorization", "Bearer " + HF_TOKEN.trim());
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                // 2. The Optimized Prompt for Full Extraction
                String prompt = "Act as a medical data extractor. Extract EVERY medicine, tablet, lotion, and tincture from this text. " +
                        "Return ONLY a raw JSON array of objects with these keys: " +
                        "'name' (full name), 'dose' (use 1-0-1 format if possible), and 'instruction' (e.g., bedtime, after dinner). " +
                        "Text: " + rawOcrText;

                // 3. Build the JSON Request
                JSONObject json = new JSONObject();
                json.put("model", "Qwen/Qwen2.5-7B-Instruct");
                json.put("max_tokens", 1000); // Increased to prevent cutting off long prescriptions

                JSONArray messages = new JSONArray();
                messages.put(new JSONObject().put("role", "user").put("content", prompt));
                json.put("messages", messages);

                // 4. Send the Request
                OutputStream os = conn.getOutputStream();
                os.write(json.toString().getBytes());
                os.close();

                // 5. Handle the Response
                if (conn.getResponseCode() == 200) {
                    String response = new java.util.Scanner(conn.getInputStream()).useDelimiter("\\A").next();
                    JSONObject responseJson = new JSONObject(response);
                    String aiContent = responseJson.getJSONArray("choices")
                            .getJSONObject(0).getJSONObject("message").getString("content");

                    // 🧹 CLEANING: Remove Markdown backticks (```json ... ```)
                    String cleanedJson = aiContent.replaceAll("(?s)```json|```", "").trim();

                    JSONArray medsArray = new JSONArray(cleanedJson);

                    runOnUiThread(() -> {
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < medsArray.length(); i++) {
                            JSONObject obj = medsArray.optJSONObject(i);
                            if (obj == null) continue;

                            // Create MedicineInfo with AI data
                            MedicineInfo med = new MedicineInfo(
                                    obj.optString("name", "Unknown"),
                                    "---",
                                    obj.optString("instruction", "No instructions"),
                                    obj.optString("dose", "0-0-0")
                            );

                            // Trigger the alarm scheduling
                            scheduleAlarmsForMed(med);

                            // Update the UI with Emojis for a professional look
                            sb.append("💊 ").append(med.name).append("\n")
                                    .append("📋 ").append(med.foodInstruction).append("\n\n");
                        }
                        tvExtractedData.setText(sb.toString());
                    });
                } else {
                    // Error handling for debugging
                    String error = new java.util.Scanner(conn.getErrorStream()).useDelimiter("\\A").next();
                    Log.e("HF_DEBUG", "AI Error: " + error);
                    runOnUiThread(() -> tvExtractedData.setText("AI Connection Error. Check Logs."));
                }
            } catch (Exception e) {
                Log.e("HF_DEBUG", "Exception: " + e.getMessage());
                runOnUiThread(() -> tvExtractedData.setText("System Error: " + e.getMessage()));
            }
        }).start();
    }

    private void scheduleAlarmsForMed(MedicineInfo med) {
        List<Integer> hours = med.getReminderHours();
        int minuteOffset = 0;

        // ✅ Add a null check to prevent crashes if 'foodInstruction' is empty
        if (med.foodInstruction != null) {
            if (med.foodInstruction.toLowerCase().contains("before")) {
                minuteOffset = -30;
            } else if (med.foodInstruction.toLowerCase().contains("after")) {
                minuteOffset = 30;
            }
        }

        for (int hour : hours) {
            setMedicineAlarm(hour, minuteOffset, med.name);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("medisage_reminder", "Medicine Alerts", NotificationManager.IMPORTANCE_HIGH);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }
}