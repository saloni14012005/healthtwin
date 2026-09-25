package com.healthtwin.ai.medicine;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.healthtwin.ai.R;

import java.util.ArrayList;
import java.util.Calendar;

public class MedicineReminderActivity extends AppCompatActivity {

    private EditText medicineName;
    private EditText dosage;
    private TextView selectedTime;
    private Spinner frequencySpinner;
    private Button saveButton;
    private RecyclerView medicineRecyclerView;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    private ArrayList<Medicine> medicineList;
    private MedicineAdapter medicineAdapter;

    private String selectedTimeValue = "";

    private int selectedHour = -1;
    private int selectedMinute = -1;

    private static final int NOTIFICATION_PERMISSION_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_medicine_reminder);

        // Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Views
        medicineName = findViewById(R.id.medicineName);
        dosage = findViewById(R.id.dosage);
        selectedTime = findViewById(R.id.selectedTime);
        frequencySpinner = findViewById(R.id.frequencySpinner);
        saveButton = findViewById(R.id.saveButton);
        medicineRecyclerView = findViewById(R.id.medicineRecyclerView);

        // Notification permission
        requestNotificationPermission();

        // Frequency spinner
        String[] frequencies = {
                "Once a day",
                "Twice a day",
                "Three times a day",
                "As needed"
        };

        ArrayAdapter<String> spinnerAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        frequencies
                );

        spinnerAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        frequencySpinner.setAdapter(spinnerAdapter);

        // RecyclerView
        medicineList = new ArrayList<>();

        medicineAdapter = new MedicineAdapter(
                medicineList,
                this::deleteMedicine
        );

        medicineRecyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );

        medicineRecyclerView.setAdapter(
                medicineAdapter
        );

        // Select time
        selectedTime.setOnClickListener(v ->
                showTimePicker()
        );

        // Save medicine
        saveButton.setOnClickListener(v ->
                saveMedicine()
        );

        // Load medicines
        loadMedicines();
    }

    // ---------------------------------------------------------
    // NOTIFICATION PERMISSION
    // ---------------------------------------------------------

    private void requestNotificationPermission() {

        if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.TIRAMISU) {

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{
                                Manifest.permission.POST_NOTIFICATIONS
                        },
                        NOTIFICATION_PERMISSION_CODE
                );
            }
        }
    }

    // ---------------------------------------------------------
    // TIME PICKER
    // ---------------------------------------------------------

    private void showTimePicker() {

        Calendar calendar = Calendar.getInstance();

        int currentHour =
                calendar.get(Calendar.HOUR_OF_DAY);

        int currentMinute =
                calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog =
                new TimePickerDialog(
                        this,
                        (view, hourOfDay, minute) -> {

                            selectedHour = hourOfDay;
                            selectedMinute = minute;

                            selectedTimeValue =
                                    String.format(
                                            "%02d:%02d",
                                            hourOfDay,
                                            minute
                                    );

                            selectedTime.setText(
                                    selectedTimeValue
                            );
                        },
                        currentHour,
                        currentMinute,
                        true
                );

        timePickerDialog.show();
    }

    // ---------------------------------------------------------
    // SAVE MEDICINE
    // ---------------------------------------------------------

    private void saveMedicine() {

        String name =
                medicineName.getText()
                        .toString()
                        .trim();

        String medicineDosage =
                dosage.getText()
                        .toString()
                        .trim();

        String frequency =
                frequencySpinner
                        .getSelectedItem()
                        .toString();

        // Validation
        if (name.isEmpty()) {

            medicineName.setError(
                    "Enter medicine name"
            );

            medicineName.requestFocus();

            return;
        }

        if (medicineDosage.isEmpty()) {

            dosage.setError(
                    "Enter dosage"
            );

            dosage.requestFocus();

            return;
        }

        if (selectedHour == -1 ||
                selectedMinute == -1 ||
                selectedTimeValue.isEmpty()) {

            Toast.makeText(
                    this,
                    "Please select medicine time",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (firebaseAuth.getCurrentUser() == null) {

            Toast.makeText(
                    this,
                    "Please login first",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String uid =
                firebaseAuth.getCurrentUser().getUid();

        DocumentReference documentReference =
                firestore
                        .collection("users")
                        .document(uid)
                        .collection("medicines")
                        .document();

        Medicine medicine =
                new Medicine(
                        documentReference.getId(),
                        name,
                        medicineDosage,
                        selectedTimeValue,
                        frequency
                );

        documentReference
                .set(medicine)
                .addOnSuccessListener(unused -> {

                    // Schedule alarm
                    scheduleMedicineReminder(
                            documentReference.getId(),
                            name,
                            medicineDosage,
                            selectedHour,
                            selectedMinute
                    );

                    Toast.makeText(
                            MedicineReminderActivity.this,
                            "Medicine saved and alarm set",
                            Toast.LENGTH_SHORT
                    ).show();

                    clearFields();

                    loadMedicines();
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            MedicineReminderActivity.this,
                            "Failed to save medicine: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    // ---------------------------------------------------------
    // SCHEDULE ALARM
    // ---------------------------------------------------------

    private void scheduleMedicineReminder(
            String medicineId,
            String name,
            String medicineDosage,
            int hour,
            int minute) {

        Calendar calendar =
                Calendar.getInstance();

        calendar.set(
                Calendar.HOUR_OF_DAY,
                hour
        );

        calendar.set(
                Calendar.MINUTE,
                minute
        );

        calendar.set(
                Calendar.SECOND,
                0
        );

        calendar.set(
                Calendar.MILLISECOND,
                0
        );

        // If selected time already passed,
        // schedule it for tomorrow.
        if (calendar.getTimeInMillis()
                <= System.currentTimeMillis()) {

            calendar.add(
                    Calendar.DAY_OF_YEAR,
                    1
            );
        }

        Intent intent =
                new Intent(
                        this,
                        MedicineReminderReceiver.class
                );

        intent.putExtra(
                "medicineName",
                name
        );

        intent.putExtra(
                "dosage",
                medicineDosage
        );

        intent.putExtra(
                "medicineId",
                medicineId
        );

        int requestCode =
                medicineId.hashCode();

        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(
                        this,
                        requestCode,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT |
                                PendingIntent.FLAG_IMMUTABLE
                );

        AlarmManager alarmManager =
                (AlarmManager)
                        getSystemService(
                                Context.ALARM_SERVICE
                        );

        if (alarmManager == null) {

            Toast.makeText(
                    this,
                    "Alarm service unavailable",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // Android 12+
        if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.S) {

            if (!alarmManager.canScheduleExactAlarms()) {

                Toast.makeText(
                        this,
                        "Please allow exact alarms for HealthTwin",
                        Toast.LENGTH_LONG
                ).show();

                Intent settingsIntent =
                        new Intent(
                                Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                        );

                startActivity(settingsIntent);

                return;
            }
        }

        // Set exact alarm
        if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.M) {

            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
            );

        } else {

            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    pendingIntent
            );
        }
    }

    // ---------------------------------------------------------
    // LOAD MEDICINES
    // ---------------------------------------------------------

    private void loadMedicines() {

        if (firebaseAuth.getCurrentUser() == null) {
            return;
        }

        String uid =
                firebaseAuth.getCurrentUser().getUid();

        firestore
                .collection("users")
                .document(uid)
                .collection("medicines")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    medicineList.clear();

                    for (var document :
                            queryDocumentSnapshots) {

                        Medicine medicine =
                                new Medicine(
                                        document.getId(),
                                        document.getString(
                                                "medicineName"
                                        ),
                                        document.getString(
                                                "dosage"
                                        ),
                                        document.getString(
                                                "time"
                                        ),
                                        document.getString(
                                                "frequency"
                                        )
                                );

                        medicineList.add(medicine);
                    }

                    medicineAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            this,
                            "Unable to load medicines",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }

    // ---------------------------------------------------------
    // DELETE MEDICINE
    // ---------------------------------------------------------

    private void deleteMedicine(
            Medicine medicine) {

        if (firebaseAuth.getCurrentUser() == null) {
            return;
        }

        String uid =
                firebaseAuth.getCurrentUser().getUid();

        cancelMedicineReminder(
                medicine.getId()
        );

        firestore
                .collection("users")
                .document(uid)
                .collection("medicines")
                .document(medicine.getId())
                .delete()
                .addOnSuccessListener(unused -> {

                    Toast.makeText(
                            this,
                            "Medicine deleted",
                            Toast.LENGTH_SHORT
                    ).show();

                    loadMedicines();
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            this,
                            "Unable to delete medicine",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }

    // ---------------------------------------------------------
    // CANCEL ALARM
    // ---------------------------------------------------------

    private void cancelMedicineReminder(
            String medicineId) {

        Intent intent =
                new Intent(
                        this,
                        MedicineReminderReceiver.class
                );

        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(
                        this,
                        medicineId.hashCode(),
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT |
                                PendingIntent.FLAG_IMMUTABLE
                );

        AlarmManager alarmManager =
                (AlarmManager)
                        getSystemService(
                                Context.ALARM_SERVICE
                        );

        if (alarmManager != null) {

            alarmManager.cancel(
                    pendingIntent
            );
        }

        pendingIntent.cancel();
    }

    // ---------------------------------------------------------
    // CLEAR FORM
    // ---------------------------------------------------------

    private void clearFields() {

        medicineName.setText("");
        dosage.setText("");

        selectedTimeValue = "";

        selectedHour = -1;
        selectedMinute = -1;

        selectedTime.setText(
                "Select Time"
        );

        frequencySpinner.setSelection(0);
    }
}