package com.assignment.androidshoppingapp.Helper;

import java.util.List;

public class ChatRequest {
    private String model = "gpt-4";
    private List<Message> messages;

    public ChatRequest(List<Message> messages) {
        this.messages = messages;
    }
}
