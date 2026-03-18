package com.example.demo.controller;

import com.example.demo.dto.request.user.AvatarUpdateRequest;
import com.example.demo.dto.request.user.LoginRequest;
import com.example.demo.dto.request.user.RegisterRequest;
import com.example.demo.dto.request.user.UpdateUserRequest;
import com.example.demo.dto.response.user.UserDTO;
import com.example.demo.service.UserService;
import com.example.demo.service.UserStatisticsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/mood/user")
@Validated
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private UserStatisticsService userStatisticsService;

    // 用户注册
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        Map<String, Object> result = userService.register(
                request.getUsername(),
                request.getPassword(),
                request.getNickname(),
                request.getEmail()
        );
        return ResponseEntity.ok(result);
    }

    // 用户登录
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        Map<String, Object> result = userService.login(
                request.getUsername(),
                request.getPassword()
        );
        return ResponseEntity.ok(result);
    }

    // 获取当前用户信息
    @GetMapping("/profile")
    public ResponseEntity<UserDTO> getProfile(HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        UserDTO userDTO = userService.getCurrentUser(userId);
//        userStatisticsService.updateLoginStats(userId);
        return ResponseEntity.ok(userDTO);
    }

    // 更新用户信息
    @PutMapping("/profile")
    public ResponseEntity<UserDTO> updateProfile(
            @Valid @RequestBody UpdateUserRequest updateRequest,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        UserDTO userDTO = userService.updateUser(
                userId,
                updateRequest.getNickname(),
                updateRequest.getBio(),
                updateRequest.getThemeId()
        );
        return ResponseEntity.ok(userDTO);
    }

    // 更新用户头像
    @PostMapping("/avatar")
    public ResponseEntity<UserDTO> updateAvatar(
            @Valid @RequestBody AvatarUpdateRequest request,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        UserDTO userDTO = userService.updateAvatar(userId, request.getAvatarData());
        return ResponseEntity.ok(userDTO);
    }

    // 修改密码
    @PutMapping("/password")
    public ResponseEntity<Map<String, String>> changePassword(
            @RequestBody Map<String, String> passwordRequest,
            HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        String oldPassword = passwordRequest.get("oldPassword");
        String newPassword = passwordRequest.get("newPassword");

        userService.changePassword(userId, oldPassword, newPassword);

        Map<String, String> response = new HashMap<>();
        response.put("message", "密码修改成功");
        return ResponseEntity.ok(response);
    }
}
//    @Autowired  // 字段注入，就不用自己创建一个新对象了，
//    但使用lombok @AllArgsConstructor 或者 @RequiredArgsConstructor + final字段就也不用写了
//    private UserMapper userMapper;
//    private OrderMapper orderMapper;
//
//    // 多表查询-Many
//    @GetMapping("/user/find1")
//    public List<User> getOrders(){
//        System.out.println("Test for find1 asked in front end");
//        return userMapper.SelectAllUserAndOrders();
//    }
//
//    // 多表查询-One
//    @GetMapping("/user/find2")
//    public List<Order> getUsers(){
//        return orderMapper.SelectAllOrdersAndUser();
//    }
//
//    // 条件查询
//    @GetMapping("/user/find3")  // 使用mybatis-plus的条件查询（可以去官网看用法）
//    public List<User> getByCondition(User user){
//        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("name", "Alan");
//        return userMapper.selectList(queryWrapper);
//    }
//
//    // 分页查询
//    @GetMapping("/user/find4")
//    public IPage<User> getByPage(){
//        Page<User> page = new Page<>(0, 1);
//        // IPage对象用于封装结果值
//        IPage iPage = userMapper.selectPage(page, null);    // 这里可以添加条件查询
//        return iPage;
//    }
//
//    @GetMapping("/user")
//    public List query(){
//        List<User> list = userMapper.getUsers();
//        System.out.println(list);
//        return list;
//    }
//
//    @GetMapping("/user/search")
//    public User get(String name){
//        User user = userMapper.getUser(name);
//        System.out.println(user);
//        return user;
//    }
//
//    @PostMapping("user")
//    public String save(User user){
//        int i = userMapper.insertUser(user);
//        System.out.println(user);
//        if(i>0){
//            return "success";
//        }
//        return "fail";
//    }
//
//    @PutMapping("/user")
//    public String update(User user){
//        int i = userMapper.updateUser(user);
//        if(i>0) {
//            return "success";
//        }
//        return "fail";
//    }
//
//    @DeleteMapping("/user")
//    public String delete(int id){
//        int i=userMapper.deleteUser(id);
//        if(i>0) {
//            return "success";
//        }
//        return "fail";
//    }

//    以下为RESTful示例

//    @Operation(summary = "获取用户")
//    @GetMapping("/user/{id}")
//    public String getUserById(@PathVariable int id){
//        System.out.println("getUserById:" + id);
//        return "get user:" + id;
//    }
//    @PostMapping("/user")
//    public String save(User user){
//        return "save user:" + user;
//    }
//    @PutMapping("/user")
//    public String update(User user){
//        return "update user:" + user;
//    }
//    @DeleteMapping("/user/{id}")
//    public String deleteById(@PathVariable int id){
//        System.out.println("deleteById:" + id);
//        return "delete user:" + id;
//    }


