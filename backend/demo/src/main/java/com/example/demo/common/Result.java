package com.example.demo.common;

import lombok.Data;

@Data
public class Result<T> {
    private Integer code; // 状态码 (200=成功, 500=系统错误, 401=未登录, 403=无权限, 404=不存在)
    private String message; // 提示信息
    private T data; // 响应数据

    // 成功静态方法
    public static <T> Result<T> success() {
        return success(null, "操作成功");
    }
    public static <T> Result<T> success(T data) {
        return success(data, "操作成功");
    }
    public static <T> Result<T> success(T data, String message) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    // 错误静态方法
    public static <T> Result<T> error(String message) {
        return error(500, message);
    }
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        result.setData(null);
        return result;
    }
}
