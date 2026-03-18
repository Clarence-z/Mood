package com.example.demo.controller;

import com.example.demo.dto.response.chat.ChatResponse;
import com.example.demo.dto.response.chat.ConversationDTO;
import com.example.demo.dto.response.chat.MessageDTO;
import com.example.demo.dto.request.chat.ChatRequest;
import com.example.demo.entity.Conversation;
import com.example.demo.entity.Message;
import com.example.demo.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mood/ai")
public class ChatController {
    @Autowired
    private ConversationService conversationService;

    @Autowired
    private MessageService messageService;

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserStatisticsService userStatisticsService;

    // 聊天、新对话
    @PostMapping("/chat")
    public ChatResponse chat(
            @Valid @RequestBody ChatRequest request,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        String userMessage = request.getMessage();
        Long conversationId = request.getConversationId();

        // 如果没有对话ID，创建新对话
        if (conversationId == null) {
            String title = chatService.generateTitle(userMessage);
            ConversationDTO dto = conversationService.createConversation(title, userId);
            conversationId = dto.getId();
        }

        // 保存用户消息
        MessageDTO userMsg = messageService.addUserMessage(conversationId, userMessage);
        userStatisticsService.incrementAiMessageTotal(userId);

        // 调用AI服务
        String aiResponse = chatService.callDifyAPI(userMessage);

        // 保存AI回复
        MessageDTO aiMsg = messageService.addAIMessage(conversationId, aiResponse);

        // 构建响应
        ChatResponse response = new ChatResponse();
        response.setConversationId(conversationId);
        response.setAnswer(aiResponse);
        response.setUserMessage(userMsg);
        response.setAiMessage(aiMsg);


        return response;
    }

}
