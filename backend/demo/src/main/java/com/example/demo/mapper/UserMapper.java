package com.example.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.entity.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查找用户
     */
    @Select("SELECT * FROM users WHERE username = #{username}")
    User selectByUsername(@Param("username") String username);

    /**
     * 根据邮箱查找用户
     */
    @Select("SELECT * FROM users WHERE email = #{email}")
    User selectByEmail(@Param("email") String email);

    // 更新都可以通过UpdateWrapper完成，不需要写SQL
    /**
     * 更新用户头像
     */
//    @Select("UPDATE users SET avatar = #{avatar} WHERE id = #{userId}")
//    void updateAvatar(@Param("userId") Long userId, @Param("avatar") String avatar);
}
