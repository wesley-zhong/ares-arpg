package com.ares.game.scene;

import com.ares.common.math.Vector3;
import com.game.protoGen.ProtoOldScene;

public class OldSceneUtil {
    public static Vector3 pbUnreal2RecastPoint(ProtoOldScene.PbVector3 point, boolean no_negative)
    {
        return new Vector3(
                no_negative ? ((float) point.getX()) / 100 : - ((float) point.getX()) / 100,
                ((float) point.getZ())/ 100,
                no_negative ? ((float) point.getY()) / 100 : - ((float) point.getY()) / 100);
    }

    public static ProtoOldScene.PbVector3 recast2PbUnrealPoint(Vector3 point, boolean no_negative)
    {
        return ProtoOldScene.PbVector3.newBuilder()
                .setX(no_negative ? (int) point.getX() * 100 : - (int) point.getX() * 100)
                .setY(no_negative ? (int) point.getZ() * 100 : - (int) point.getZ() * 100)
                .setZ((int) point.getY() * 100)
                .build();
    }

    public static Vector3 recast2UnrealPoint(Vector3 point, boolean no_negative)
    {
        return new Vector3(
                no_negative ? point.getX() : - point.getX(),
                no_negative ? point.getZ() : - point.getZ(),
                point.getY());
    }
}
