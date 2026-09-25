package com.healthtwin.ai.medicine;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.healthtwin.ai.R;

import java.util.ArrayList;

public class MedicineAdapter
        extends RecyclerView.Adapter<MedicineAdapter.MedicineViewHolder> {

    public interface OnDeleteClickListener {
        void onDelete(Medicine medicine);
    }

    private final ArrayList<Medicine> medicineList;
    private final OnDeleteClickListener deleteClickListener;

    public MedicineAdapter(
            ArrayList<Medicine> medicineList,
            OnDeleteClickListener deleteClickListener) {

        this.medicineList = medicineList;
        this.deleteClickListener = deleteClickListener;
    }

    @NonNull
    @Override
    public MedicineViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(
                        R.layout.item_medicine,
                        parent,
                        false
                );

        return new MedicineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull MedicineViewHolder holder,
            int position) {

        Medicine medicine = medicineList.get(position);

        holder.medicineNameText.setText(
                medicine.getMedicineName()
        );

        holder.dosageText.setText(
                "Dosage: " + medicine.getDosage()
        );

        holder.timeText.setText(
                "Time: " + medicine.getTime()
        );

        holder.frequencyText.setText(
                "Frequency: " + medicine.getFrequency()
        );

        holder.deleteButton.setOnClickListener(v -> {

            if (deleteClickListener != null) {
                deleteClickListener.onDelete(medicine);
            }
        });
    }

    @Override
    public int getItemCount() {
        return medicineList.size();
    }

    public static class MedicineViewHolder
            extends RecyclerView.ViewHolder {

        TextView medicineNameText;
        TextView dosageText;
        TextView timeText;
        TextView frequencyText;
        Button deleteButton;

        public MedicineViewHolder(
                @NonNull View itemView) {

            super(itemView);

            medicineNameText =
                    itemView.findViewById(
                            R.id.medicineNameText
                    );

            dosageText =
                    itemView.findViewById(
                            R.id.dosageText
                    );

            timeText =
                    itemView.findViewById(
                            R.id.timeText
                    );

            frequencyText =
                    itemView.findViewById(
                            R.id.frequencyText
                    );

            deleteButton =
                    itemView.findViewById(
                            R.id.deleteButton
                    );
        }
    }
}