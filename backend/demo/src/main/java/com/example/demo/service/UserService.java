package com.example.demo.service;

import com.example.demo.dto.response.user.UserDTO;
import com.example.demo.entity.User;
import com.example.demo.mapper.UserMapper;
import com.example.demo.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional  // ->异常时事务会回滚
@Slf4j
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 用户注册
    public Map<String, Object> register(String username, String password, String nickname, String email) {
        // 检查用户名是否已存在
        if (userMapper.selectByUsername(username) != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (email != null && !email.trim().isEmpty() && userMapper.selectByEmail(email) != null) {
            throw new RuntimeException("邮箱已被注册");
        }

        // 创建用户
        User user = new User();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setNickname(nickname != null ? nickname : username);
        user.setEmail(email);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userMapper.insert(user);

        // 生成token
        String token = jwtUtil.generateToken(user.getId().toString(), user.getUsername());

        Map<String, Object> result = new HashMap<>();
        result.put("user", UserDTO.fromEntity(user));
        result.put("token", token);

        log.info("用户注册成功: {}", username);
        return result;
    }

    // 用户登录
    public Map<String, Object> login(String username, String password) {
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在或密码错误");
        }

        // 验证密码
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new RuntimeException("用户不存在或密码错误");
        }

        // 更新最后登录时间
        user.setLastLoginAt(LocalDateTime.now());
        userMapper.updateById(user);

        // 生成token
        String token = jwtUtil.generateToken(user.getId().toString(), user.getUsername());

        Map<String, Object> result = new HashMap<>();
        result.put("user", UserDTO.fromEntity(user));
        result.put("token", token);

        log.info("用户登录成功: {}", username);
        return result;
    }

    // 获取当前用户信息
    public UserDTO getCurrentUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        return UserDTO.fromEntity(user);
    }

    // 更新用户信息
    public UserDTO updateUser(Long userId, String nickname, String bio, String themeId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (nickname != null) user.setNickname(nickname);
        if (bio != null) user.setBio(bio);
        if (themeId != null) {
            try {
                user.setThemeId(Integer.valueOf(themeId));
            } catch (NumberFormatException e) {
                throw new RuntimeException("主题ID格式错误");
            }
        }
        user.setUpdatedAt(LocalDateTime.now());

        userMapper.updateById(user);
        return UserDTO.fromEntity(user);
    }

    // 更新用户头像 (Base64)
    public UserDTO updateAvatar(Long userId, String avatarData) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 可选：简单的Base64数据格式验证
        if (avatarData == null || !avatarData.startsWith("data:image")) {
            // 更严谨的做法是使用正则表达式验证完整的Base64数据URL格式
            throw new RuntimeException("无效的头像数据格式");
        }

        int base64Length = avatarData.length();
        // 粗略估算原始图片的二进制大小（字节）
        int approximateOriginalSize = (base64Length * 3) / 4;

        // 限制原始图片大小不超过 2MB (2 * 1024 * 1024)。
        // MEDIUMTEXT 支持约16MB字符，2MB的图片转Base64后约2.6MB，远远低于容量上限，非常安全。
        int maxAllowedOriginalSize = 2 * 1024 * 1024; // 2MB

        if (approximateOriginalSize > maxAllowedOriginalSize) {
            throw new RuntimeException(String.format("图片过大。请上传小于 %dMB 的图片。", maxAllowedOriginalSize / (1024 * 1024)));
        }

        user.setAvatar(avatarData);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);

        log.info("用户头像更新成功: {}", user.getUsername());
        return UserDTO.fromEntity(user);
    }

    // 修改密码
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new RuntimeException("原密码错误");
        }

        // 加密新密码
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());

        userMapper.updateById(user);
        log.info("用户密码修改成功: {}", user.getUsername());
    }
}