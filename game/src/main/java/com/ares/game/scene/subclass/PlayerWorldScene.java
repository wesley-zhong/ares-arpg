package com.ares.game.scene.subclass;

import com.ares.game.player.Player;
import com.ares.game.scene.Scene;
import com.ares.game.scene.modules.sight.SceneGridSightModule;
import com.ares.game.scene.world.PlayerWorld;
import com.ares.game.scene.world.World;
import com.game.protoGen.ProtoCommon;

public class PlayerWorldScene extends Scene {
    public PlayerWorldScene(int sceneId) {
        super(sceneId);
    }

    private SceneGridSightModule gridSightModule = new SceneGridSightModule(this);

    @Override
    public ProtoCommon.SceneType getSceneType() {
        return ProtoCommon.SceneType.SCENE_WORLD;
    }

    @Override
    public World getOwnWorld() {
        return getOwnPlayerWorld();
    }

    @Override
    public PlayerWorld getOwnPlayerWorld() {
        Player owner = getOwnPlayer();
        if (owner == null)
        {
            return null;
        }
        return owner.getSceneModule().getMyPlayerWorld();
    }
}
