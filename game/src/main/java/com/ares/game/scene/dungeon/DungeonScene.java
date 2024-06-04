package com.ares.game.scene.dungeon;

import com.ares.game.scene.Scene;
import com.game.protoGen.ProtoCommon;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Slf4j
public class DungeonScene extends Scene {
    // 副本结果
    enum DungeonResult
    {
        DUNGEON_RESULT_NONE,        // 无结果
        DUNGEON_RESULT_SUCCEED,     // 成功
        DUNGEON_RESULT_FAIL,        // 失败
        DUNGEON_RESULT_CANCEL       // 取消
    };

    public DungeonScene(int sceneId) {
        super(sceneId);
    }

    @Override
    public ProtoCommon.SceneType getSceneType() {
        return ProtoCommon.SceneType.SCENE_DUNGEON;
    }
}
