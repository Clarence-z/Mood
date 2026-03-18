package com.example.demo.dto.response.chat;

import com.example.demo.entity.Message;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class MessageDTO {
    private String role;
    private String content;
    private Date createdAt;
    private Integer messageOrder;

    public static MessageDTO fromEntity(Message message) {
        MessageDTO dto = new MessageDTO();
        dto.setRole(message.getRole());
        dto.setContent(message.getContent());
        dto.setCreatedAt(message.getCreatedAt());
        dto.setMessageOrder(message.getMessageOrder());
        return dto;
    }

    public static List<MessageDTO> fromEntities(List<Message> messages) {
        return messages.stream()
                .map(MessageDTO::fromEntity)
                .collect(Collectors.toList());
    }
}
