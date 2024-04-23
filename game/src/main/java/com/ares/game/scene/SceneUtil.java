package com.ares.game.scene;

import com.game.protoGen.ProtoScene;

public class SceneUtil {
    public static Vector3 pbUnreal2RecastPoint(ProtoScene.PbVector3 point, boolean no_negative)
    {
        return new Vector3(
                no_negative ? ((double) point.getX()) / 100 : - ((double) point.getX()) / 100,
                ((double) point.getZ())/ 100,
                no_negative ? ((double) point.getY()) / 100 : - ((double) point.getY()) / 100);
    }

    public static ProtoScene.PbVector3 recast2PbUnrealPoint(Vector3 point, boolean no_negative)
    {
        return ProtoScene.PbVector3.newBuilder()
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
