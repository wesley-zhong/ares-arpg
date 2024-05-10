package com.ares.game.scene.group;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Slf4j
public class Group {
    enum GroupType
    {
        NORMAL,             // 一般意义的group
        DYNAMIC,            // dynamic_load = true的group
        PATTERN,            // pattern生成的group
    }

    private final int groupId;
    private final GroupType type;

    public Group(final int groupId, final GroupType type) {
        this.groupId = groupId;
        this.type = type;
    }
}
