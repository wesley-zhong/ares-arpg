package com.ares.game.scene;

import com.game.protoGen.ProtoCommon;

public class SceneUtil {
    public static boolean isWorldScene(ProtoCommon.SceneType type) {
        switch (type) {
            case SCENE_WORLD -> {
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    public static boolean isPlayerScene(ProtoCommon.SceneType type)
    {
        switch (type)
        {
            case SCENE_WORLD:
            case SCENE_ROOM:
                return true;
            default:
                return false;
        }
    }
}
