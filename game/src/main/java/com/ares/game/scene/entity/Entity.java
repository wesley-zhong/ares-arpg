package com.ares.game.scene.entity;

import com.ares.common.math.Coordinate;
import com.ares.game.player.GamePlayer;
import com.ares.game.scene.Grid;
import com.ares.game.scene.Scene;
import com.ares.game.scene.visitor.Visitor;
import com.game.protoGen.ProtoCommon;
import com.game.protoGen.ProtoScene;

public abstract class Entity {
    private int entityId;
    private int configId;
    private String entityName;
    private Scene scene;
    private Grid grid;
    private Coordinate coordinate;      // 以grid为单位的坐标
    private ProtoCommon.VisionLevelType visionLevelType = ProtoCommon.VisionLevelType.VISION_LEVEL_NORMAL;

    public abstract ProtoScene.ProtEntityType getEntityType();

    public int getEntityId() {
        return entityId;
    }

    public void setEntityId(int entityId) {
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

    public Scene getScene() {
        return scene;
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }

    public Grid getGrid() {
        return grid;
    }

    public void setGrid(Grid grid) {
        this.grid = grid;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(Coordinate coord) {
        this.coordinate = coord;
    }

    public ProtoCommon.VisionLevelType getVisionLevelType() {
        return visionLevelType;
    }

    public void setVisionLevelType(ProtoCommon.VisionLevelType visionLevelType) {
        this.visionLevelType = visionLevelType;
    }

    // Visitor设计模式，实现accept方法
    public int accept(Visitor visitor)
    {
        return visitor.visitEntity(this);
    }

    // 获取所属player(该实体是player的一部分)
    public GamePlayer getPlayer() {
        return null;
    }

    public long getPlayerUid() {
        GamePlayer player = getPlayer();
        if (player != null) {
            return player.getUid();
        }
        return 0;
    }

    // 获取所属player(该实体不是player的一部分)
    public GamePlayer getOwnerPlayer() {
        return null;
    }
}
