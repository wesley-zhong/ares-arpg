package com.ares.game.configuration;

public enum VirtualThreadPoolType {
    LOGIC(0, "玩家登录"),
    LOGIN(1, "玩家逻辑");

    private final int value;
    private final String name;


    private VirtualThreadPoolType(int value, String name) {
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
