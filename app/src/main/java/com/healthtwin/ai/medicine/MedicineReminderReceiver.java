package com.healthtwin.ai.medicine;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.healthtwin.ai.R;

public class MedicineReminderReceiver extends BroadcastReceiver {

    private static final String TAG = "MedicineAlarm";

    private static final String CHANNEL_ID =
            "medicine_alarm_channel";

    @Override
    public void onReceive(
            Context context,
            Intent intent) {

        Log.d(
                TAG,
                "========== MEDICINE ALARM RECEIVED =========="
        );

        String medicineName =
                intent.getStringExtra("medicineName");

        String dosage =
                intent.getStringExtra("dosage");

        if (medicineName == null ||
                medicineName.isEmpty()) {

            medicineName = "Medicine";
        }

        if (dosage == null) {
            dosage = "";
        }

        createNotificationChannel(context);

        // Open full-screen alarm activity
        Intent alarmIntent =
                new Intent(
                        context,
                        MedicineAlarmActivity.class
                );

        alarmIntent.putExtra(
                "medicineName",
                medicineName
        );

        alarmIntent.putExtra(
                "dosage",
                dosage
        );

        alarmIntent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
        );

        PendingIntent fullScreenPendingIntent =
                PendingIntent.getActivity(
                        context,
                        (int) System.currentTimeMillis(),
                        alarmIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT |
                                PendingIntent.FLAG_IMMUTABLE
                );

        Uri alarmSound =
                RingtoneManager.getDefaultUri(
                        RingtoneManager.TYPE_ALARM
                );

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(
                        context,
                        CHANNEL_ID
                );

        builder
                .setSmallIcon(
                        R.mipmap.ic_launcher
                )
                .setContentTitle(
                        "Medicine Time 💊"
                )
                .setContentText(
                        medicineName +
                                (dosage.isEmpty()
                                        ? ""
                                        : " - " + dosage)
                )
                .setPriority(
                        NotificationCompat.PRIORITY_MAX
                )
                .setCategory(
                        NotificationCompat.CATEGORY_ALARM
                )
                .setAutoCancel(false)
                .setOngoing(true)
                .setSound(alarmSound)
                .setFullScreenIntent(
                        fullScreenPendingIntent,
                        true
                );

        NotificationManager notificationManager =
                (NotificationManager)
                        context.getSystemService(
                                Context.NOTIFICATION_SERVICE
                        );

        if (notificationManager != null) {

            notificationManager.notify(
                    (int) System.currentTimeMillis(),
                    builder.build()
            );

            Log.d(
                    TAG,
                    "Medicine alarm notification created"
            );
        }
    }

    private void createNotificationChannel(
            Context context) {

        if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.O) {

            Uri alarmSound =
                    RingtoneManager.getDefaultUri(
                            RingtoneManager.TYPE_ALARM
                    );

            NotificationChannel channel =
                    new NotificationChannel(
                            CHANNEL_ID,
                            "Medicine Alarms",
                            NotificationManager.IMPORTANCE_HIGH
                    );

            channel.setDescription(
                    "HealthTwin medicine reminders"
            );

            channel.enableVibration(true);

            channel.setVibrationPattern(
                    new long[]{
                            0,
                            1000,
                            500,
                            1000,
                            500
                    }
            );

            if (alarmSound != null) {

                android.media.AudioAttributes
                        audioAttributes =
                        new android.media.AudioAttributes.Builder()
                                .setUsage(
                                        android.media.AudioAttributes.USAGE_ALARM
                                )
                                .setContentType(
                                        android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION
                                )
                                .build();

                channel.setSound(
                        alarmSound,
                        audioAttributes
                );
            }

            NotificationManager notificationManager =
                    (NotificationManager)
                            context.getSystemService(
                                    Context.NOTIFICATION_SERVICE
                            );

            if (notificationManager != null) {

                notificationManager.createNotificationChannel(
                        channel
                );
            }
        }
    }
}

