package com.healthtwin.ai.medicine;

public class Medicine {

    private String id;
    private String medicineName;
    private String dosage;
    private String time;
    private String frequency;

    public Medicine() {
        // Required empty constructor for Firestore
    }

    public Medicine(
            String id,
            String medicineName,
            String dosage,
            String time,
            String frequency) {

        this.id = id;
        this.medicineName = medicineName;
        this.dosage = dosage;
        this.time = time;
        this.frequency = frequency;
    }

    public String getId() {
        return id;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public String getDosage() {
        return dosage;
    }

    public String getTime() {
        return time;
    }

    public String getFrequency() {
        return frequency;
    }
}