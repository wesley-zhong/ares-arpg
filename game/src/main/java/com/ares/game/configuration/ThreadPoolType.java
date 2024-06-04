package com.ares.game.configuration;

public enum ThreadPoolType {
    PLAYER_LOGIN(0, "玩家登录"),
    LOGIC(1, "玩家逻辑");

    private final int value;
    private final String name;


    private ThreadPoolType(int value, String name) {
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
