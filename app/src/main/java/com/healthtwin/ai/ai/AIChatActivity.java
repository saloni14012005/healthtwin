package com.healthtwin.ai.ai;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.healthtwin.ai.R;

import java.util.ArrayList;
import java.util.List;

public class AIChatActivity extends AppCompatActivity {

    private RecyclerView recyclerChat;
    private EditText etMessage;
    private ImageButton btnSend;
    private ImageButton btnBack;
    private ProgressBar progressBar;

    private ChatAdapter chatAdapter;
    private List<ChatMessage> messageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_ai_chat);

        recyclerChat = findViewById(R.id.recyclerChat);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.progressBar);

        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Message list
        messageList = new ArrayList<>();

        chatAdapter = new ChatAdapter(
                this,
                messageList
        );

        recyclerChat.setLayoutManager(
                new LinearLayoutManager(this)
        );

        recyclerChat.setAdapter(chatAdapter);

        // Send button
        btnSend.setOnClickListener(
                v -> sendMessage()
        );

        // First AI message
        addAIMessage(
                "Hello! 👋\n\n" +
                        "I'm HealthTwin AI, your personal health assistant.\n\n" +
                        "You can ask me about general health, " +
                        "nutrition, fitness, BMI and healthy lifestyle."
        );
    }

    // ============================================================
    // SEND MESSAGE
    // ============================================================

    private void sendMessage() {

        String message = etMessage
                .getText()
                .toString()
                .trim();

        if (message.isEmpty()) {

            Toast.makeText(
                    this,
                    "Please enter a message",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // Add user message
        addUserMessage(message);

        // Clear input
        etMessage.setText("");

        // Show loading
        progressBar.setVisibility(
                ProgressBar.VISIBLE
        );

        btnSend.setEnabled(false);

        // Temporary AI response
        new Handler(
                Looper.getMainLooper()
        ).postDelayed(() -> {

            progressBar.setVisibility(
                    ProgressBar.GONE
            );

            btnSend.setEnabled(true);

            String response =
                    generateResponse(message);

            addAIMessage(response);

        }, 1200);
    }

    // ============================================================
    // USER MESSAGE
    // ============================================================

    private void addUserMessage(String message) {

        messageList.add(
                new ChatMessage(
                        message,
                        true
                )
        );

        chatAdapter.notifyItemInserted(
                messageList.size() - 1
        );

        recyclerChat.smoothScrollToPosition(
                messageList.size() - 1
        );
    }

    // ============================================================
    // AI MESSAGE
    // ============================================================

    private void addAIMessage(String message) {

        messageList.add(
                new ChatMessage(
                        message,
                        false
                )
        );

        chatAdapter.notifyItemInserted(
                messageList.size() - 1
        );

        recyclerChat.smoothScrollToPosition(
                messageList.size() - 1
        );
    }

    // ============================================================
    // TEMPORARY AI RESPONSE
    // ============================================================

    private String generateResponse(
            String userMessage
    ) {

        String message =
                userMessage.toLowerCase();

        if (message.contains("hello") ||
                message.contains("hi") ||
                message.contains("hey")) {

            return "Hello! 👋 How can I help you with your health today?";
        }

        if (message.contains("bmi")) {

            return "BMI stands for Body Mass Index. " +
                    "It is calculated using your weight and height. " +
                    "You can use the BMI Calculator available in HealthTwin.";
        }

        if (message.contains("fever")) {

            return "Fever can occur due to infections or other causes. " +
                    "Stay hydrated and take adequate rest. " +
                    "If the fever is severe or persistent, consult a healthcare professional.";
        }

        if (message.contains("headache")) {

            return "Headaches can have different causes such as stress, " +
                    "dehydration or lack of sleep. Rest and stay hydrated. " +
                    "If the headache is severe or persistent, seek medical advice.";
        }

        if (message.contains("water") ||
                message.contains("hydration")) {

            return "Staying hydrated is important for your health. " +
                    "Drink water regularly throughout the day and increase " +
                    "your fluid intake during exercise or hot weather.";
        }

        if (message.contains("medicine")) {

            return "I can provide general information about medicines. " +
                    "However, do not start, stop or change a medicine " +
                    "without consulting a qualified healthcare professional.";
        }

        if (message.contains("diet") ||
                message.contains("food")) {

            return "A balanced diet can include vegetables, fruits, " +
                    "whole grains, protein sources and adequate fluids. " +
                    "Individual nutritional needs can vary.";
        }
        if (message.contains("nutrition") ||
                message.contains("nutrient") ||
                message.contains("nutritious") ||
                message.contains("healthy food") ||
                message.contains("healthy diet") ||
                message.contains("diet") ||
                message.contains("food") ||
                message.contains("protein") ||
                message.contains("vitamin") ||
                message.contains("mineral") ||
                message.contains("calorie") ||
                message.contains("calories") ||
                message.contains("carbohydrate") ||
                message.contains("carbs") ||
                message.contains("fiber") ||
                message.contains("fat")) {

            return "Good nutrition is important for maintaining overall health. " +
                    "A balanced diet can include vegetables, fruits, whole grains, " +
                    "protein sources such as pulses, eggs, dairy or lean meat, " +
                    "and healthy fats. Stay hydrated and try to limit highly " +
                    "processed foods, excess sugar and excessive salt.";
        }

        return "I can help with general health information, BMI, " +
                "nutrition, fitness, medicines and healthy lifestyle questions.\n\n" +
                "For diagnosis or treatment decisions, please consult " +
                "a qualified healthcare professional.";
    }
}