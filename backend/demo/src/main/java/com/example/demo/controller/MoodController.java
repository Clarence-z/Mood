package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.dto.request.mood.AddMoodRequest;
import com.example.demo.dto.request.mood.UpdateMoodRequest;
import com.example.demo.dto.response.PageResult;
import com.example.demo.dto.response.mood.MoodDTO;
import com.example.demo.dto.response.todo.TodoDTO;
import com.example.demo.service.MoodService;
import com.example.demo.service.UserStatisticsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@RestController
@RequestMapping("/mood/diary")
@AllArgsConstructor
@Validated
public class MoodController {

    @Autowired
    private MoodService moodService;
    @Autowired
    private UserStatisticsService userStatisticsService;

    @GetMapping
    public Result<PageResult<MoodDTO>> getMoodList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "7") Integer size,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @Min(1) @Max(5) Integer minValue,
            @RequestParam(required = false) @Min(1) @Max(5) Integer maxValue,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        // 参数校验（增强健壮性，以后有时间可以完善一下其他功能的这个问题）
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            return Result.error(400, "开始日期不能晚于结束日期");
        }
        if (minValue != null && maxValue != null && minValue > maxValue) {
            return Result.error(400, "心情最小值不能大于最大值");
        }

        try {
            LocalDateTime startDateTime = (startDate != null) ? startDate.atStartOfDay() : null;
            LocalDateTime endDateTime = (endDate != null) ? endDate.atTime(LocalTime.MAX) : null;

            PageResult<MoodDTO> records = moodService.getMoodList(page, size, userId, startDateTime, endDateTime, keyword, minValue, maxValue);

            return Result.success(records, "查询成功");
        } catch (IllegalArgumentException e) {
            // 处理业务逻辑参数错误
            return Result.error(400, e.getMessage());
        } catch (Exception e) {
            // 处理其他未预期的系统异常
            // 实际项目中可在此记录日志
            return Result.error(500, "系统繁忙，请稍后重试");
        }
    }

    // 其实是create但是做得早了统一起来不方便（
    @PostMapping
    public Result<MoodDTO> postMood(
            @Valid @RequestBody AddMoodRequest addMoodRequest,
            HttpServletRequest httpRequest){
        Long userId = (Long) httpRequest.getAttribute("userId");
        MoodDTO moodDTO = moodService.addMood(addMoodRequest, userId);
        userStatisticsService.incrementDiaryCount(userId);
        return Result.success(moodDTO, "日记上传成功");
    }

    @GetMapping("/{id}")
    public Result<MoodDTO> getMood(@PathVariable Long id,
                                   HttpServletRequest httpRequest){
        Long userId = (Long) httpRequest.getAttribute("userId");
        MoodDTO mood = moodService.getMoodById(id, userId);
        return Result.success(mood, "查询成功");
    }

    @PutMapping("/{id}")
    public Result<MoodDTO> updateMood(
            @PathVariable Long id,
            @Valid @RequestBody UpdateMoodRequest updateMoodRequest,
            HttpServletRequest httpRequest){
        Long userId = (Long) httpRequest.getAttribute("userId");
        MoodDTO moodDTO = moodService.updateMood(id, updateMoodRequest, userId);
        return Result.success(moodDTO, "日记更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteMood(
            @PathVariable @Min(1) Long id,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        boolean isDeleted = moodService.deleteMood(id, userId);
        return isDeleted ? Result.success(null, "日记删除成功") : Result.error("日记删除失败");
    }
}
