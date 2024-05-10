package com.ares.game.scene.modules.sight;

import com.ares.common.math.Vector2;
import com.ares.common.math.Vector3;
import com.ares.core.excetion.FyLogicException;
import com.ares.game.scene.Region;
import com.ares.game.scene.Scene;
import com.ares.game.scene.SceneModule;
import com.ares.game.scene.entity.Entity;
import com.ares.game.scene.visitor.Visitor;
import com.game.protoGen.ProtoCommon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Set;

/**
 *  场景视距管理器基类
 **/
public abstract class SceneSightModule extends SceneModule {
    private static final Logger log = LoggerFactory.getLogger(SceneSightModule.class);

    protected Vector2 beginPos;                 // 场景的开始坐标
    protected Vector2 sceneSize;                // 场景实际大小

    public SceneSightModule(Scene scene) {
        super(ProtoCommon.GameModuleId.GMI_SceneSight, scene);
    }

    public void init(Vector2 beginPos, Vector2 sceneSize)
    {
        this.beginPos = beginPos;
        this.sceneSize = sceneSize;
        if (0 == sceneSize.getX() || 0 == sceneSize.getY())
        {
            throw new FyLogicException(ProtoCommon.ErrCode.UNKNOWN_VALUE, "0 == sceneSize.x || 0 == sceneSize.y");
        }
    }

    public Vector2 getBeginPos() {
        return beginPos;
    }

    public Vector2 getSceneSize() {
        return sceneSize;
    }

    // entity进入场景
    public abstract Collection<Entity> placeEntity(Entity entity);

    // entity离开场景
    public abstract Collection<Entity> removeEntity(Entity entity);

    public static class EntityMoveToRet {
        public Collection<Entity> missEntities;
        public Collection<Entity> meetEntities;
    }

    // entity在场景中移动
    public abstract EntityMoveToRet entityMoveTo(Entity entity, Vector3 destPos);

    // 查询所有视距级别，entity附近满足visitor条件的entity集合
    public abstract void visitGridsInSight(Entity entity, Visitor visitor);

    // entity是否移动格子
    public abstract boolean isEntityMoveGrid(Entity entity, Vector3 prev_pos, int prevRoom);

    // 寻找pos关联的region列表
    public abstract Set<Region> findPossibleRegionSet(Vector3 pos, int roomId);
}
