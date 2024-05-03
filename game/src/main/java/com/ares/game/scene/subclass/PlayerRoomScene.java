package com.ares.game.scene.subclass;

import com.ares.game.scene.Scene;
import com.game.protoGen.ProtoCommon;

public class PlayerRoomScene extends Scene {
    public PlayerRoomScene(int sceneId) {
        super(sceneId);
    }

    @Override
    public ProtoCommon.SceneType getSceneType() {
        return ProtoCommon.SceneType.SCENE_ROOM;
    }
}
