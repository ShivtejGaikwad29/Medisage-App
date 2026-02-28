package com.example.medisageapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class ReportsAdapter extends RecyclerView.Adapter<ReportsAdapter.ReportViewHolder> {

    private Context context;
    private List<ReportModel> reportList;
    private DatabaseReference databaseReference;

    public ReportsAdapter(Context context, List<ReportModel> reportList, DatabaseReference databaseReference) {
        this.context = context;
        this.reportList = reportList;
        this.databaseReference = databaseReference;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_report, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {

        ReportModel report = reportList.get(position);
        String url = report.url;
        String name = report.name;

        // 📄 Show PDF icon OR image preview
        if (url.toLowerCase().endsWith(".pdf")) {
            holder.ivReportImage.setImageResource(R.drawable.pdf_logo);
        } else {
            Glide.with(context)
                    .load(url)
                    .placeholder(android.R.drawable.progress_horizontal)
                    .error(android.R.drawable.stat_notify_error)
                    .into(holder.ivReportImage);
        }

        holder.tvTitle.setText(name);

        // 📂 Open file on click
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        });

        // 📌 Long-press menu (Rename / Delete)
        holder.itemView.setOnLongClickListener(v -> {

            PopupMenu popup = new PopupMenu(context, holder.itemView);
            popup.getMenu().add("Rename");
            popup.getMenu().add("Delete");

            popup.setOnMenuItemClickListener(item -> {

                if (item.getTitle().equals("Delete")) {
                    deleteReport(report);
                } else if (item.getTitle().equals("Rename")) {
                    renameReport(report);
                }

                return true;
            });

            popup.show();
            return true;
        });
    }

    // 🗑 Delete report from Firebase
    private void deleteReport(ReportModel report) {

        databaseReference.orderByChild("url").equalTo(report.url)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot data : snapshot.getChildren()) {
                            data.getRef().removeValue();
                        }
                        Toast.makeText(context, "Report deleted", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(context, "Delete failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ✏ Rename report in Firebase
    private void renameReport(ReportModel report) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Rename Report");

        EditText input = new EditText(context);
        input.setText(report.name);
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {

            String newName = input.getText().toString().trim();

            if (newName.isEmpty()) {
                Toast.makeText(context, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            databaseReference.orderByChild("url").equalTo(report.url)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot data : snapshot.getChildren()) {
                                data.getRef().child("name").setValue(newName);
                            }
                            Toast.makeText(context, "Renamed successfully", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(context, "Rename failed", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

    static class ReportViewHolder extends RecyclerView.ViewHolder {

        ImageView ivReportImage;
        TextView tvTitle;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            ivReportImage = itemView.findViewById(R.id.ivReportImage);
            tvTitle = itemView.findViewById(R.id.tvReportTimestamp);
        }
    }
}
