package com.healthtwin.ai.report;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.healthtwin.ai.R;

import java.util.ArrayList;

public class HealthReportsActivity extends AppCompatActivity {

    private RecyclerView reportsRecyclerView;
    private ProgressBar progressBar;
    private TextView tvNoReports;
    private Button btnAddReport;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private ArrayList<ReportModel> reportList;
    private ReportAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_health_reports);

        // -----------------------------
        // Find Views
        // -----------------------------

        reportsRecyclerView =
                findViewById(R.id.reportsRecyclerView);

        progressBar =
                findViewById(R.id.progressBar);

        tvNoReports =
                findViewById(R.id.tvNoReports);

        btnAddReport =
                findViewById(R.id.btnAddReport);

        // -----------------------------
        // Firebase
        // -----------------------------

        auth = FirebaseAuth.getInstance();

        db = FirebaseFirestore.getInstance();

        // -----------------------------
        // Report List
        // -----------------------------

        reportList = new ArrayList<>();

        adapter = new ReportAdapter(
                this,
                reportList
        );

        reportsRecyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );

        reportsRecyclerView.setAdapter(adapter);

        // -----------------------------
        // Add Report Button
        // -----------------------------

        btnAddReport.setOnClickListener(v -> {

            Intent intent = new Intent(
                    HealthReportsActivity.this,
                    AddReportActivity.class
            );

            startActivity(intent);
        });

        // -----------------------------
        // Load Reports
        // -----------------------------

        loadReports();
    }

    // ============================================================
    // RELOAD WHEN ACTIVITY OPENS AGAIN
    // ============================================================

    @Override
    protected void onResume() {
        super.onResume();

        if (auth != null) {
            loadReports();
        }
    }

    // ============================================================
    // LOAD REPORTS FROM FIRESTORE
    // ============================================================

    private void loadReports() {

        // Check Login
        if (auth.getCurrentUser() == null) {

            Toast.makeText(
                    this,
                    "Please login first",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String userId =
                auth.getCurrentUser().getUid();

        // Show Progress
        progressBar.setVisibility(
                View.VISIBLE
        );

        tvNoReports.setVisibility(
                View.GONE
        );

        // Firestore Path:
        // users/{uid}/reports

        db.collection("users")
                .document(userId)
                .collection("reports")
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    // Clear old data
                    reportList.clear();

                    // Read every report
                    for (DocumentSnapshot document :
                            querySnapshot.getDocuments()) {

                        ReportModel report =
                                new ReportModel();

                        // Document ID
                        report.setId(
                                document.getId()
                        );

                        // Report Name
                        report.setReportName(
                                getStringValue(
                                        document,
                                        "reportName"
                                )
                        );

                        // Report Date
                        report.setReportDate(
                                getStringValue(
                                        document,
                                        "reportDate"
                                )
                        );

                        // Doctor Name
                        report.setDoctorName(
                                getStringValue(
                                        document,
                                        "doctorName"
                                )
                        );

                        // Notes
                        report.setNotes(
                                getStringValue(
                                        document,
                                        "notes"
                                )
                        );

                        // Firebase Storage URL
                        report.setFileUrl(
                                getStringValue(
                                        document,
                                        "fileUrl"
                                )
                        );

                        // Firebase Storage Path
                        report.setStoragePath(
                                getStringValue(
                                        document,
                                        "storagePath"
                                )
                        );

                        // Add report to list
                        reportList.add(report);
                    }

                    // Update RecyclerView
                    adapter.notifyDataSetChanged();

                    // Hide progress
                    progressBar.setVisibility(
                            View.GONE
                    );

                    // Show empty message
                    if (reportList.isEmpty()) {

                        tvNoReports.setVisibility(
                                View.VISIBLE
                        );

                    } else {

                        tvNoReports.setVisibility(
                                View.GONE
                        );
                    }

                })
                .addOnFailureListener(e -> {

                    progressBar.setVisibility(
                            View.GONE
                    );

                    Toast.makeText(
                            HealthReportsActivity.this,
                            "Failed to load reports: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    // ============================================================
    // GET STRING FROM FIRESTORE SAFELY
    // ============================================================

    private String getStringValue(
            DocumentSnapshot document,
            String field
    ) {

        String value =
                document.getString(field);

        if (value == null) {
            return "";
        }

        return value;
    }
}
