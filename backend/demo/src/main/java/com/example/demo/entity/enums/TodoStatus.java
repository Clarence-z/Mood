package com.example.demo.entity.enums;

import lombok.Getter;

@Getter
public enum TodoStatus {
    TODO(0, "未完成"),
    DONE(1, "已完成");

    private final int code;
    private final String description;

    TodoStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static TodoStatus fromCode(int code) {
        for (TodoStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return TODO; // 默认返回未完成
    }

    public static String getDesc(int code) {
        for (TodoStatus status : values()) {
            if (status.code == code) {
                return status.description;
            }
        }
        return "未知状态";
    }
}
