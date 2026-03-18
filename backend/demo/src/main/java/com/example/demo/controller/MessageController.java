package com.example.demo.controller;

import com.example.demo.dto.request.chat.AddMessageRequest;
import com.example.demo.dto.response.chat.MessageDTO;
import com.example.demo.entity.Message;
import com.example.demo.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/mood/ai/conversations/{conversationId}/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    // 展示聊天记录
    @GetMapping
    public List<MessageDTO> getMessages(@PathVariable Long conversationId) {
        return messageService.getConversationHistory(conversationId);
    }
}