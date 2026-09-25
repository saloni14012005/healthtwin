package com.healthtwin.ai.profile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.healthtwin.ai.R;
import com.healthtwin.ai.auth.LoginActivity;

import java.io.File;

public class AccountActivity extends AppCompatActivity {

    ImageView imgProfile;

    TextView tvName, tvEmail, tvAge,
            tvGender, tvHeight, tvWeight, tvBlood;

    Button btnEditProfile, btnLogout;

    FirebaseAuth auth;
    FirebaseFirestore firestore;

    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_account);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        preferences = getSharedPreferences(
                "HealthTwinProfile",
                MODE_PRIVATE
        );

        imgProfile = findViewById(R.id.imgProfile);

        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvAge = findViewById(R.id.tvAge);
        tvGender = findViewById(R.id.tvGender);
        tvHeight = findViewById(R.id.tvHeight);
        tvWeight = findViewById(R.id.tvWeight);
        tvBlood = findViewById(R.id.tvBlood);

        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnLogout = findViewById(R.id.btnLogout);

        // Load profile picture
        loadProfilePicture();

        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {

            if (user.getEmail() != null) {
                tvEmail.setText(user.getEmail());
            }

            loadProfile(user.getUid());
        }

        // Edit Profile
        btnEditProfile.setOnClickListener(v -> {

            Intent intent = new Intent(
                    AccountActivity.this,
                    EditProfileActivity.class
            );

            startActivity(intent);
        });

        // Logout
        btnLogout.setOnClickListener(v -> {

            auth.signOut();

            Intent intent = new Intent(
                    AccountActivity.this,
                    LoginActivity.class
            );

            intent.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
            );

            startActivity(intent);

            finish();
        });
    }

    // =========================================================
    // LOAD PROFILE PICTURE FROM APP STORAGE
    // =========================================================

    private void loadProfilePicture() {

        String imagePath =
                preferences.getString(
                        "profile_picture",
                        null
                );

        if (imagePath != null &&
                !imagePath.isEmpty()) {

            File file = new File(imagePath);

            if (file.exists()) {

                Bitmap bitmap =
                        BitmapFactory.decodeFile(
                                file.getAbsolutePath()
                        );

                if (bitmap != null) {

                    imgProfile.setImageBitmap(bitmap);

                    imgProfile.setScaleType(
                            ImageView.ScaleType.CENTER_CROP
                    );

                    return;
                }
            }
        }

        // Default profile picture
        imgProfile.setImageResource(
                android.R.drawable.ic_menu_myplaces
        );
    }

    // =========================================================
    // LOAD PROFILE FROM FIRESTORE
    // =========================================================

    private void loadProfile(String uid) {

        firestore.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (documentSnapshot.exists()) {

                        // Name
                        String name =
                                documentSnapshot.getString("name");

                        if (name != null &&
                                !name.trim().isEmpty()) {

                            tvName.setText(name);

                        } else {

                            tvName.setText(
                                    "HealthTwin User"
                            );
                        }

                        // Age
                        String age =
                                documentSnapshot.getString("age");

                        tvAge.setText(
                                age != null &&
                                        !age.isEmpty()
                                        ? age + " Years"
                                        : "Not Added"
                        );

                        // Gender
                        String gender =
                                documentSnapshot.getString("gender");

                        tvGender.setText(
                                gender != null &&
                                        !gender.isEmpty()
                                        ? gender
                                        : "Not Added"
                        );

                        // Height
                        String height =
                                documentSnapshot.getString("height");

                        tvHeight.setText(
                                height != null &&
                                        !height.isEmpty()
                                        ? height + " cm"
                                        : "Not Added"
                        );

                        // Weight
                        String weight =
                                documentSnapshot.getString("weight");

                        tvWeight.setText(
                                weight != null &&
                                        !weight.isEmpty()
                                        ? weight + " kg"
                                        : "Not Added"
                        );

                        // Blood Group
                        String bloodGroup =
                                documentSnapshot.getString(
                                        "bloodGroup"
                                );

                        tvBlood.setText(
                                bloodGroup != null &&
                                        !bloodGroup.isEmpty()
                                        ? bloodGroup
                                        : "Not Added"
                        );
                    }
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            AccountActivity.this,
                            "Failed to load profile: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    // =========================================================
    // REFRESH WHEN RETURNING FROM EDIT PROFILE
    // =========================================================

    @Override
    protected void onResume() {

        super.onResume();

        loadProfilePicture();

        FirebaseUser user =
                auth.getCurrentUser();

        if (user != null) {

            loadProfile(user.getUid());
        }
    }
}