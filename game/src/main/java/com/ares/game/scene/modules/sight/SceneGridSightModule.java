package com.ares.game.scene.modules.sight;

import com.ares.common.math.Coordinate;
import com.ares.common.math.Vector2;
import com.ares.common.math.Vector3;
import com.ares.common.util.ForeachPolicy;
import com.ares.core.exception.UnknownLogicException;
import com.ares.game.scene.Grid;
import com.ares.game.scene.GridMgr;
import com.ares.game.scene.Region;
import com.ares.game.scene.Scene;
import com.ares.game.scene.entity.Entity;
import com.ares.game.scene.entity.avatar.Avatar;
import com.ares.game.scene.visitor.VisitEntityVisitor;
import com.ares.game.scene.visitor.Visitor;
import com.ares.game.scene.visitor.VisitorType;
import com.game.protoGen.ProtoCommon;
import com.game.protoGen.ProtoScene;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.ares.common.math.MathUtil.UINT16_MAX;

public class SceneGridSightModule extends SceneSightModule {
    private static final Logger log = LoggerFactory.getLogger(SceneGridSightModule.class);

    private final GridMgr[] gridMgrs = new GridMgr[ProtoCommon.VisionLevelType.VISION_LEVEL_NUM_VALUE];

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

        for (int i = 0; i < gridMgrs.length; i++)
        {
            ProtoCommon.VisionLevelType rangeType = ProtoCommon.VisionLevelType.forNumber(i);
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

    private GridMgr getGridMgr(ProtoCommon.VisionLevelType rangeType)
    {
        if (rangeType.getNumber() >= gridMgrs.length)
        {
            throw new UnknownLogicException("invalid rangeType: " + rangeType);
        }
        return gridMgrs[rangeType.getNumber()];
    }

    @Override
    public Collection<Entity> placeEntity(Entity entity) {
        ProtoCommon.VisionLevelType rangeType = getVisionLevelType(entity);
        GridMgr gridMgr = getGridMgr(rangeType);
        if (gridMgr == null)
        {
            throw new UnknownLogicException("gridMgr is null, rangeType:" + rangeType);
        }

        // region对象需要特殊处理
        if (entity.getEntityType() == ProtoScene.ProtEntityType.PROT_ENTITY_REGION)
        {
//            vector<Coordinate> coord_vec;
//            Region region = (Region)entity;
//            region.getCoveredCoordinates(*this, coord_vec);
//            gridMgr.placeRegionEntity(region, coord_vec);
//            entity.setScene(scene_.shared_from_this());
            return List.of();
        }

        // 放入格子中
        Vector3 pos = entity.getPosition();
        Coordinate coord = posToCoordinate(rangeType, Vector2.fromVector3(pos));
        if (0 != gridMgr.placeEntity(entity, coord))
        {
            throw new UnknownLogicException("rangeType: " + rangeType + " placeEntity failed" + entity);
        }
        // 非room模式，需要清理room_id
        entity.setRoomId(0);
        //设置场景 (后续getSurroundEntityVec里面要用到)
        entity.setScene(getScene());

        // 如果当前没有玩家，可以不需要计算
        if (0 == getScene().getPlayerCount())
        {
            return List.of();
        }
        // 计算附近的entity
        return getSurroundEntityList(entity);
    }

    @Override
    public Collection<Entity> removeEntity(Entity entity) {
        ProtoCommon.VisionLevelType rangeType = getVisionLevelType(entity);
        GridMgr gridMgr = getGridMgr(rangeType);
        if (gridMgr == null)
        {
            throw new UnknownLogicException("gridMgr is null, rangeType:" + rangeType);
        }

        // region对象需要特殊处理
        if (entity.getEntityType() == ProtoScene.ProtEntityType.PROT_ENTITY_REGION)
        {
//            vector<Coordinate> coord_vec;
//            Region region = (Region)entity;
//            region.getCoveredCoordinates(*this, coord_vec);
//            grid_mgr_utr.removeRegionEntity(region, coord_vec);
            return List.of();
        }

        // 从格子中删除
        if (0 != gridMgr.removeEntity(entity))
        {
            throw new UnknownLogicException("rangeType: " + rangeType + " removeEntity failed" + entity);
        }

        // 如果当前没有玩家，可以不需要计算
        if (0 == getScene().getPlayerCount())
        {
            return List.of();
        }

        // 计算附近的entity
        return getSurroundEntityList(entity);
    }

    @Override
    public EntityMoveToRet entityMoveTo(Entity entity, Vector3 destPos) {
        EntityMoveToRet ret = new EntityMoveToRet();
        ProtoCommon.VisionLevelType rangeType = getVisionLevelType(entity);
        GridMgr gridMgr = getGridMgr(rangeType);
        if (gridMgr == null)
        {
            throw new UnknownLogicException("gridMgr is null, rangeType:" + rangeType);
        }

        Coordinate destCoord = posToCoordinate(rangeType, Vector2.fromVector3(destPos));
        if (0 != gridMgr.entityMoveTo(entity, destCoord))
        {
            throw new UnknownLogicException("range_type:" + rangeType + " entityMoveTo failed." + entity
                + " dest_pos:" + destPos + " destCoord:" + destCoord + " scene_begin_pos:" + beginPos);
        }

        VisitEntityVisitor missVisitor = new VisitEntityVisitor(entity);
        VisitEntityVisitor meetVisitor = new VisitEntityVisitor(entity);

        // PROT_ENTITY_EYE_POINT对象不会移动，无需处理
        if (entity.getEntityType() == ProtoScene.ProtEntityType.PROT_ENTITY_AVATAR)
        {
            visitDiffGrids(entity.getPosition(), destPos, missVisitor, meetVisitor);
        }
        else
        {
            Vector3 curPos = entity.getPosition();

            Coordinate cur_coord = posToCoordinate(rangeType, Vector2.fromVector3(curPos));
            Coordinate dest_coord = posToCoordinate(rangeType, Vector2.fromVector3(destPos));
            if (cur_coord == dest_coord)
            {
                return ret;
            }

            // 直接遍历player(数量 < 格子数)
            // 非avatar只需被player看到，不需要看到其他对象，取player_eye_entity就行
            getScene().foreachPlayer(player -> {
                Entity eyeEntity = getScene().getPlayerEyeEntity(player.getUid());
                if (eyeEntity == null)
                {
                    return ForeachPolicy.CONTINUE;
                }

                boolean inCurSight = isInSightRange(rangeType, curPos, eyeEntity.getPosition());
                boolean inDestSight = isInSightRange(rangeType, destPos, eyeEntity.getPosition());
                if (inCurSight && !inDestSight)
                {
                    missVisitor.visitEntity(eyeEntity);
                }
                if (!inCurSight && inDestSight)
                {
                    meetVisitor.visitEntity(eyeEntity);
                }
                return ForeachPolicy.CONTINUE;
            });
        }

        ret.missEntities = missVisitor.getResultList();
        ret.meetEntities = meetVisitor.getResultList();
        return ret;
    }

    // 计算附近的entity
    private List<Entity> getSurroundEntityList(Entity entity)
    {
        VisitEntityVisitor visitor = new VisitEntityVisitor(entity);
        visitGridsInSight(entity, visitor);

        return visitor.getResultList();
    }

    @Override
    public void visitGridsInSight(Entity entity, Visitor visitor) {
        Vector3 pos = entity.getPosition();
        VisitorType visitorType = visitor.getType();
        switch (visitorType)
        {
            case VISIT_ENTITY_VISITOR:
                if (entity.getEntityType() != ProtoScene.ProtEntityType.PROT_ENTITY_AVATAR
                        && entity.getEntityType() != ProtoScene.ProtEntityType.PROT_ENTITY_EYE_POINT)
                {
                    // 非角色或eye_point的实体，只需要查找周围的player_eye_entity即可
                    visitPlayerEyeEntityDirectly(entity, visitor);
                    return;
                }
                break;
            case VISIT_AVATAR_VISITOR:
            case VISIT_EXCLUDE_SELF_AVATAR_VISITOR:
                // 查找周围的角色实体，用直接的方式
                visitAvatarDirectly(entity, visitor);
                return;
            default:
                break;
        }
        // 剩余的情况，走通用的流程
        for (int i = 0; i < gridMgrs.length; i++)
        {
            ProtoCommon.VisionLevelType rangeType = ProtoCommon.VisionLevelType.forNumber(i);
            GridMgr gridMgr = gridMgrs[i];
            Coordinate coord = posToCoordinate(rangeType, Vector2.fromVector3(entity.getPosition()));
            gridMgr.visitGridsInSight(coord, visitor, 0);
        }
    }

    @Override
    public boolean isEntityMoveGrid(Entity entity, Vector3 prevPos, int prevRoom) {
        for (int i = 0; i < gridMgrs.length; i++)
        {
            ProtoCommon.VisionLevelType rangeType = ProtoCommon.VisionLevelType.forNumber(i);
            Coordinate coord = posToCoordinate(rangeType, Vector2.fromVector3(entity.getPosition()));
            Coordinate prevCoord = posToCoordinate(rangeType, Vector2.fromVector3(prevPos));
            if(coord.equals(prevCoord))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public Set<Region> findPossibleRegionSet(Vector3 pos, int roomId) {
        Set<Region> regionSet = new HashSet<>();
        for (int i = 0; i < gridMgrs.length; i++)
        {
            ProtoCommon.VisionLevelType rangeType = ProtoCommon.VisionLevelType.forNumber(i);
            GridMgr gridMgr = gridMgrs[i];
            Coordinate coord = posToCoordinate(rangeType, Vector2.fromVector3(pos));
            Grid grid = gridMgr.getGrid(coord);
            if (grid == null)
            {
                log.error("getGrid fails, cur_coord:" + coord);
                continue;
            }
            regionSet.addAll(grid.getAllRegion());
        }
        return regionSet;
    }

    // 是否在视野距离内
    private boolean isInSightRange(ProtoCommon.VisionLevelType range_type, Vector3 pos, Vector3 farPos)
    {
        int sight_range = getSightRange(range_type);
        int grid_width = getGridWidth(range_type);
        int sight_radius = 1;
        if (grid_width != 0)
        {
            sight_radius = sight_range / grid_width;
        }
        if (0 == sight_radius)
        {
            sight_radius = 1;
        }

        Coordinate coord = posToCoordinate(range_type, Vector2.fromVector3(pos));
        Coordinate far_coord = posToCoordinate(range_type, Vector2.fromVector3(farPos));
        return Math.abs(coord.getX() - far_coord.getX()) <= sight_radius && Math.abs(coord.getY() - far_coord.getY()) <= sight_radius;
    }

    // 直接访问Avatar(不通过格子)
    private void visitAvatarDirectly(Entity entity, Visitor visitor)
    {
        // 如果entity处于更高的视野，直接通过视野距离获取avatar
        ProtoCommon.VisionLevelType rangeType = getVisionLevelType(entity);
        // 直接遍历player(数量 < 格子数)
        getScene().foreachPlayer(player -> {
            Avatar avatar = player.getCurAvatar();
            if (avatar != null && avatar.isOnScene() && isInSightRange(rangeType, entity.getPosition(), avatar.getPosition()))
            {
                visitor.visitEntity(avatar);
            }
            return ForeachPolicy.CONTINUE;
        });
    }

    // 直接访问PlayerEyeEntity(不通过格子)
    private void visitPlayerEyeEntityDirectly(Entity entity, Visitor visitor)
    {
        // 如果entity处于更高的视野，直接通过视野距离获取eye_entity
        ProtoCommon.VisionLevelType rangeType = getVisionLevelType(entity);
        // 直接遍历player(数量 < 格子数)
        getScene().foreachPlayer(player -> {
            Entity eyeEntity = getScene().getPlayerEyeEntity(player.getUid());
            if (eyeEntity != null && isInSightRange(rangeType, entity.getPosition(), eyeEntity.getPosition()))
            {
                visitor.visitEntity(eyeEntity);
            }
            return ForeachPolicy.CONTINUE;
        });
    }

    // 查询两个position entity集合的差集：减少以及新增
    private void visitDiffGrids(Vector3 fromPos, Vector3 toPos, Visitor missVisitor, Visitor meetVisitor)
    {
        for (int i = 0; i < gridMgrs.length; i++)
        {
            ProtoCommon.VisionLevelType rangeType = ProtoCommon.VisionLevelType.forNumber(i);
            Coordinate fromCoord = posToCoordinate(rangeType, Vector2.fromVector3(fromPos));
            Coordinate toCoord = posToCoordinate(rangeType, Vector2.fromVector3(toPos));
            if (fromCoord.equals(toCoord))
            {
                continue;
            }
            gridMgrs[i].visitDiffGrids(fromCoord, toCoord, missVisitor, meetVisitor);
        }
    }

    private ProtoCommon.VisionLevelType getVisionLevelType(Entity entity)
    {
        return entity.getVisionLevelType();
    }

    private Coordinate posToCoordinate(ProtoCommon.VisionLevelType rangeType, Vector2 pos)
    {
        int gridWidth = getGridWidth(rangeType);
        if (0 == gridWidth)
        {
            throw new UnknownLogicException("0 == gridWidth");
        }

        int x = (int)((pos.getX() - beginPos.getX()) / gridWidth);
        int y = (int)((pos.getY() - beginPos.getY()) / gridWidth);
        return new Coordinate(x, y);
    }
}
