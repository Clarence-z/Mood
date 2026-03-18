package com.example.demo.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.demo.dto.request.todo.AddTodoRequest;
import com.example.demo.dto.request.todo.UpdateTodoRequest;
import com.example.demo.dto.response.PageResult;
import com.example.demo.dto.response.todo.TodoDTO;
import com.example.demo.dto.response.todo.TodoStatisticsDTO;
import com.example.demo.entity.Todo;
import com.example.demo.entity.enums.TodoStatus;
import com.example.demo.mapper.TodoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

// 用于管理TodoList
@Service
@Transactional
public class TodoService {

    @Autowired
    private TodoMapper todoMapper;

    private QueryWrapper<Todo> buildTodoQueryWrapper(Long userId, Integer status, Integer priority,
                                                     List<String> tags, LocalDateTime startDate,
                                                     LocalDateTime endDate, String keyword) {
        QueryWrapper<Todo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);

        if (status != null)
            queryWrapper.eq("status", status);
        if (priority != null)
            queryWrapper.eq("priority", priority);
        if (tags != null && !tags.isEmpty()) {
            queryWrapper.and(w -> {
                for (int i = 0; i < tags.size(); i++) {
                    if (i == 0) {
                        w.like("tags", tags.get(i));
                    } else {
                        w.or().like("tags", tags.get(i));
                    }
                }
            });
        }

        // 日期范围查询
        if (startDate != null && endDate != null) {
            queryWrapper.between("due_date", startDate, endDate);
        } else if (startDate != null) {
            queryWrapper.ge("due_date", startDate); // 大于等于开始日期
        } else if (endDate != null) {
            queryWrapper.le("due_date", endDate);   // 小于等于结束日期
        }

        // 关键词查询
        if (keyword != null && !keyword.trim().isEmpty())
            queryWrapper.and(wrapper -> wrapper
                    .like("title", keyword.trim())
                    .or()
                    .like("description", keyword.trim()));

        queryWrapper.last("ORDER BY is_pinned DESC, IF(completed_at IS NULL, 0, 1), completed_at DESC, due_date ASC");

        return queryWrapper;
    }

    // 获取多条ToDo,除了userId都是可选参数，用于筛选查询(为什么todo还会高亮啊！太智能了吧！
    /**
     * 获取任务列表（支持多条件筛选）
     * @param status 状态筛选（0:待办, 1:进行中, 2:已完成, 3:已取消）
     * @param priority 优先级筛选（0:低, 1:中, 2:高, 3:紧急）
     * @param tags 标签筛选
     * @param startDate 开始日期（可选，格式：yyyy-MM-dd HH:mm:ss）
     * @param endDate 结束日期（可选，格式：yyyy-MM-dd HH:mm:ss）
     * @param keyword 关键词搜索（标题或描述）
     */
    public PageResult<TodoDTO> getTodoListByPage(Integer pageNum, Integer pageSize,
                                     Long userId, Integer status, Integer priority,
                                     List<String> tags, LocalDateTime startDate,
                                     LocalDateTime endDate, String keyword) {
        // 参数校验与默认值
        pageNum = (pageNum == null || pageNum < 1) ? 1 : pageNum;
        pageSize = (pageSize == null || pageSize < 1) ? 7 :
                (pageSize > 100 ? 100 : pageSize); // 限制最大100条

        QueryWrapper<Todo> queryWrapper = buildTodoQueryWrapper(userId, status, priority, tags, startDate, endDate, keyword);

        // 执行分页查询
        Page<Todo> page = new Page<>(pageNum, pageSize);
        Page<Todo> todoPage = todoMapper.selectPage(page, queryWrapper);

        List<TodoDTO> dtoList = TodoDTO.fromEntities(todoPage.getRecords());

        return new PageResult<>(
                dtoList,
                todoPage.getTotal(),
                todoPage.getPages(),
                todoPage.getCurrent(),
                todoPage.getSize()
        );
    }

    public List<TodoDTO> getTodoList(Long userId, Integer status, Integer priority,
                                     List<String> tags, LocalDateTime startDate,
                                     LocalDateTime endDate, String keyword) {
        QueryWrapper<Todo> queryWrapper = buildTodoQueryWrapper(userId, status, priority, tags, startDate, endDate, keyword);

        List<Todo> todoList = todoMapper.selectList(queryWrapper);
        return TodoDTO.fromEntities(todoList);
    }

    // 获取单个任务详情
    public TodoDTO getTodoById(Long id, Long userId) {
        QueryWrapper<Todo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id)
                .eq("user_id", userId);
        Todo todo = todoMapper.selectOne(queryWrapper);
        if(todo == null)
            throw new RuntimeException("任务不存在");
        return TodoDTO.fromEntity(todo);
    }

    // 新建
    public TodoDTO addTodo(AddTodoRequest addTodoRequest, Long userId) {
        Todo todo = addTodoRequest.toEntity();
        todo.setUserId(userId);
        todo.setCreatedAt(LocalDateTime.now());
        todo.setUpdatedAt(LocalDateTime.now());

        int i = todoMapper.insert(todo);
        if(i>0)
            return TodoDTO.fromEntity(todo);
        else
            throw new RuntimeException("创建失败");
    }

    // 更新（包括各个字段、置顶状态等）
    public TodoDTO updateTodo(Long id, UpdateTodoRequest updateTodoRequest, Long userId) {
        // 1.先验证要更新的任务存在
        QueryWrapper<Todo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id)
                .eq("user_id", userId);

        Todo todo = todoMapper.selectOne(queryWrapper);
        if(todo == null)
            throw new RuntimeException("任务不存在");

        // 2.进行更新
        updateTodoRequest.updateEntity(todo);
        todo.setUpdatedAt(LocalDateTime.now());
        int i = todoMapper.updateById(todo);
        if(i>0)
            return TodoDTO.fromEntity(todo);
        else
            throw new RuntimeException("更新失败");
    }

    // 删除（软删除）
    public boolean deleteTodo(Long id, Long userId) {
        QueryWrapper<Todo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id)
                .eq("user_id", userId);

        int result = todoMapper.delete(queryWrapper);
        return result > 0;
    }

    // 以下：将核心业务相关的更新独立出来
    // 而update的dto更多服务于用户对一些自定义属性的修改

    // 切换任务置顶状态
    public TodoDTO togglePin(Long id, Long userId) {
        QueryWrapper<Todo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id)
                .eq("user_id", userId);

        Todo todo = todoMapper.selectOne(queryWrapper);
        if (todo == null) {
            throw new RuntimeException("任务不存在");
        }

        Boolean currentPinned = todo.getIsPinned();
        todo.setIsPinned(currentPinned == null || !currentPinned);
        todo.setUpdatedAt(LocalDateTime.now());

        todoMapper.updateById(todo);
        return TodoDTO.fromEntity(todo);
    }

    // 标记任务为done
    public TodoDTO markAsDone(Long id, Long userId) {
        return updateStatus(id, TodoStatus.DONE.getCode(), userId);
    }

    // 标记任务为todo
    public TodoDTO markAsTodo(Long id, Long userId) {
        return updateStatus(id, TodoStatus.TODO.getCode(), userId);
    }

    // 通用的状态更新方法（私有）
    private TodoDTO updateStatus(Long id, Integer status, Long userId) {
        QueryWrapper<Todo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id)
                .eq("user_id", userId);

        Todo todo = todoMapper.selectOne(queryWrapper);
        if (todo == null) {
            throw new RuntimeException("任务不存在");
        }

        todo.setStatus(status);
        todo.setUpdatedAt(LocalDateTime.now());

        // 如果状态变为完成，记录完成时间
        // 考虑可以自选完成时间？
        if (status == TodoStatus.DONE.getCode()) {
            todo.setCompletedAt(LocalDateTime.now());
        } else {
            // 状态从未完成 -> 未完成，或完成 -> 未完成，都清空完成时间
            todo.setCompletedAt(null);
        }

        todoMapper.updateById(todo);
        return TodoDTO.fromEntity(todo);
    }

    // 统计（仅限当日，有待完善|现在的统计是在前端）
    public TodoStatisticsDTO getStatistics(Long userId) {
        TodoStatisticsDTO stats = new TodoStatisticsDTO();

        // 今日任务数
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime todayEnd = todayStart.plusDays(1);

        QueryWrapper<Todo> todayQuery = new QueryWrapper<>();
        todayQuery.eq("user_id", userId)
                .ge("due_date", todayStart)
                .lt("due_date", todayEnd);
        stats.setTodayCount(todoMapper.selectCount(todayQuery));

        // 总任务数
        QueryWrapper<Todo> totalQuery = new QueryWrapper<>();
        totalQuery.eq("user_id", userId);
        Long totalCount = todoMapper.selectCount(totalQuery);
        stats.setTotalCount(totalCount);

        // 已完成数
        QueryWrapper<Todo> doneQuery = new QueryWrapper<>();
        doneQuery.eq("user_id", userId)
                .eq("status", TodoStatus.DONE.getCode());
        Long doneCount = todoMapper.selectCount(doneQuery);
        stats.setDoneCount(doneCount);

        // 计算完成率
        double completionRate = totalCount > 0 ? (double) doneCount / totalCount * 100 : 0;
        stats.setCompletionRate(completionRate);

        return stats;
    }
}
