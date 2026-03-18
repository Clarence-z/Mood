package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.dto.response.user.AchievementDTO;
import com.example.demo.dto.response.user.UserStatisticsDTO;
import com.example.demo.entity.UserStatistics;
import com.example.demo.service.AchievementService;
import com.example.demo.service.UserStatisticsService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/mood/achievements")
public class AchievementController {
    @Autowired
    private AchievementService achievementService;
    @Autowired
    private UserStatisticsService userStatisticsService;

    @GetMapping
    public Result<List<AchievementDTO>> getOrderedAchievements(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        userStatisticsService.updateLoginStats(userId);

        List<AchievementDTO> orderedList = achievementService.getOrderedAchievements(userId);
        return Result.success(orderedList);
    }
}