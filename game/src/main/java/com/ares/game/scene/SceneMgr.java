package com.ares.game.scene;

import com.ares.core.excetion.LogicException;
import com.ares.game.player.Player;
import com.ares.game.scene.subclass.PlayerWorldScene;
import com.game.protoGen.ProtoCommon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SceneMgr {
    private static final Logger log = LoggerFactory.getLogger(SceneMgr.class);

    public static Scene createScene(int sceneId, Player player) {
        ProtoCommon.SceneType sceneType = ProtoCommon.SceneType.SCENE_WORLD;
        Scene scene = null;
        switch (sceneType) {
            case SCENE_WORLD:
                scene = new PlayerWorldScene(sceneId);
                break;
            default:
                throw new LogicException(ProtoCommon.ErrCode.UNKNOWN_VALUE, "Invalid scene type: " + sceneType);
        }

        if (player != null) {
            scene.setOwnPlayer(player);
        }

        log.info("[SCENE] create scene succ. scene:" + scene);
        return scene;
    }
}
