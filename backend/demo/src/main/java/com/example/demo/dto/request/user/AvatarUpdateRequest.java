package com.example.demo.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AvatarUpdateRequest {
    @NotBlank(message = "头像数据不能为空")
    private String avatarData; // Base64字符串，例如 "data:image/png;base64,iVBORw0KGgo..."
}
