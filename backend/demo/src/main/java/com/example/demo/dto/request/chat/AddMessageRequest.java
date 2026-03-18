package com.example.demo.dto.request.chat;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddMessageRequest {
    @NotBlank
    private String role; // "user" or "assistant"
    @NotBlank
    private String content;
}
