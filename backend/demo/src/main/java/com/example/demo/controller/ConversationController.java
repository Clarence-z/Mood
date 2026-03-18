package com.example.demo.controller;

import com.example.demo.dto.request.chat.CreateConversationRequest;
import com.example.demo.dto.request.chat.UpdateConversationRequest;
import com.example.demo.dto.response.chat.ConversationDTO;
import com.example.demo.entity.Conversation;
import com.example.demo.service.ConversationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/mood/ai/conversations")
@Validated
public class ConversationController {

    @Autowired
    private ConversationService conversationService;

    // 获取对话列表（边栏展示）
    @GetMapping
    public List<ConversationDTO> getConversations(HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        List<ConversationDTO> dtos = conversationService.getConversationList(userId);
        return dtos;
    }

    // 新建对话 - 返回201 created
    @PostMapping
    public ResponseEntity<ConversationDTO> createConversation(
            @Valid @RequestBody CreateConversationRequest request,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        ConversationDTO dto = conversationService.createConversation(request.getTitle(), userId);
//        System.out.println("controller");
//        System.out.println(dto);

        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    // 打开对话
    @GetMapping("/{conversationId}")
    public ConversationDTO getConversation(
            @PathVariable @Min(1) Long conversationId,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        ConversationDTO dto = conversationService.getConversationById(conversationId, userId);
        return dto;
    }

    // 重命名对话
    @PutMapping("/{conversationId}")
    public ConversationDTO updateConversation(
            @PathVariable @Min(1) Long conversationId,
            @Valid @RequestBody UpdateConversationRequest request,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        ConversationDTO dto = conversationService.updateConversation(
                conversationId, userId, request.getTitle());
        return dto;
    }

    // 删除对话 - 返回204 No Content
    @DeleteMapping("/{conversationId}")
    public ResponseEntity<Void> deleteConversation(
            @PathVariable @Min(1) Long conversationId,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        conversationService.deleteConversation(conversationId, userId);
        return ResponseEntity.noContent().build();
    }
}