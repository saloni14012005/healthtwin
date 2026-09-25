package com.healthtwin.ai.ai;

public class ChatMessage {

    private final String message;
    private final boolean userMessage;

    public ChatMessage(String message, boolean userMessage) {
        this.message = message;
        this.userMessage = userMessage;
    }

    public String getMessage() {
        return message;
    }

    public boolean isUserMessage() {
        return userMessage;
    }
}