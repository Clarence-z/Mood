package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.entity.Message;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface MessageMapper extends BaseMapper<Message> {
    // 查询对话的所有消息（按顺序）
    @Select("SELECT * FROM messages WHERE conversation_id = #{conversationId} ORDER BY message_order")
    List<Message> findByConversationIdOrderByOrder(Long conversationId);

    // 查询对话的最后一条消息
    @Select("SELECT * FROM messages WHERE conversation_id = #{conversationId} ORDER BY message_order DESC LIMIT 1")
    Message findLastMessageByConversationId(Long conversationId);
}
