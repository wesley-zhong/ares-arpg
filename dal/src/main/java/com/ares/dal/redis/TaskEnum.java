package com.ares.dal.redis;

public enum TaskEnum {
    LEADER_BOARD_RESET(0, "排行重置"),
    LEADER_BOARD_FRESH(1, "排行刷新");

    private final int value;
    private final String name;


    private TaskEnum(int value, String name) {
        this.value = value;
        this.name = name;
    }
    public int getValue() {
        return value;
    }

    public String getName() {
        return name;
    }
}
