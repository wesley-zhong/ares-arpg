package com.ares.common.math;

import com.game.protoGen.BinServer;
import com.game.protoGen.ProtoCommon;

import java.util.Objects;

public class Vector3 {
    public static final Vector3 ZERO = new Vector3(0, 0, 0);
    public static final Vector3 ONE = new Vector3(1, 1, 1);
    public static final Vector3 UP = new Vector3(0, 1, 0);
    public static final Vector3 DOWN = new Vector3(0, -1, 0);

    private final float x;
    private final float y;
    private final float z;

    public Vector3(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3(Vector3 v) {
        this.x = v.x;
        this.y = v.y;
        this.z = v.z;
    }

    public static Vector3 fromBin(BinServer.VectorBin bin) {
        return new Vector3(bin.getX(), bin.getY(), bin.getZ());
    }

    public static Vector3 fromClient(ProtoCommon.Vector pb) {
        return new Vector3(pb.getX(), pb.getY(), pb.getZ());
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public void toBin(BinServer.VectorBin.Builder bin) {
        bin.setX(x);
        bin.setY(y);
        bin.setZ(z);
    }

    public void toClient(ProtoCommon.Vector.Builder pb) {
        pb.setX(x);
        pb.setY(y);
        pb.setZ(z);
    }

    public static float getDistance(Vector3 pos1, Vector3 pos2)
    {
        return (float) Math.sqrt(Math.pow(pos2.x - pos1.x, 2) + Math.pow(pos2.y - pos1.y, 2) + Math.pow(pos2.z - pos1.z, 2));
    }

    @Override
    public String toString() {
        return "Vector3{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Vector3 vector3 = (Vector3) o;
        return Float.compare(vector3.x, x) == 0 &&
                Float.compare(vector3.y, y) == 0 &&
                Float.compare(vector3.z, z) == 0;
    }
}
