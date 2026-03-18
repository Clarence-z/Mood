package com.example.demo.dto.request.user;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {
    @Size(max = 50, message = "昵称最多50字符")
    private String nickname;

    @Size(max = 200, message = "个人简介最多200字符")
    private String bio;

    private String themeId;
}
