package com.healthtwin.ai.report;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.healthtwin.ai.R;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AddReportActivity extends AppCompatActivity {

    private EditText etReportName;
    private EditText etReportDate;
    private EditText etDoctorName;
    private EditText etNotes;

    private Button btnSaveReport;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_report);

        // Find views
        etReportName = findViewById(R.id.etReportName);
        etReportDate = findViewById(R.id.etReportDate);
        etDoctorName = findViewById(R.id.etDoctorName);
        etNotes = findViewById(R.id.etNotes);

        btnSaveReport = findViewById(R.id.btnSaveReport);

        // Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Date picker
        etReportDate.setOnClickListener(
                v -> openDatePicker()
        );

        // Save report
        btnSaveReport.setOnClickListener(
                v -> saveReport()
        );
    }

    // ============================================================
    // DATE PICKER
    // ============================================================

    private void openDatePicker() {

        Calendar calendar = Calendar.getInstance();

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog =
                new DatePickerDialog(
                        this,
                        (view, selectedYear, selectedMonth, selectedDay) -> {

                            String date =
                                    String.format(
                                            "%02d/%02d/%04d",
                                            selectedDay,
                                            selectedMonth + 1,
                                            selectedYear
                                    );

                            etReportDate.setText(date);
                        },
                        year,
                        month,
                        day
                );

        datePickerDialog.show();
    }

    // ============================================================
    // SAVE REPORT
    // ============================================================

    private void saveReport() {

        String reportName =
                etReportName.getText()
                        .toString()
                        .trim();

        String reportDate =
                etReportDate.getText()
                        .toString()
                        .trim();

        String doctorName =
                etDoctorName.getText()
                        .toString()
                        .trim();

        String notes =
                etNotes.getText()
                        .toString()
                        .trim();

        // Check login
        if (auth.getCurrentUser() == null) {

            Toast.makeText(
                    this,
                    "Please login first",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // Validate report name
        if (reportName.isEmpty()) {

            etReportName.setError(
                    "Enter report name"
            );

            etReportName.requestFocus();

            return;
        }

        // Validate date
        if (reportDate.isEmpty()) {

            etReportDate.setError(
                    "Select report date"
            );

            etReportDate.requestFocus();

            return;
        }

        // Disable button while saving
        btnSaveReport.setEnabled(false);

        String userId =
                auth.getCurrentUser()
                        .getUid();

        saveReportToFirestore(
                userId,
                reportName,
                reportDate,
                doctorName,
                notes
        );
    }

    // ============================================================
    // SAVE TO FIRESTORE
    // ============================================================

    private void saveReportToFirestore(
            String userId,
            String reportName,
            String reportDate,
            String doctorName,
            String notes
    ) {

        Map<String, Object> report =
                new HashMap<>();

        report.put(
                "reportName",
                reportName
        );

        report.put(
                "reportDate",
                reportDate
        );

        report.put(
                "doctorName",
                doctorName
        );

        report.put(
                "notes",
                notes
        );

        report.put(
                "createdAt",
                System.currentTimeMillis()
        );

        db.collection("users")
                .document(userId)
                .collection("reports")
                .add(report)
                .addOnSuccessListener(
                        documentReference -> {

                            Toast.makeText(
                                    AddReportActivity.this,
                                    "Report saved successfully",
                                    Toast.LENGTH_SHORT
                            ).show();

                            finish();
                        }
                )
                .addOnFailureListener(e -> {

                    btnSaveReport.setEnabled(true);

                    Toast.makeText(
                            AddReportActivity.this,
                            "Failed to save report: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }
}