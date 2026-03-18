package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.dto.response.user.AchievementDTO;
import com.example.demo.entity.Achievement;
import com.example.demo.entity.UserAchievement;
import com.example.demo.entity.UserStatistics;
import com.example.demo.mapper.AchievementMapper;
import com.example.demo.mapper.UserAchievementMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AchievementService {

    private final AchievementMapper achievementMapper;
    private final UserAchievementMapper userAchievementMapper;
    private final UserStatisticsService userStatisticsService;

    // 检查并授予成就
    @Transactional
    public List<AchievementDTO> checkAndAwardAchievements(Long userId) {
        // 1. 获取用户已获得的成就ID
        List<Long> earnedIds = userAchievementMapper.selectAchievedIdsByUserId(userId);

        // 2. 获取所有未获得的成就
        List<Achievement> unearned;
        if (earnedIds == null || earnedIds.isEmpty()) {
            // 如果用户还没有任何成就，直接查询所有成就
            unearned = achievementMapper.selectList(null);
        } else {
            QueryWrapper<Achievement> queryWrapper = new QueryWrapper<>();
            queryWrapper.notIn("id", earnedIds);
            unearned = achievementMapper.selectList(queryWrapper);
        }

        log.info("用户已获得成就ID: {}", earnedIds);
        log.info("未获得成就数量: {}", unearned.size());
        for (Achievement a : unearned) {
            log.debug("未获得成就: id={}, type={}, threshold={}", a.getId(), a.getConditionType(), a.getThreshold());
        }

        // 3. 按条件类型分组，批量获取统计值
        Map<String, Integer> statsCache = unearned.stream()
                .map(Achievement::getConditionType)
                .distinct()
                .collect(Collectors.toMap(
                        type -> type,
                        type -> userStatisticsService.getStatisticValue(userId, type)
                ));

        // 4. 检查并授予成就
        List<AchievementDTO> newlyAwarded = new ArrayList<>();
        for (Achievement achievement : unearned) {
            Integer currentValue = statsCache.get(achievement.getConditionType());
            if (currentValue != null && currentValue >= achievement.getThreshold()) {
                UserAchievement ua = new UserAchievement();
                ua.setUserId(userId);
                ua.setAchievementId(achievement.getId());
                ua.setAchievedAt(LocalDateTime.now());
                ua.setIsNew(true);
                userAchievementMapper.insert(ua);

                AchievementDTO dto = AchievementDTO.fromEntity(achievement, currentValue, true, ua.getAchievedAt(), true);
                newlyAwarded.add(dto);

                log.info("用户 {} 获得成就: {} ({} >= {})",
                        userId, achievement.getName(), currentValue, achievement.getThreshold());
            }
        }

        return newlyAwarded;
    }

    // 获取用户所有成就（包含进度信息）
    public List<AchievementDTO> getUserAchievementsWithProgress(Long userId) {
        // 1. 获取所有成就定义
        List<Achievement> allAchievements = achievementMapper.selectList(null);

        // 2. 获取用户已获得的成就
        List<UserAchievement> earned = userAchievementMapper.selectAchievementsWithDetail(userId);
        Map<Long, UserAchievement> earnedMap = earned.stream()
                .collect(Collectors.toMap(ua -> ua.getAchievementId(), ua -> ua));

        // 3. 批量获取用户统计值（优化性能）
        Map<String, Integer> statsCache = allAchievements.stream()
                .map(Achievement::getConditionType)
                .distinct()
                .collect(Collectors.toMap(
                        type -> type,
                        type -> userStatisticsService.getStatisticValue(userId, type)
                ));

        // 4. 构建成就DTO列表
        List<AchievementDTO> result = new ArrayList<>();
        for (Achievement achievement : allAchievements) {
            Integer currentValue = statsCache.get(achievement.getConditionType());
            UserAchievement earnedRecord = earnedMap.get(achievement.getId());

            AchievementDTO dto = AchievementDTO.fromEntity(achievement, currentValue,
                    earnedRecord != null,
                    earnedRecord != null ? earnedRecord.getAchievedAt() : null,
                    earnedRecord != null ? earnedRecord.getIsNew() : false);

            result.add(dto);
        }

        return result;
    }

    public List<AchievementDTO> getOrderedAchievements(Long userId) {
        // 1. 把以前获得的都设为已读方便这次排序
        userAchievementMapper.markAsRead(userId);

        // 2. 检查并授予新成就
        List<AchievementDTO> newlyAwarded = this.checkAndAwardAchievements(userId);
        // 获取新成就的ID集合，用于后续排序标识
        Set<Long> newlyAwardedIds = newlyAwarded.stream()
                .map(AchievementDTO::getId)
                .collect(Collectors.toSet());

        // 2. 获取用户所有成就（包含进度状态）
        List<AchievementDTO> allAchievements = this.getUserAchievementsWithProgress(userId);

        // 3. 排序
        allAchievements.sort((a, b) -> {
            // 是否为新获得的
            boolean aIsNew = newlyAwardedIds.contains(a.getId());
            boolean bIsNew = newlyAwardedIds.contains(b.getId());
            if (aIsNew != bIsNew) {
                return aIsNew ? -1 : 1;
            }
            // 是否已获得
            if (a.getIsAchieved() != b.getIsAchieved()) {
                return a.getIsAchieved() ? -1 : 1;
            }
            // 已获得的按成就ID
            return a.getId().compareTo(b.getId());
        });

        return allAchievements;
    }
}
