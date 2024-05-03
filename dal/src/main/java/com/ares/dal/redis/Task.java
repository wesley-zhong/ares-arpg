package com.ares.dal.redis;


import lombok.Data;

@Data
public class Task {
    private int taskType; // TaskEnum
    private String taskId; // task nunique id
    private String eventId; // event id  can read event detail data by this id
    private long eventTime;
}
