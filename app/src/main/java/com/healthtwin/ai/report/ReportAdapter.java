package com.healthtwin.ai.report;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.healthtwin.ai.R;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

public class ReportAdapter
        extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {

    private final Context context;
    private final ArrayList<ReportModel> reportList;

    public ReportAdapter(
            Context context,
            ArrayList<ReportModel> reportList
    ) {
        this.context = context;
        this.reportList = reportList;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view = LayoutInflater.from(context)
                .inflate(
                        R.layout.item_report,
                        parent,
                        false
                );

        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ReportViewHolder holder,
            int position
    ) {

        ReportModel report = reportList.get(position);

        // -----------------------------
        // REPORT NAME
        // -----------------------------

        holder.tvReportName.setText(
                report.getReportName()
        );

        // -----------------------------
        // DATE
        // -----------------------------

        holder.tvReportDate.setText(
                "Date: " + report.getReportDate()
        );

        // -----------------------------
        // DOCTOR
        // -----------------------------

        String doctor = report.getDoctorName();

        if (doctor == null || doctor.isEmpty()) {

            holder.tvDoctorName.setText(
                    "Doctor: Not provided"
            );

        } else {

            holder.tvDoctorName.setText(
                    "Doctor: " + doctor
            );
        }

        // -----------------------------
        // NOTES
        // -----------------------------

        String notes = report.getNotes();

        if (notes == null || notes.isEmpty()) {

            holder.tvNotes.setText(
                    "Notes: No notes"
            );

        } else {

            holder.tvNotes.setText(
                    "Notes: " + notes
            );
        }

        // -----------------------------
        // FILE
        // -----------------------------

        String fileUrl = report.getFileUrl();

        if (fileUrl == null || fileUrl.isEmpty()) {

            holder.tvFileStatus.setText(
                    "No report file attached"
            );

            holder.btnOpenReport.setVisibility(
                    View.GONE
            );

        } else {

            holder.tvFileStatus.setText(
                    "📄 Report file attached"
            );

            holder.btnOpenReport.setVisibility(
                    View.VISIBLE
            );

            holder.btnOpenReport.setOnClickListener(v -> {

                try {

                    Intent intent =
                            new Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse(fileUrl)
                            );

                    intent.addFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK
                    );

                    context.startActivity(intent);

                } catch (Exception e) {

                    Toast.makeText(
                            context,
                            "Unable to open report",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            });
        }

        // -----------------------------
        // DELETE
        // -----------------------------

        holder.btnDeleteReport.setOnClickListener(v -> {

            new AlertDialog.Builder(context)
                    .setTitle("Delete Report")
                    .setMessage(
                            "Are you sure you want to delete this report?"
                    )
                    .setNegativeButton(
                            "Cancel",
                            null
                    )
                    .setPositiveButton(
                            "Delete",
                            (dialog, which) -> {

                                deleteReport(
                                        report,
                                        holder.getAdapterPosition()
                                );
                            }
                    )
                    .show();
        });
    }

    // ============================================================
    // DELETE REPORT
    // ============================================================

    private void deleteReport(
            ReportModel report,
            int position
    ) {

        if (position == RecyclerView.NO_POSITION) {
            return;
        }

        String reportId =
                report.getId();

        if (reportId == null || reportId.isEmpty()) {

            Toast.makeText(
                    context,
                    "Unable to delete report",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String storagePath =
                report.getStoragePath();

        // First delete file from Firebase Storage
        if (storagePath != null
                && !storagePath.isEmpty()) {

            StorageReference fileReference =
                    FirebaseStorage
                            .getInstance()
                            .getReference()
                            .child(storagePath);

            fileReference.delete()
                    .addOnSuccessListener(unused -> {

                        deleteFirestoreReport(
                                reportId,
                                position
                        );

                    })
                    .addOnFailureListener(e -> {

                        // Even if the Storage file is
                        // already missing, delete Firestore record

                        deleteFirestoreReport(
                                reportId,
                                position
                        );
                    });

        } else {

            deleteFirestoreReport(
                    reportId,
                    position
            );
        }
    }

    // ============================================================
    // DELETE FIRESTORE DOCUMENT
    // ============================================================

    private void deleteFirestoreReport(
            String reportId,
            int position
    ) {

        FirebaseFirestore
                .getInstance()
                .collection("users")
                .document(
                        com.google.firebase.auth.FirebaseAuth
                                .getInstance()
                                .getCurrentUser()
                                .getUid()
                )
                .collection("reports")
                .document(reportId)
                .delete()
                .addOnSuccessListener(unused -> {

                    reportList.remove(position);

                    notifyItemRemoved(position);

                    Toast.makeText(
                            context,
                            "Report deleted",
                            Toast.LENGTH_SHORT
                    ).show();
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            context,
                            "Failed to delete report",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

    // ============================================================
    // VIEW HOLDER
    // ============================================================

    public static class ReportViewHolder
            extends RecyclerView.ViewHolder {

        TextView tvReportName;
        TextView tvReportDate;
        TextView tvDoctorName;
        TextView tvNotes;
        TextView tvFileStatus;

        MaterialButton btnOpenReport;
        MaterialButton btnDeleteReport;

        public ReportViewHolder(
                @NonNull View itemView
        ) {

            super(itemView);

            tvReportName =
                    itemView.findViewById(
                            R.id.tvReportName
                    );

            tvReportDate =
                    itemView.findViewById(
                            R.id.tvReportDate
                    );

            tvDoctorName =
                    itemView.findViewById(
                            R.id.tvDoctorName
                    );

            tvNotes =
                    itemView.findViewById(
                            R.id.tvNotes
                    );

            tvFileStatus =
                    itemView.findViewById(
                            R.id.tvFileStatus
                    );

            btnOpenReport =
                    itemView.findViewById(
                            R.id.btnOpenReport
                    );

            btnDeleteReport =
                    itemView.findViewById(
                            R.id.btnDeleteReport
                    );
        }
    }
}