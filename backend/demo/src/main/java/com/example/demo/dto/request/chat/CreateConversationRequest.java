package com.example.demo.dto.request.chat;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateConversationRequest {
    @NotBlank
    private String title;
}
