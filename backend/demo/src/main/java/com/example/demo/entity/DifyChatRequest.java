package com.example.demo.entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DifyChatRequest {
    private Object inputs;
    private String query;
    private String response_mode;
    private String conversation_id;
    private String user;
    private List<Object> files;
}