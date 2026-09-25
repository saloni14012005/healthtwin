
package com.healthtwin.ai.water;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.healthtwin.ai.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class WaterIntakeActivity extends AppCompatActivity {

    private TextView tvWaterAmount;
    private TextView tvWaterGoal;
    private TextView tvPercentage;
    private TextView tvGlassCount;

    private EditText etWaterAmount;

    private Button btnAddWater;
    private Button btnRemoveWater;
    private Button btnReset;

    private ProgressBar progressWater;

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;

    private double currentWater = 0;
    private double waterGoal = 2000;

    private String todayDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_water_intake);

        // Firebase
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Get today's date
        todayDate = new SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
        ).format(new Date());

        // Find views
        tvWaterAmount = findViewById(R.id.tvWaterAmount);
        tvWaterGoal = findViewById(R.id.tvWaterGoal);
        tvPercentage = findViewById(R.id.tvPercentage);
        tvGlassCount = findViewById(R.id.tvGlassCount);

        etWaterAmount = findViewById(R.id.etWaterAmount);

        btnAddWater = findViewById(R.id.btnAddWater);
        btnRemoveWater = findViewById(R.id.btnRemoveWater);
        btnReset = findViewById(R.id.btnReset);

        progressWater = findViewById(R.id.progressWater);

        // Show default goal
        tvWaterGoal.setText("Goal: " + (int) waterGoal + " ml");

        // Load today's data
        loadWaterData();

        // Add water
        btnAddWater.setOnClickListener(v -> {

            String amountText = etWaterAmount.getText().toString().trim();

            if (amountText.isEmpty()) {
                etWaterAmount.setError("Enter water amount");
                return;
            }

            double amount;

            try {
                amount = Double.parseDouble(amountText);
            } catch (NumberFormatException e) {
                etWaterAmount.setError("Enter a valid amount");
                return;
            }

            if (amount <= 0) {
                etWaterAmount.setError("Amount must be greater than 0");
                return;
            }

            currentWater += amount;

            saveWaterData();

            etWaterAmount.setText("");

            Toast.makeText(
                    WaterIntakeActivity.this,
                    "Water added successfully 💧",
                    Toast.LENGTH_SHORT
            ).show();
        });

        // Remove water
        btnRemoveWater.setOnClickListener(v -> {

            String amountText = etWaterAmount.getText().toString().trim();

            if (amountText.isEmpty()) {
                etWaterAmount.setError("Enter water amount");
                return;
            }

            double amount;

            try {
                amount = Double.parseDouble(amountText);
            } catch (NumberFormatException e) {
                etWaterAmount.setError("Enter a valid amount");
                return;
            }

            if (amount <= 0) {
                etWaterAmount.setError("Amount must be greater than 0");
                return;
            }

            currentWater -= amount;

            if (currentWater < 0) {
                currentWater = 0;
            }

            saveWaterData();

            etWaterAmount.setText("");

            Toast.makeText(
                    WaterIntakeActivity.this,
                    "Water removed",
                    Toast.LENGTH_SHORT
            ).show();
        });

        // Reset today's water
        btnReset.setOnClickListener(v -> {

            currentWater = 0;

            saveWaterData();

            Toast.makeText(
                    WaterIntakeActivity.this,
                    "Today's water intake reset",
                    Toast.LENGTH_SHORT
            ).show();
        });
    }

    // Load today's water data from Firestore
    private void loadWaterData() {

        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            Toast.makeText(
                    this,
                    "Please login first",
                    Toast.LENGTH_SHORT
            ).show();
            finish();
            return;
        }

        String userId = user.getUid();

        firestore.collection("users")
                .document(userId)
                .collection("waterIntake")
                .document(todayDate)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (documentSnapshot.exists()) {

                        Double savedWater =
                                documentSnapshot.getDouble("waterAmount");

                        Double savedGoal =
                                documentSnapshot.getDouble("waterGoal");

                        if (savedWater != null) {
                            currentWater = savedWater;
                        }

                        if (savedGoal != null) {
                            waterGoal = savedGoal;
                        }
                    }

                    updateUI();
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            WaterIntakeActivity.this,
                            "Failed to load water data",
                            Toast.LENGTH_SHORT
                    ).show();

                    updateUI();
                });
    }

    // Save today's water data
    private void saveWaterData() {

        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            Toast.makeText(
                    this,
                    "Please login first",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        String userId = user.getUid();

        Map<String, Object> waterData = new HashMap<>();

        waterData.put("waterAmount", currentWater);
        waterData.put("waterGoal", waterGoal);
        waterData.put("date", todayDate);
        waterData.put("updatedAt", System.currentTimeMillis());

        firestore.collection("users")
                .document(userId)
                .collection("waterIntake")
                .document(todayDate)
                .set(waterData)
                .addOnSuccessListener(unused -> {

                    updateUI();

                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            WaterIntakeActivity.this,
                            "Failed to save water data: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    // Update screen
    private void updateUI() {

        int current = (int) currentWater;
        int goal = (int) waterGoal;

        tvWaterAmount.setText(current + " ml");

        tvWaterGoal.setText(
                "Goal: " + goal + " ml"
        );

        // Calculate percentage
        int percentage;

        if (waterGoal > 0) {
            percentage = (int) ((currentWater / waterGoal) * 100);
        } else {
            percentage = 0;
        }

        // Maximum 100 for progress bar
        int progress = Math.min(percentage, 100);

        progressWater.setProgress(progress);

        tvPercentage.setText(
                percentage + "%"
        );

        // One glass = 250 ml
        int glasses = (int) Math.round(currentWater / 250);

        tvGlassCount.setText(
                glasses + " glasses"
        );
    }
}