package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.dto.request.mood.AddMoodRequest;
import com.example.demo.dto.request.mood.UpdateMoodRequest;
import com.example.demo.dto.response.PageResult;
import com.example.demo.dto.response.mood.MoodDTO;
import com.example.demo.entity.Mood;
import com.example.demo.mapper.MoodMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

// 用于管理日记
@Service
@Transactional
public class MoodService {

    @Autowired
    private MoodMapper moodMapper;


    private QueryWrapper<Mood> buildMoodQueryWrapper(
                                     Long userId,
                                     LocalDateTime startDate,
                                     LocalDateTime endDate,
                                     String keyword,
                                     Integer minValue,
                                     Integer maxValue) {
        QueryWrapper<Mood> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);

        // 1. 日期范围查询
        if(startDate != null && endDate != null) {
            queryWrapper.between("create_time", startDate, endDate);
        } else if(startDate != null) {
            queryWrapper.ge("create_time", startDate); // 大于等于开始日期
        } else if(endDate != null) {
            queryWrapper.le("create_time", endDate);   // 小于等于结束日期
        }

        // 2. 关键词查询（description）
        if(keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.like("description", keyword.trim());
        }

        // 3. 心情值范围查询
        if(minValue != null) {
            queryWrapper.ge("value", minValue); // value >= minValue
        }
        if(maxValue != null) {
            queryWrapper.le("value", maxValue); // value <= maxValue
        }

        // 默认按创建时间排序
        queryWrapper.orderByDesc("create_time");

        return queryWrapper;
    }

    /**
     * 获取日记列表（支持多条件筛选）
     * @param startDate 开始日期（可选，格式：yyyy-MM-dd）
     * @param endDate 结束日期（可选，格式：yyyy-MM-dd）
     * @param keyword 关键词搜索
     * @param minValue 心情值范围
     * @param maxValue 心情值范围
     */
    public PageResult<MoodDTO> getMoodList(Integer pageNum,
                                               Integer pageSize,
                                               Long userId,
                                               LocalDateTime startDate,
                                               LocalDateTime endDate,
                                               String keyword,
                                               Integer minValue,
                                               Integer maxValue){
        // 参数校验与默认值
        pageNum = (pageNum == null || pageNum < 1) ? 1 : pageNum;
        pageSize = (pageSize == null || pageSize < 1) ? 7 :
                (pageSize > 15 ? 15 : pageSize); // 限制最大10条

        QueryWrapper<Mood> queryWrapper = buildMoodQueryWrapper(
                userId, startDate, endDate, keyword, minValue, maxValue
        );

        // 执行分页查询
        Page<Mood> page = new Page<>(pageNum, pageSize);
        Page<Mood> moodPage = moodMapper.selectPage(page, queryWrapper);

        List<MoodDTO> dtoList = MoodDTO.fromEntities(moodPage.getRecords());

        return new PageResult<>(
                dtoList,
                moodPage.getTotal(),
                moodPage.getPages(),
                moodPage.getCurrent(),
                moodPage.getSize()
        );
    }

    public MoodDTO getMoodById(Long id, Long userId){
        QueryWrapper<Mood> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id)
                .eq("user_id", userId);
        Mood mood = moodMapper.selectOne(queryWrapper);
        if(mood == null){
            throw new RuntimeException("日记不存在");
        }
        return MoodDTO.fromEntity(mood);
    }

    public MoodDTO addMood(AddMoodRequest addMoodRequest, Long userId) {
        Mood mood = addMoodRequest.toEntity();
        mood.setUserId(userId);
        mood.setCreateTime(LocalDateTime.now());
        mood.setUpdateTime(LocalDateTime.now());

        int i = moodMapper.insert(mood);
        if(i>0){
            return MoodDTO.fromEntity(mood);
        }
        else
            throw new RuntimeException("记录失败");
    }

    public MoodDTO updateMood(Long id, UpdateMoodRequest updateMoodRequest, Long userId) {
        // 先验证要更新的任务存在
        QueryWrapper<Mood> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id)
                .eq("user_id", userId);

        Mood mood = moodMapper.selectOne(queryWrapper);
        if(mood == null)
            throw new RuntimeException("日记不存在");

        updateMoodRequest.updateEntity(mood);
        mood.setUserId(userId);
        mood.setUpdateTime(LocalDateTime.now());

        int i = moodMapper.updateById(mood);
        if(i>0){
            return MoodDTO.fromEntity(mood);
        }
        else
            throw new RuntimeException("更新失败");
    }

    public boolean deleteMood(Long id, Long userId) {
        QueryWrapper<Mood> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id)
                .eq("user_id", userId);

        int result = moodMapper.delete(queryWrapper);
        return result > 0;
    }
}
