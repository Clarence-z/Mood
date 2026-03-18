package com.example.demo.service;

import com.example.demo.entity.DifyChatRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// 用于调用API获取回复，仅字符串处理，不涉及实体
@Service
public class ChatService {

    @Autowired
    private WebClient difyWebClient;

    // 调用Dify API获取AI回复
    public String callDifyAPI(String message) {
        try {
            // 构建Dify请求
            DifyChatRequest difyRequest = new DifyChatRequest();
            difyRequest.setInputs(new HashMap<>());
            difyRequest.setQuery(message);
            difyRequest.setResponse_mode("blocking");
            difyRequest.setConversation_id("");
            difyRequest.setUser("user-" + System.currentTimeMillis());
            difyRequest.setFiles(new ArrayList<>());

            System.out.println("发送给Dify的请求: " + difyRequest);

            // 同步调用Dify API
            Map<String, Object> difyResponse = difyWebClient.post()
                    .bodyValue(difyRequest)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(50))
                    .block(); // 阻塞直到获取响应

            if (difyResponse != null) {
                // 提取并清理回答内容
                String rawAnswer = (String) difyResponse.get("answer");
                String cleanAnswer = extractCleanAnswer(rawAnswer);

                System.out.println("原始回答: " + rawAnswer);
                System.out.println("清理后回答: " + cleanAnswer);

                return cleanAnswer;
            } else {
                throw new RuntimeException("Dify API返回空响应");
            }

        } catch (Exception e) {
            System.err.println("调用Dify API失败: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("AI服务暂时不可用: " + e.getMessage());
        }
    }

    // 从原始回答中提取干净的正文
    private String extractCleanAnswer(String rawAnswer) {
        if (rawAnswer == null || rawAnswer.trim().isEmpty()) {
            return "抱歉，AI没有生成有效回答。";
        }

        // 方法1: 使用正则表达式移除思考部分
        String cleanAnswer = rawAnswer.replaceAll("(?s)<think>.*?<\\/think>", "").trim();

        // 如果正则没匹配到，尝试其他方法
        if (cleanAnswer.equals(rawAnswer) || cleanAnswer.isEmpty()) {
            // 方法2: 查找思考标记并截取
            int thinkEnd = rawAnswer.indexOf("</think>");
            if (thinkEnd != -1) {
                cleanAnswer = rawAnswer.substring(thinkEnd + 8).trim(); // 8是</think>的长度
            } else {
                // 方法3: 如果没有明显标记，返回原文
                cleanAnswer = rawAnswer;
            }
        }

        // 清理多余的空白和换行
        cleanAnswer = cleanAnswer.replaceAll("\\n\\s*\\n", "\n\n").trim();

        // 如果清理后为空，返回原始内容
        if (cleanAnswer.isEmpty()) {
            return rawAnswer;
        }

        return cleanAnswer;
    }

    public String generateTitle(String userMessage) {
        return userMessage.length() > 20 ?
                userMessage.substring(0, 20) + "..." : userMessage;
    }
}
