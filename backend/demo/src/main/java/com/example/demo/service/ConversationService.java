package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.dto.response.chat.ConversationDTO;
import com.example.demo.entity.Conversation;
import com.example.demo.entity.Message;
import com.example.demo.mapper.ConversationMapper;
import com.example.demo.mapper.MessageMapper;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

// 用于管理对话
@Service
@Transactional
public class ConversationService {

    @Autowired
    private ConversationMapper conversationMapper;

    // 获取对话列表 - 返回DTO List
    public List<ConversationDTO> getConversationList(Long userId) {
        QueryWrapper<Conversation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId) // 添加用户过滤
                .orderByDesc("updated_at");
        List<Conversation> records = conversationMapper.selectList(queryWrapper);

        return ConversationDTO.fromEntities(records);
    }

    // 创建对话 - Entity->DTO
    public ConversationDTO createConversation(String title, Long userId) {
        Conversation conversation = new Conversation();
        conversation.setTitle(title);
        conversation.setUserId(userId);
        conversation.setCreatedAt(new Date());  // 手动设置
        conversation.setUpdatedAt(new Date());  // 手动设置
        conversationMapper.insert(conversation);
//        System.out.println("service");
//        System.out.println(conversation);
        return ConversationDTO.fromEntity(conversation);
    }

    // 获取单个对话
    public ConversationDTO getConversationById(Long conversationId, Long userId) {
        Conversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null) {
            throw new RuntimeException("对话不存在");
        }
        if (!conversation.getUserId().equals(userId)) {
            throw new RuntimeException("无权访问此对话");
        }
        return ConversationDTO.fromEntity(conversation);
    }

    // 更新对话标题
    public ConversationDTO updateConversation(Long conversationId, Long userId, String title) {
        Conversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null) {
            throw new RuntimeException("对话不存在");
        }
        if (!conversation.getUserId().equals(userId)) {
            throw new RuntimeException("无权访问此对话");
        }
        conversation.setTitle(title);
        conversation.setUpdatedAt(new Date());
        conversationMapper.updateById(conversation);
        return ConversationDTO.fromEntity(conversation);
    }

    // 删除对话
    public void deleteConversation(Long conversationId, Long userId) {
        Conversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null) {
            throw new RuntimeException("对话不存在");
        }
        if (!conversation.getUserId().equals(userId)) {
            throw new RuntimeException("无权访问此对话");
        }
        conversationMapper.deleteById(conversationId);
    }
}