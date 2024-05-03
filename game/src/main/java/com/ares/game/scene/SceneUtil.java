package com.ares.game.scene;

import com.game.protoGen.ProtoCommon;
import com.game.protoGen.ProtoScene;

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

    public static boolean isVisionTypeReplace(ProtoScene.VisionType type)
    {
        switch (type)
        {
            case VISION_REPLACE:
            case VISION_REPLACE_DIE:
            case VISION_REPLACE_NO_NOTIFY:
                return true;
            default:
                return false;
        }
    }

    public static boolean isEyePointAsEyeClosed() {
        return true;
    }

    public static boolean isNotifyEyePointClosed() {
        return true;
    }

    public static boolean isGroupVisionTypeClosed() {
        return true;
    }
}
