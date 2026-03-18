package com.example.demo.entity.enums;

import lombok.Getter;

@Getter
public enum TodoPriority {
    LOW(0, "低"),
    MEDIUM(1, "中"),
    HIGH(2, "高"),
    URGENT(3, "紧急");

    private final int code;
    private final String desc;

    TodoPriority(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static TodoPriority fromCode(int code) {
        for (TodoPriority priority : values()) {
            if (priority.code == code) {
                return priority;
            }
        }
        return MEDIUM;
    }

    public static String getDesc(int code) {
        for (TodoPriority priority : values()) {
            if (priority.code == code) {
                return priority.desc;
            }
        }
        return "未知优先级";
    }
}
