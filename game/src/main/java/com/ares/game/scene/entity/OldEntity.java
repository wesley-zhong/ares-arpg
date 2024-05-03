package com.ares.game.scene.entity;

import com.ares.game.scene.OldScene;

public abstract class OldEntity {
    private long entityId;
    private int configId;
    private String entityName;
    private OldScene scene;

    public long getEntityId() {
        return entityId;
    }

    public void setEntityId(long entityId) {
        this.entityId = entityId;
    }

    public int getConfigId() {
        return configId;
    }

    public void setConfigId(int configId) {
        this.configId = configId;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public OldScene getScene() {
        return scene;
    }

    public void setScene(OldScene scene) {
        this.scene = scene;
    }
}
