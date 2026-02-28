package com.example.medisageapp;

import android.content.Intent;
import com.google.firebase.auth.FirebaseAuth;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportsActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private Button btnSelect, btnCamera;

    private RecyclerView recyclerView;
    private ReportsAdapter adapter;
    private List<ReportModel> reportList;   // ✅ changed
    private DatabaseReference databaseReference;

    private ActivityResultLauncher<Intent> filePickerLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        // 🔹 Cloudinary init
        try {
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", "dcmtq7ij5");
            MediaManager.init(this, config);
        } catch (IllegalStateException ignored) {}

        // 🔹 UI
        btnCamera = findViewById(R.id.btnCameraReport);
        btnSelect = findViewById(R.id.btnSelectReport);
        progressBar = findViewById(R.id.uploadProgress);
        recyclerView = findViewById(R.id.reportsRecyclerView);

        // 🔹 Firebase
        String uid = FirebaseAuth.getInstance()
                .getCurrentUser()
                .getUid();

        databaseReference = FirebaseDatabase.getInstance()
                .getReference("MedicalReports")
                .child(uid);

        reportList = new ArrayList<>();

        // 🔹 RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReportsAdapter(this, reportList, databaseReference);
        recyclerView.setAdapter(adapter);

        // 🔹 Fetch reports
        fetchReports();

        // 🔹 File picker launcher
        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri fileUri = result.getData().getData();
                        if (fileUri != null) uploadToCloudinary(fileUri);
                    }
                });

        // 🔹 Camera launcher
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.getExtras() != null) {
                            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                            if (bitmap != null) uploadBitmapToCloudinary(bitmap);
                        }
                    }
                });

        btnCamera.setOnClickListener(v -> openCamera());
        btnSelect.setOnClickListener(v -> openFilePicker());
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(intent);
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES,
                new String[]{"image/*", "application/pdf"});
        filePickerLauncher.launch(intent);
    }

    // 🔹 Fetch from Firebase
    private void fetchReports() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                reportList.clear();

                for (DataSnapshot data : snapshot.getChildren()) {
                    ReportModel report = data.getValue(ReportModel.class);
                    if (report != null) reportList.add(report);
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ReportsActivity.this,
                        "Failed to load reports", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 🔹 Upload file
    private void uploadToCloudinary(Uri fileUri) {
        progressBar.setProgress(0);
        progressBar.setVisibility(View.VISIBLE);


        MediaManager.get().upload(fileUri)
                .option("resource_type", "auto")
                .unsigned("medisage_preset")
                .callback(new UploadCallback() {

                    @Override public void onStart(String requestId) {}
                    @Override public void onProgress(String requestId, long bytes, long totalBytes) {}
                    @Override public void onReschedule(String requestId, ErrorInfo error) {}

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String secureUrl = (String) resultData.get("secure_url");
                        saveUrlToFirebase(secureUrl);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(ReportsActivity.this,
                                "Upload failed", Toast.LENGTH_SHORT).show();
                    }
                })
                .dispatch();
    }

    private void uploadBitmapToCloudinary(Bitmap bitmap) {
        progressBar.setVisibility(View.VISIBLE);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

        MediaManager.get().upload(baos.toByteArray())
                .unsigned("medisage_preset")
                .callback(new UploadCallback() {

                    @Override public void onStart(String requestId) {}
                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {

                        int progress = (int) ((bytes * 100) / totalBytes);

                        runOnUiThread(() -> {
                            progressBar.setProgress(progress);
                        });
                    }

                    @Override public void onReschedule(String requestId, ErrorInfo error) {}

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String secureUrl = (String) resultData.get("secure_url");
                        saveUrlToFirebase(secureUrl);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        progressBar.setVisibility(View.GONE);
                    }
                })
                .dispatch();
    }

    private void saveUrlToFirebase(String url) {
        ReportModel report = new ReportModel("Medical Report", url);

        databaseReference.push().setValue(report)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        Toast.makeText(this,
                                "Report uploaded successfully!", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
