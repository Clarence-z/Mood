package com.example.demo.interceptor;

import com.example.demo.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Slf4j
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取请求方法和路径
        String method = request.getMethod();
        String path = request.getServletPath();

        // 放行OPTIONS预检请求
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }

        // 放行登录注册接口
        if (path.equals("/mood/user/login") || path.equals("/mood/user/register")) {
            return true;
        }

        // 检查token
        String token = request.getHeader("Authorization");
        System.out.println(token);
        if (token == null || !token.startsWith("Bearer ")) {
            System.out.println("无法读取token");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"code\": 401, \"message\": \"请先登录\"}");
            return false;
        }

        token = token.substring(7); // 去掉"Bearer "

        if (!jwtUtil.validateToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"code\": 401, \"message\": \"Token无效或已过期\"}");
            return false;
        }

        // 将用户ID存入request
        try {
            String userId = jwtUtil.getUserIdFromToken(token);
            request.setAttribute("userId", Long.valueOf(userId));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"code\": 401, \"message\": \"Token解析失败\"}");
            return false;
        }

        return true;
    }
}