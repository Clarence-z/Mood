package com.example.demo.dto.response.chat;

import lombok.Data;

@Data
public class ChatResponse {
    private Long conversationId;
    private String answer;
    private MessageDTO userMessage;
    private MessageDTO aiMessage;
}
