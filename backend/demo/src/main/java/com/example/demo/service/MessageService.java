package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.dto.response.chat.MessageDTO;
import com.example.demo.entity.Conversation;
import com.example.demo.entity.Message;
import com.example.demo.mapper.ConversationMapper;
import com.example.demo.mapper.MessageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

// 用于加载历史消息
@Service
@Transactional
public class MessageService {

    @Autowired
    private ConversationMapper conversationMapper;

    @Autowired
    private MessageMapper messageMapper;

    // 获取历史信息
    public List<MessageDTO> getConversationHistory(Long conversationId) {
        // 验证对话存在
        Conversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null) {
            throw new RuntimeException("对话不存在");
        }

        // 查询消息，按顺序排序
        QueryWrapper<Message> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("conversation_id", conversationId)
                .orderByAsc("message_order");

        return MessageDTO.fromEntities(messageMapper.selectList(queryWrapper));
    }

    // 记录信息
    private MessageDTO addMessage(Long conversationId, String role, String content) {
        Conversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null) {
            throw new RuntimeException("对话不存在");
        }

        int nextOrder = conversation.getMessageCount() + 1;

        Message message = new Message();
        message.setConversationId(conversationId);
        message.setRole(role);
        message.setContent(content);
        message.setMessageOrder(nextOrder);
        message.setCreatedAt(new Date());

        messageMapper.insert(message);

        conversation.setMessageCount(nextOrder);
        conversation.setUpdatedAt(new Date());
        conversationMapper.updateById(conversation);

        return MessageDTO.fromEntity(message);
    }

    // 添加用户消息
    public MessageDTO addUserMessage(Long conversationId, String content) {
        return addMessage(conversationId, "user", content);
    }

    // 添加AI消息
    public MessageDTO addAIMessage(Long conversationId, String content) {
        return addMessage(conversationId, "assistant", content);
    }
}
