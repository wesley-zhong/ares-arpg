package com.ares.game.scene.subclass;

import com.ares.game.player.Player;
import com.ares.game.scene.world.PlayerWorld;
import com.ares.game.scene.world.World;
import com.game.protoGen.ProtoCommon;

public class PlayerRoomScene extends RoomScene {
    public PlayerRoomScene(int sceneId) {
        super(sceneId);
    }

    @Override
    public ProtoCommon.SceneType getSceneType() {
        return ProtoCommon.SceneType.SCENE_ROOM;
    }

    @Override
    public World getOwnWorld()
    {
        return getOwnPlayerWorld();
    }

    // 获取owner的world
    @Override
    public PlayerWorld getOwnPlayerWorld()
    {
        Player ownPlayer = getOwnPlayer();
        if (ownPlayer == null)
        {
            return null;
        }
        return ownPlayer.getSceneModule().getMyPlayerWorld();
    }
}
