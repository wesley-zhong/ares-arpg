package com.ares.game.scene;

import com.ares.core.excetion.FyLogicException;
import com.ares.game.player.Player;
import com.ares.game.scene.subclass.DungeonScene;
import com.ares.game.scene.subclass.PlayerRoomScene;
import com.ares.game.scene.subclass.PlayerWorldScene;
import com.game.protoGen.ProtoCommon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SceneMgr {
    private static final Logger log = LoggerFactory.getLogger(SceneMgr.class);

    public static Scene createScene(int sceneId, Player player) {
        ProtoCommon.SceneType sceneType = getSceneTypeBySceneId(sceneId);
        Scene scene = null;
        switch (sceneType) {
            case SCENE_WORLD:
                scene = new PlayerWorldScene(sceneId);
                break;
            case SCENE_DUNGEON:
                scene = new DungeonScene(sceneId);
                break;
            case SCENE_ROOM:
                scene = new PlayerRoomScene(sceneId);
                break;
            default:
                throw new FyLogicException(ProtoCommon.ErrCode.UNKNOWN_VALUE, "Invalid scene type: " + sceneType);
        }

        if (player != null) {
            scene.setOwnPlayer(player);
        }

        log.info("[SCENE] create scene succ. scene:" + scene);
        return scene;
    }

    private static ProtoCommon.SceneType getSceneTypeBySceneId(int sceneId) {
        switch (sceneId) {
            case 1:
                return ProtoCommon.SceneType.SCENE_WORLD;
            case 2:
                return ProtoCommon.SceneType.SCENE_DUNGEON;
            case 3:
                return ProtoCommon.SceneType.SCENE_ROOM;
            default:
                throw new FyLogicException(ProtoCommon.ErrCode.UNKNOWN_VALUE, "Invalid sceneId: " + sceneId);
        }
    }
}
