package com.healthtwin.ai.dashboard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.healthtwin.ai.R;
import com.healthtwin.ai.medicine.MedicineReminderActivity;
import com.healthtwin.ai.water.WaterIntakeActivity;

import java.util.Calendar;

public class DashboardActivity extends AppCompatActivity {

    private CardView cardBMI;
    private CardView cardMedicine;
    private CardView cardReport;
    private CardView cardWater;

    private TextView tvUsername;
    private TextView tvGreeting;

    private ImageView btnAccount;

    private EditText etAIQuestion;
    private ImageButton btnAISend;

    private Button btnLogout;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private SharedPreferences preferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_dashboard);

        // Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // SharedPreferences for profile picture
        preferences = getSharedPreferences(
                "HealthTwinProfile",
                MODE_PRIVATE
        );

        // -----------------------------------------------------
        // FIND VIEWS
        // -----------------------------------------------------

        cardBMI = findViewById(R.id.cardBMI);
        cardMedicine = findViewById(R.id.cardMedicine);
        cardReport = findViewById(R.id.cardReport);
        cardWater = findViewById(R.id.cardWater);

        tvUsername = findViewById(R.id.tvUsername);
        tvGreeting = findViewById(R.id.tvGreeting);

        btnAccount = findViewById(R.id.btnAccount);

        etAIQuestion = findViewById(R.id.etAIQuestion);
        btnAISend = findViewById(R.id.btnAISend);

        btnLogout = findViewById(R.id.btnLogout);


        // -----------------------------------------------------
        // GREETING
        // -----------------------------------------------------

        updateGreeting();


        // -----------------------------------------------------
        // LOAD USER NAME
        // -----------------------------------------------------

        loadUserName();


        // -----------------------------------------------------
        // LOAD PROFILE PICTURE
        // -----------------------------------------------------

        loadProfilePicture();


        // -----------------------------------------------------
        // ACCOUNT BUTTON
        // -----------------------------------------------------

        btnAccount.setOnClickListener(v -> {

            try {

                Intent intent = new Intent(
                        DashboardActivity.this,
                        com.healthtwin.ai.profile.AccountActivity.class
                );

                startActivity(intent);

            } catch (Exception e) {

                Toast.makeText(
                        DashboardActivity.this,
                        "Account page could not be opened",
                        Toast.LENGTH_SHORT
                ).show();

                e.printStackTrace();
            }
        });


        // -----------------------------------------------------
        // BMI
        // -----------------------------------------------------

        cardBMI.setOnClickListener(v -> {

            try {

                Intent intent = new Intent(
                        DashboardActivity.this,
                        com.healthtwin.ai.bmi.BMIActivity.class
                );

                startActivity(intent);

            } catch (Exception e) {

                Toast.makeText(
                        DashboardActivity.this,
                        "BMI Calculator could not be opened",
                        Toast.LENGTH_SHORT
                ).show();

                e.printStackTrace();
            }
        });


        // -----------------------------------------------------
        // MEDICINE
        // -----------------------------------------------------

        cardMedicine.setOnClickListener(v -> {

            try {

                Intent intent = new Intent(
                        DashboardActivity.this,
                        MedicineReminderActivity.class
                );

                startActivity(intent);

            } catch (Exception e) {

                Toast.makeText(
                        DashboardActivity.this,
                        "Medicine Reminder could not be opened",
                        Toast.LENGTH_SHORT
                ).show();

                e.printStackTrace();
            }
        });


        // -----------------------------------------------------
        // HEALTH REPORT
        // -----------------------------------------------------

        cardReport.setOnClickListener(v -> {

            try {

                Intent intent = new Intent(
                        DashboardActivity.this,
                        com.healthtwin.ai.report.HealthReportsActivity.class
                );

                startActivity(intent);

            } catch (Exception e) {

                Toast.makeText(
                        DashboardActivity.this,
                        "Health Report could not be opened",
                        Toast.LENGTH_SHORT
                ).show();

                e.printStackTrace();
            }
        });


        // -----------------------------------------------------
        // WATER INTAKE
        // -----------------------------------------------------

        cardWater.setOnClickListener(v -> {

            try {

                Intent intent = new Intent(
                        DashboardActivity.this,
                        WaterIntakeActivity.class
                );

                startActivity(intent);

            } catch (Exception e) {

                Toast.makeText(
                        DashboardActivity.this,
                        "Water Intake could not be opened",
                        Toast.LENGTH_SHORT
                ).show();

                e.printStackTrace();
            }
        });


        // -----------------------------------------------------
        // AI CHAT
        // -----------------------------------------------------

        btnAISend.setOnClickListener(v -> {

            String question =
                    etAIQuestion.getText().toString().trim();

            if (question.isEmpty()) {

                etAIQuestion.setError("Ask something first");

                etAIQuestion.requestFocus();

                return;
            }

            try {

                Intent intent = new Intent(
                        DashboardActivity.this,
                        com.healthtwin.ai.ai.AIChatActivity.class
                );

                intent.putExtra("question", question);

                startActivity(intent);

                etAIQuestion.setText("");

            } catch (Exception e) {

                Toast.makeText(
                        DashboardActivity.this,
                        "AI Chat could not be opened",
                        Toast.LENGTH_SHORT
                ).show();

                e.printStackTrace();
            }
        });


    }

    // =========================================================
    // UPDATE GREETING
    // =========================================================

    private void updateGreeting() {

        Calendar calendar = Calendar.getInstance();

        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        String greeting;

        if (hour >= 5 && hour < 12) {

            greeting = "Good Morning";

        } else if (hour >= 12 && hour < 17) {

            greeting = "Good Afternoon";

        } else if (hour >= 17 && hour < 21) {

            greeting = "Good Evening";

        } else {

            greeting = "Good Night";
        }

        tvGreeting.setText(greeting);
    }


    // =========================================================
    // LOAD USER NAME
    // =========================================================

    private void loadUserName() {

        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {

            tvUsername.setText("User");

            return;
        }

        String uid = currentUser.getUid();

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (documentSnapshot.exists()) {

                        String name =
                                documentSnapshot.getString("name");

                        if (name != null &&
                                !name.trim().isEmpty()) {

                            tvUsername.setText(name);

                        } else {

                            tvUsername.setText("User");
                        }

                    } else {

                        tvUsername.setText("User");
                    }

                })
                .addOnFailureListener(e -> {

                    tvUsername.setText("User");

                    Toast.makeText(
                            DashboardActivity.this,
                            "Could not load user name",
                            Toast.LENGTH_SHORT
                    ).show();

                    e.printStackTrace();
                });
    }


    // =========================================================
    // LOAD PROFILE PICTURE
    // =========================================================

    private void loadProfilePicture() {

        String imageUri = preferences.getString(
                "profile_picture",
                null
        );

        if (imageUri != null && !imageUri.isEmpty()) {

            try {

                Uri uri = Uri.parse(imageUri);

                btnAccount.setImageURI(uri);

                btnAccount.setScaleType(
                        ImageView.ScaleType.CENTER_CROP
                );

            } catch (Exception e) {

                e.printStackTrace();

                btnAccount.setImageResource(
                        android.R.drawable.ic_menu_myplaces
                );
            }

        } else {

            btnAccount.setImageResource(
                    android.R.drawable.ic_menu_myplaces
            );
        }
    }


    // =========================================================
    // REFRESH WHEN RETURNING TO DASHBOARD
    // =========================================================

    @Override
    protected void onResume() {

        super.onResume();

        if (tvGreeting != null) {
            updateGreeting();
        }

        if (auth != null && tvUsername != null) {
            loadUserName();
        }

        if (preferences != null && btnAccount != null) {
            loadProfilePicture();
        }
    }
}