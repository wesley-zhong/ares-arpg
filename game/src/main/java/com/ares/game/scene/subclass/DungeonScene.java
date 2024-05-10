package com.ares.game.scene.subclass;

import com.ares.game.scene.Scene;
import com.ares.game.scene.modules.sight.SceneGridSightModule;
import com.game.protoGen.ProtoCommon;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Setter
@Slf4j
public class DungeonScene extends Scene {
    private int dungeonId;
    private SceneGridSightModule gridSightModule = new SceneGridSightModule(this);

    public DungeonScene(int sceneId) {
        super(sceneId);
    }

    @Override
    public ProtoCommon.SceneType getSceneType() {
        return ProtoCommon.SceneType.SCENE_DUNGEON;
    }

    @Override
    public void init(SceneInitParam initParam) {
        dungeonId = initParam.dungeonId;
        super.init(initParam);
    }
}
