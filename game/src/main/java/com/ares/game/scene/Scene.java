package com.ares.game.scene;

import com.ares.game.player.GamePlayer;
import com.ares.game.scene.modules.sight.SceneSightModule;
import com.game.protoGen.ProtoCommon;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class Scene extends SceneModuleContainer {
    private final int sceneId;
    private GamePlayer player;
    private long ownerUid;

    public Scene(final int sceneId) {
        this.sceneId = sceneId;
    }

    public abstract ProtoCommon.SceneType getSceneType();

    private SceneSightModule sightModule;
    public SceneSightModule getSightModule() {
        if (sightModule == null) {
            sightModule = (SceneSightModule) getModule(ProtoCommon.GameModuleId.GMI_SceneSight);
        }
        return sightModule;
    }

    public void setOwnPlayer(GamePlayer player)
    {
        this.player = player;
        if (player != null) {
            ownerUid = player.getUid();
        }
    }

    public void init() {

    }

    public void start() {

    }
}
