package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.entity.UserStatistics;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface UserStatisticsMapper extends BaseMapper<UserStatistics> {
    @Select("SELECT * FROM user_statistics WHERE user_id = #{userId}")
    UserStatistics selectByUserId(@Param("userId") Long userId);

    // 原子操作：增加日记计数
    @Update("UPDATE user_statistics SET diary_count = diary_count + 1, updated_at = NOW() WHERE user_id = #{userId}")
    int incrementDiaryCount(@Param("userId") Long userId);

    // 原子操作：增加已完成任务数
    @Update("UPDATE user_statistics SET todo_completed = todo_completed + 1, updated_at = NOW() WHERE user_id = #{userId}")
    int incrementTodoCompleted(@Param("userId") Long userId);

    // 原子操作：减少已完成任务数
    @Update("UPDATE user_statistics SET todo_completed = todo_completed - 1, updated_at = NOW() WHERE user_id = #{userId}")
    int decrementTodoCompleted(@Param("userId") Long userId);

    // 原子操作：增加AI消息总数
    @Update("UPDATE user_statistics SET ai_message_total = ai_message_total + 1, updated_at = NOW() WHERE user_id = #{userId}")
    int incrementAiMessageTotal(@Param("userId") Long userId);

    // 创建用户统计记录
    @Update({
            "INSERT INTO user_statistics (user_id, diary_count, todo_completed, ai_message_total, ",
            "total_login_days, current_streak, last_login_date) ",
            "VALUES (#{userId}, 0, 0, 0, 0, 0, NULL) ",
            "ON DUPLICATE KEY UPDATE updated_at = NOW()"
    })
    int initUserStatistics(@Param("userId") Long userId);
}
