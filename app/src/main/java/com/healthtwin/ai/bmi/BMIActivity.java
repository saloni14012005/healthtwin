package com.healthtwin.ai.bmi;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.healthtwin.ai.R;

import java.util.Locale;

public class BMIActivity extends AppCompatActivity {

    EditText etWeight, etHeight;
    Button btnCalculate;
    TextView btnClear, tvBMI, tvCategory;
    ImageButton btnBack;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Firebase
        auth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = auth.getCurrentUser();

        // Check if user is logged in
        if (currentUser == null) {

            Toast.makeText(
                    this,
                    "Please login first",
                    Toast.LENGTH_LONG
            ).show();

            finish();
            return;
        }

        setContentView(R.layout.activity_bmi);

        // Find Views
        etWeight = findViewById(R.id.etWeight);
        etHeight = findViewById(R.id.etHeight);

        btnCalculate = findViewById(R.id.btnCalculate);
        btnClear = findViewById(R.id.btnClear);

        tvBMI = findViewById(R.id.tvBMI);
        tvCategory = findViewById(R.id.tvCategory);

        btnBack = findViewById(R.id.btnBack);

        // Calculate BMI
        btnCalculate.setOnClickListener(v -> calculateBMI());

        // Clear
        btnClear.setOnClickListener(v -> {

            etWeight.setText("");
            etHeight.setText("");

            tvBMI.setText("--");
            tvCategory.setText("Enter your details");

            // Gray color after clearing
            tvCategory.setTextColor(
                    getColor(android.R.color.darker_gray)
            );
        });

        // Back
        btnBack.setOnClickListener(v -> finish());
    }


    private void calculateBMI() {

        String weightString =
                etWeight.getText().toString().trim();

        String heightString =
                etHeight.getText().toString().trim();


        // Check empty fields
        if (weightString.isEmpty() ||
                heightString.isEmpty()) {

            Toast.makeText(
                    this,
                    "Please enter your weight and height",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        try {

            double weight =
                    Double.parseDouble(weightString);

            double heightCm =
                    Double.parseDouble(heightString);


            // Check valid values
            if (weight <= 0 || heightCm <= 0) {

                Toast.makeText(
                        this,
                        "Please enter valid values",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }


            // Convert cm to meters
            double heightMeter =
                    heightCm / 100;


            // BMI Formula
            double bmi =
                    weight / (heightMeter * heightMeter);


            // Show BMI
            tvBMI.setText(
                    String.format(
                            Locale.US,
                            "%.1f",
                            bmi
                    )
            );


            // BMI Category
            if (bmi < 18.5) {

                tvCategory.setText("Underweight");

                // BLUE
                tvCategory.setTextColor(
                        android.graphics.Color.parseColor("#64B5F6")
                );

            } else if (bmi < 25) {

                tvCategory.setText("Normal Weight");

                // GREEN
                tvCategory.setTextColor(
                        android.graphics.Color.parseColor("#66BB6A")
                );

            } else if (bmi < 30) {

                tvCategory.setText("Overweight");

                // ORANGE
                tvCategory.setTextColor(
                        android.graphics.Color.parseColor("#FFA726")
                );

            } else {

                tvCategory.setText("Obese");

                // RED
                tvCategory.setTextColor(
                        android.graphics.Color.parseColor("#EF5350")
                );
            }


        } catch (NumberFormatException e) {

            Toast.makeText(
                    this,
                    "Please enter valid numbers",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
}