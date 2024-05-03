package com.ares.game.scene.subclass;

import com.ares.game.scene.Scene;
import com.game.protoGen.ProtoCommon;

public class DungeonScene extends Scene {
    public DungeonScene(int sceneId) {
        super(sceneId);
    }

    @Override
    public ProtoCommon.SceneType getSceneType() {
        return ProtoCommon.SceneType.SCENE_DUNGEON;
    }
}
