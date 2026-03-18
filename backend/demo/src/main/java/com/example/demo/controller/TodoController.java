package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.dto.request.todo.AddTodoRequest;
import com.example.demo.dto.request.todo.UpdateTodoRequest;
import com.example.demo.dto.response.PageResult;
import com.example.demo.dto.response.todo.TodoDTO;
import com.example.demo.dto.response.todo.TodoStatisticsDTO;
import com.example.demo.service.TodoService;
import com.example.demo.service.UserStatisticsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/mood/todos")
public class TodoController {

    @Autowired
    private TodoService todoService;
    @Autowired
    private UserStatisticsService userStatisticsService;

    /**
     * 获取任务列表（支持多条件筛选）
     * @param status 状态筛选（0:待办, 1:进行中, 2:已完成, 3:已取消）
     * @param priority 优先级筛选（0:低, 1:中, 2:高, 3:紧急）
     * @param tags 标签筛选
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param keyword 关键词搜索（标题或描述）
     */
    //这里要注意前端是传NULL还是空字符串……
    @GetMapping("/page")
    public Result<PageResult<TodoDTO>> getTodoListByPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "7") Integer size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer priority,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm") LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm") LocalDateTime endDate,
            @RequestParam(required = false) String keyword,
            HttpServletRequest request) {

        Long userId = (Long) request.getAttribute("userId");
        PageResult<TodoDTO> todoList = todoService.getTodoListByPage(page, size, userId, status, priority, tags, startDate, endDate, keyword);
        return Result.success(todoList, "查询成功");
    }

    @GetMapping
    public Result<List<TodoDTO>> getTodoList(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer priority,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm") LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm") LocalDateTime endDate,
            @RequestParam(required = false) String keyword,
            HttpServletRequest request) {

        Long userId = (Long) request.getAttribute("userId");
        List<TodoDTO> todoList = todoService.getTodoList(userId, status, priority, tags, startDate, endDate, keyword);
        return Result.success(todoList, "查询成功");
    }

    // 获取单个任务详情
    @GetMapping("/{id}")
    public Result<TodoDTO> getTodoById(@PathVariable Long id,
                                       HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        TodoDTO todo = todoService.getTodoById(id, userId);
        return Result.success(todo, "查询成功");
    }

    // 创建新任务
    @PostMapping
    public Result<TodoDTO> createTodo(@Valid @RequestBody AddTodoRequest addTodoRequest,
                                      HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        TodoDTO createdTodo = todoService.addTodo(addTodoRequest, userId);
        return Result.success(createdTodo, "任务创建成功");
    }

    // 更新任务（全字段可选更新）
    @PutMapping("/{id}")
    public Result<TodoDTO> updateTodo(@PathVariable Long id,
                                      @Valid @RequestBody UpdateTodoRequest updateTodoRequest,
                                      HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        TodoDTO updatedTodo = todoService.updateTodo(id, updateTodoRequest, userId);
        return Result.success(updatedTodo, "任务更新成功");
    }

    // 删除任务（软删除）
    @DeleteMapping("/{id}")
    public Result<Void> deleteTodo(@PathVariable Long id,
                                   HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        boolean isDeleted = todoService.deleteTodo(id, userId);
        // 删除这里就不考虑更新用户统计数据了
        return isDeleted ? Result.success(null, "任务删除成功") : Result.error("任务删除失败");
    }

    // 切换任务置顶状态
    @PatchMapping("/{id}/pin")
    public Result<TodoDTO> togglePin(@PathVariable Long id,
                                     HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        TodoDTO updatedTodo = todoService.togglePin(id, userId);
        String message = Boolean.TRUE.equals(updatedTodo.getIsPinned()) ? "已置顶" : "已取消置顶";
        return Result.success(updatedTodo, message);
    }

    // 标记任务为【已完成】
    @PatchMapping("/{id}/done")
    public Result<TodoDTO> markAsDone(@PathVariable Long id,
                                      HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        TodoDTO updatedTodo = todoService.markAsDone(id, userId);
        userStatisticsService.incrementTodoCompleted(userId);
        return Result.success(updatedTodo, "任务标记为【已完成】");
    }

    // 标记任务为【待办】
    @PatchMapping("/{id}/todo")
    public Result<TodoDTO> markAsTodo(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        TodoDTO updatedTodo = todoService.markAsTodo(id, userId);
        userStatisticsService.decrementTodoCompleted(userId);
        return Result.success(updatedTodo, "任务标记为【待办】");
    }

    // 获取用户的任务统计数据
    @GetMapping("/statistics")
    public Result<TodoStatisticsDTO> getStatistics(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        TodoStatisticsDTO statistics = todoService.getStatistics(userId);
        return Result.success(statistics, "统计获取成功");
    }
}
