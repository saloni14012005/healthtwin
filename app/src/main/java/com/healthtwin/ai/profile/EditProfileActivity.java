package com.healthtwin.ai.profile;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.healthtwin.ai.R;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    TextInputEditText etName, etAge, etGender,
            etHeight, etWeight, etBloodGroup;

    MaterialButton btnSave, btnChangePhoto;

    ImageView imgProfile;

    FirebaseAuth auth;
    FirebaseFirestore firestore;
    FirebaseUser user;

    SharedPreferences preferences;

    // Image picker
    private final ActivityResultLauncher<String> imagePicker =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {

                        if (uri != null) {
                            saveProfileImage(uri);
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit_profile);

        // Firebase
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        user = auth.getCurrentUser();

        // SharedPreferences
        preferences = getSharedPreferences(
                "HealthTwinProfile",
                MODE_PRIVATE
        );

        // Views
        imgProfile = findViewById(R.id.imgProfile);
        btnChangePhoto = findViewById(R.id.btnChangePhoto);

        etName = findViewById(R.id.etName);
        etAge = findViewById(R.id.etAge);
        etGender = findViewById(R.id.etGender);
        etHeight = findViewById(R.id.etHeight);
        etWeight = findViewById(R.id.etWeight);
        etBloodGroup = findViewById(R.id.etBloodGroup);

        btnSave = findViewById(R.id.btnSave);

        // Load saved picture
        loadProfilePicture();

        // Load Firestore profile
        loadProfile();

        // Change profile picture
        btnChangePhoto.setOnClickListener(v -> {

            imagePicker.launch("image/*");

        });

        // Save profile
        btnSave.setOnClickListener(v -> saveProfile());
    }

    // =========================================================
    // SAVE PROFILE PICTURE PERMANENTLY
    // =========================================================

    private void saveProfileImage(Uri uri) {

        try {

            Bitmap bitmap = BitmapFactory.decodeStream(
                    getContentResolver().openInputStream(uri)
            );

            if (bitmap == null) {

                Toast.makeText(
                        EditProfileActivity.this,
                        "Could not load selected image",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            // Create file inside app's private storage
            File file = new File(
                    getFilesDir(),
                    "profile_picture.jpg"
            );

            FileOutputStream outputStream =
                    new FileOutputStream(file);

            bitmap.compress(
                    Bitmap.CompressFormat.JPEG,
                    90,
                    outputStream
            );

            outputStream.flush();
            outputStream.close();

            // Save permanent local file path
            preferences.edit()
                    .putString(
                            "profile_picture",
                            file.getAbsolutePath()
                    )
                    .apply();

            // Show image immediately
            imgProfile.setImageBitmap(bitmap);

            Toast.makeText(
                    EditProfileActivity.this,
                    "Profile picture updated",
                    Toast.LENGTH_SHORT
            ).show();

        } catch (Exception e) {

            e.printStackTrace();

            Toast.makeText(
                    EditProfileActivity.this,
                    "Could not save profile picture",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    // =========================================================
    // LOAD PROFILE PICTURE
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
                }
            }
        }
    }

    // =========================================================
    // LOAD PROFILE FROM FIRESTORE
    // =========================================================

    private void loadProfile() {

        if (user == null) {
            return;
        }

        firestore.collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (documentSnapshot.exists()) {

                        etName.setText(
                                documentSnapshot.getString("name")
                        );

                        etAge.setText(
                                documentSnapshot.getString("age")
                        );

                        etGender.setText(
                                documentSnapshot.getString("gender")
                        );

                        etHeight.setText(
                                documentSnapshot.getString("height")
                        );

                        etWeight.setText(
                                documentSnapshot.getString("weight")
                        );

                        etBloodGroup.setText(
                                documentSnapshot.getString("bloodGroup")
                        );
                    }
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            EditProfileActivity.this,
                            "Failed to load profile: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    // =========================================================
    // SAVE PROFILE TO FIRESTORE
    // =========================================================

    private void saveProfile() {

        if (user == null) {
            return;
        }

        String name = etName.getText()
                .toString()
                .trim();

        String age = etAge.getText()
                .toString()
                .trim();

        String gender = etGender.getText()
                .toString()
                .trim();

        String height = etHeight.getText()
                .toString()
                .trim();

        String weight = etWeight.getText()
                .toString()
                .trim();

        String bloodGroup = etBloodGroup.getText()
                .toString()
                .trim();

        Map<String, Object> profile =
                new HashMap<>();

        profile.put("name", name);
        profile.put("age", age);
        profile.put("gender", gender);
        profile.put("height", height);
        profile.put("weight", weight);
        profile.put("bloodGroup", bloodGroup);
        profile.put("email", user.getEmail());

        firestore.collection("users")
                .document(user.getUid())
                .set(profile)
                .addOnSuccessListener(unused -> {

                    Toast.makeText(
                            EditProfileActivity.this,
                            "Profile Saved Successfully",
                            Toast.LENGTH_SHORT
                    ).show();

                    finish();
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            EditProfileActivity.this,
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }
}