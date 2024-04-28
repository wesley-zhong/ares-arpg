package com.ares.game.scene.modules.sight;

import com.ares.common.math.Vector2;
import com.ares.common.math.Vector3;
import com.ares.core.excetion.UnknownLogicException;
import com.ares.game.scene.GridMgr;
import com.ares.game.scene.Region;
import com.ares.game.scene.Scene;
import com.ares.game.scene.entity.Entity;
import com.ares.game.scene.visitor.Visitor;
import com.game.protoGen.ProtoCommon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.ares.common.math.MathUtil.UINT16_MAX;

public class SceneGridSightModule extends SceneSightModule {
    private static final Logger log = LoggerFactory.getLogger(SceneGridSightModule.class);

    private final GridMgr[] gridMgrs = new GridMgr[ProtoCommon.VisionLevelType.values().length];

    public SceneGridSightModule(Scene scene) {
        super(scene);
    }

    /**
     * 初始化组件
     * beginPos sceneSize 里面存储的都是实际距离
     **/
    @Override
    public void init(Vector2 beginPos, Vector2 sceneSize) {
        super.init(beginPos, sceneSize);

        for (ProtoCommon.VisionLevelType rangeType : ProtoCommon.VisionLevelType.values())
        {
            int gridWidth = getGridWidth(rangeType);
            int sightRange = getSightRange(rangeType);
            if (gridWidth <= 0 || sightRange <= 0)
            {
                throw new UnknownLogicException("type: " + rangeType + " gridWidth or sightRange valid!");
            }

            // 校验格子是否可以整除
            if (sightRange / gridWidth * gridWidth != sightRange)
            {
                throw new UnknownLogicException(sightRange + "/" + gridWidth + " divide not exactly!");
            }

            // 初始化各级管理器
            createGridMgr(rangeType, gridWidth, sightRange);
        }
    }

    public static int getGridWidth(ProtoCommon.VisionLevelType type)
    {
        switch (type) {
            case VISION_LEVEL_NORMAL -> {
                return 20;
            }
            case VISION_LEVEL_LITTLE_REMOTE -> {
                return 40;
            }
            case VISION_LEVEL_REMOTE -> {
                return 250;
            }
            case VISION_LEVEL_SUPER -> {
                return 1000;
            }
            case VISION_LEVEL_NEARBY -> {
                return 20;
            }
            case VISION_LEVEL_SUPER_NEARBY -> {
                return 20;
            }
            default -> {
                return 0;
            }
        }
    }

    public static int getSightRange(ProtoCommon.VisionLevelType type)
    {
        switch (type) {
            case VISION_LEVEL_NORMAL -> {
                return 80;
            }
            case VISION_LEVEL_LITTLE_REMOTE -> {
                return 160;
            }
            case VISION_LEVEL_REMOTE -> {
                return 1000;
            }
            case VISION_LEVEL_SUPER -> {
                return 4000;
            }
            case VISION_LEVEL_NEARBY -> {
                return 40;
            }
            case VISION_LEVEL_SUPER_NEARBY -> {
                return 20;
            }
            default -> {
                return 0;
            }
        }
    }

    private void createGridMgr(ProtoCommon.VisionLevelType rangeType, int gridWidth, int sightRange)
    {
        GridMgr gridMgr = new GridMgr(getScene(), rangeType);
        if (0 == gridWidth)
        {
            throw new UnknownLogicException("gridWidth == 0");
        }
        // 把距离大小转化成格子单位
        int length = ((int) sceneSize.getX() + gridWidth - 1) / gridWidth;
        if (length <= 0 || length > UINT16_MAX)
        {
            throw new UnknownLogicException("invalid length: " + length);
        }
        int width = ((int) sceneSize.getY() + gridWidth - 1) / gridWidth;
        if (width <= 0 || width > UINT16_MAX)
        {
            throw new UnknownLogicException("invalid width: " + width);
        }
        int sightRadius = sightRange / gridWidth;
        if (0 == sightRadius)
        {
            sightRadius = 1;
        }
        gridMgr.init(length, width, sightRadius);
        gridMgrs[rangeType.getNumber()] = gridMgr;
        log.debug("GridMgr::init length: " + length + " width: " + width + " sightRadius: " + sightRadius
                + " vision_level: " + rangeType);
    }

    @Override
    public Collection<Entity> placeEntity(Entity entity) {
        return List.of();
    }

    @Override
    public Collection<Entity> removeEntity(Entity entity) {
        return List.of();
    }

    @Override
    public EntityMoveToResult entityMoveTo(Entity entity, Vector3 dest_pos) {
        return null;
    }

    @Override
    public void visitGridsInSight(Entity entity, Visitor visitor) {

    }

    @Override
    public boolean isEntityMoveGrid(Entity entity, Vector3 prev_pos, int prevRoom) {
        return false;
    }

    @Override
    public Set<Region> findPossibleRegionSet(Vector3 pos, int roomId) {
        return Set.of();
    }
}
