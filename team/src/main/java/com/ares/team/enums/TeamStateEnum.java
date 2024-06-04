package com.ares.team.enums;

public enum TeamStateEnum {
    CREATED(1),
    STARTED(2);
    private int value;

    private TeamStateEnum(int value) {
        this.value = value;
    }
}
