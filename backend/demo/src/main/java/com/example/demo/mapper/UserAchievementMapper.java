package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.entity.UserAchievement;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface UserAchievementMapper extends BaseMapper<UserAchievement> {

    // 查询用户已获得的成就ID列表
    @Select("SELECT achievement_id FROM user_achievement WHERE user_id = #{userId}")
    List<Long> selectAchievedIdsByUserId(@Param("userId") Long userId);

    // 查询用户已获得的成就详情（包含成就信息）
    @Select("SELECT ua.*, a.* FROM user_achievement ua " +
            "LEFT JOIN achievement a ON ua.achievement_id = a.id " +
            "WHERE ua.user_id = #{userId} " +
            "ORDER BY ua.achieved_at DESC")
    List<UserAchievement> selectAchievementsWithDetail(@Param("userId") Long userId);

    // 标记成就为“已读”
    @Update("UPDATE user_achievement SET is_new = false WHERE user_id = #{userId}")
    int markAsRead(@Param("userId") Long userId);
}
