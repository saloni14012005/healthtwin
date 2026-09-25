package com.healthtwin.ai.medicine;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.healthtwin.ai.R;

public class MedicineAlarmActivity
        extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;

    private TextView tvAlarmMedicine;
    private TextView tvAlarmDosage;
    private Button btnStopAlarm;

    @Override
    protected void onCreate(
            Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Show activity over lock screen
        if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.O_MR1) {

            setShowWhenLocked(true);
            setTurnScreenOn(true);
        }

        setContentView(
                R.layout.activity_medicine_alarm
        );

        tvAlarmMedicine =
                findViewById(
                        R.id.tvAlarmMedicine
                );

        tvAlarmDosage =
                findViewById(
                        R.id.tvAlarmDosage
                );

        btnStopAlarm =
                findViewById(
                        R.id.btnStopAlarm
                );

        String medicineName =
                getIntent().getStringExtra(
                        "medicineName"
                );

        String dosage =
                getIntent().getStringExtra(
                        "dosage"
                );

        if (medicineName == null) {
            medicineName = "Medicine";
        }

        if (dosage == null) {
            dosage = "";
        }

        tvAlarmMedicine.setText(
                medicineName
        );

        tvAlarmDosage.setText(
                dosage
        );

        // Start alarm sound
        startAlarmSound();

        // Start vibration
        startVibration();

        // Stop button
        btnStopAlarm.setOnClickListener(v -> {

            stopAlarm();

            finish();
        });
    }

    // ---------------------------------------------------------
    // START SOUND
    // ---------------------------------------------------------

    private void startAlarmSound() {

        try {

            mediaPlayer =
                    MediaPlayer.create(
                            this,
                            R.raw.medicine_alarm
                    );

            if (mediaPlayer != null) {

                mediaPlayer.setLooping(true);

                mediaPlayer.setVolume(
                        1.0f,
                        1.0f
                );

                mediaPlayer.start();
            }

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    // ---------------------------------------------------------
    // VIBRATION
    // ---------------------------------------------------------

    private void startVibration() {

        if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.S) {

            VibratorManager vibratorManager =
                    (VibratorManager)
                            getSystemService(
                                    Context.VIBRATOR_MANAGER_SERVICE
                            );

            if (vibratorManager != null) {

                vibrator =
                        vibratorManager.getDefaultVibrator();
            }

        } else {

            vibrator =
                    (Vibrator)
                            getSystemService(
                                    Context.VIBRATOR_SERVICE
                            );
        }

        if (vibrator == null) {
            return;
        }

        if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.O) {

            vibrator.vibrate(
                    VibrationEffect.createWaveform(
                            new long[]{
                                    0,
                                    1000,
                                    500,
                                    1000,
                                    500
                            },
                            0
                    )
            );

        } else {

            vibrator.vibrate(
                    new long[]{
                            0,
                            1000,
                            500,
                            1000,
                            500
                    },
                    0
            );
        }
    }

    // ---------------------------------------------------------
    // STOP ALARM
    // ---------------------------------------------------------

    private void stopAlarm() {

        if (mediaPlayer != null) {

            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }

            mediaPlayer.release();
            mediaPlayer = null;
        }

        if (vibrator != null) {
            vibrator.cancel();
        }
    }

    @Override
    protected void onDestroy() {

        stopAlarm();

        super.onDestroy();
    }
}