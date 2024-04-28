package com.ares.game.scene.entity;

import com.ares.common.math.Vector3;

public class EntityTRS {
    private Vector3 position;
    private Vector3 rotation;
    private Vector3 scale;

    public Vector3 getPosition() {
        return position;
    }

    public void setPosition(Vector3 position) {
        this.position = position;
    }

    public Vector3 getRotation() {
        return rotation;
    }

    public void setRotation(Vector3 rotation) {
        this.rotation = rotation;
    }

    public Vector3 getScale() {
        return scale;
    }

    public void setScale(Vector3 scale) {
        this.scale = scale;
    }
}
