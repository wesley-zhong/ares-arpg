package com.ares.gateway.configuration;

public enum ThreadPoolType {
    LOGIN(0, "USER_LOGIN");

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
