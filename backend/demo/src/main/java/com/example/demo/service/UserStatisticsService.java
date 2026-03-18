package com.example.demo.service;

import com.example.demo.entity.UserStatistics;
import com.example.demo.mapper.UserStatisticsMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
@Transactional
public class UserStatisticsService {

    @Autowired
    private UserStatisticsMapper userStatisticsMapper;

    // 获取或创建用户统计记录
    public UserStatistics getOrCreateStatistics(Long userId) {
        UserStatistics stats = userStatisticsMapper.selectByUserId(userId);
        if (stats == null) {
            // 初始化用户统计记录
            int created = userStatisticsMapper.initUserStatistics(userId);
            if (created > 0) {
                stats = userStatisticsMapper.selectByUserId(userId);
            }
            log.info("为用户 {} 初始化统计记录", userId);
        }
        return stats;
    }

    // 更新登录统计
    public void updateLoginStats(Long userId) {
        UserStatistics stats = getOrCreateStatistics(userId);
        LocalDate today = LocalDate.now();

        // 检查是否需要更新（避免一天内多次登录重复计算）
        if (stats.getLastLoginDate() == null || !stats.getLastLoginDate().equals(today)) {
            // 计算连续登录天数
            if (stats.getLastLoginDate() == null) {
                // 首次登录
                stats.setCurrentStreak(1);
            } else if (stats.getLastLoginDate().equals(today.minusDays(1))) {
                // 上次登录是昨天，连续登录天数+1
                int newStreak = stats.getCurrentStreak() + 1;
                stats.setCurrentStreak(newStreak);
            } else if (stats.getLastLoginDate().isBefore(today.minusDays(1))) {
                // 登录中断，重置连续登录天数为1
                stats.setCurrentStreak(1);
            }
            // 如果上次登录就是今天，不更新连续登录天数

            // 更新累计登录天数（如果上次登录不是今天）
            if (!today.equals(stats.getLastLoginDate())) {
                stats.setTotalLoginDays(stats.getTotalLoginDays() + 1);
            }

            // 更新最后登录日期
            stats.setLastLoginDate(today);

            // 保存到数据库
            userStatisticsMapper.updateById(stats);
            log.debug("更新用户 {} 的登录统计: streak={}, totalDays={}",
                    userId, stats.getCurrentStreak(), stats.getTotalLoginDays());
        }
    }

    // 增加日记计数
    public void incrementDiaryCount(Long userId) {
        int affected = userStatisticsMapper.incrementDiaryCount(userId);
        if (affected == 0) {
            // 如果记录不存在，先创建再增加
            getOrCreateStatistics(userId);
            userStatisticsMapper.incrementDiaryCount(userId);
        }
        log.debug("增加用户 {} 的日记计数", userId);
    }

    // 增加已完成任务数
    public void incrementTodoCompleted(Long userId) {
        int affected = userStatisticsMapper.incrementTodoCompleted(userId);
        if (affected == 0) {
            getOrCreateStatistics(userId);
            userStatisticsMapper.incrementTodoCompleted(userId);
        }
        log.debug("增加用户 {} 的已完成任务数", userId);
    }

    // 减少已完成任务数
    public void decrementTodoCompleted(Long userId) {
        int affected = userStatisticsMapper.decrementTodoCompleted(userId);
        if (affected == 0) {
            getOrCreateStatistics(userId);
            userStatisticsMapper.decrementTodoCompleted(userId);
        }
        log.debug("减少用户 {} 的已完成任务数", userId);
    }

    // 增加AI消息总数
    public void incrementAiMessageTotal(Long userId) {
        int affected = userStatisticsMapper.incrementAiMessageTotal(userId);
        if (affected == 0) {
            getOrCreateStatistics(userId);
            userStatisticsMapper.incrementAiMessageTotal(userId);
        }
        log.debug("增加用户 {} 的AI消息总数", userId);
    }

    // 获取用户统计数据
    public UserStatistics getStatistics(Long userId) {
        return getOrCreateStatistics(userId);
    }

    // 为成就系统提供的数据查询方法
    public int getStatisticValue(Long userId, String statType) {
        UserStatistics stats = getStatistics(userId);

        switch (statType) {
            case "DIARY_COUNT":
                return stats.getDiaryCount() != null ? stats.getDiaryCount() : 0;
            case "TODO_COMPLETED":
                return stats.getTodoCompleted() != null ? stats.getTodoCompleted() : 0;
            case "AI_MESSAGE_TOTAL":
                return stats.getAiMessageTotal() != null ? stats.getAiMessageTotal() : 0;
            case "TOTAL_LOGIN_DAYS":
                return stats.getTotalLoginDays() != null ? stats.getTotalLoginDays() : 0;
            case "CURRENT_STREAK":
                return stats.getCurrentStreak() != null ? stats.getCurrentStreak() : 0;
            default:
                return 0;
        }
    }
}