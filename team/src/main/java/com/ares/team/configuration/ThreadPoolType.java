package com.ares.team.configuration;

public enum ThreadPoolType {
    LOGIC(0, "玩家IO");

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
