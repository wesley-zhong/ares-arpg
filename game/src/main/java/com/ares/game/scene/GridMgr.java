package com.ares.game.scene;

import com.ares.common.math.Coordinate;
import com.ares.common.math.Mesh;
import com.ares.game.scene.entity.Entity;
import com.ares.game.scene.visitor.Visitor;
import com.game.protoGen.ProtoCommon;
import com.game.protoGen.ProtoScene;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public final class GridMgr {
    private static final Logger log = LoggerFactory.getLogger(GridMgr.class);

    private final Scene scene;
    private final ProtoCommon.VisionLevelType rangeType;
    private int length = 0;           // 以格子为单位
    private int width = 0;            // 以格子为单位
    private int sightRadius = 0;     // 视距(以格子为单位)
    Mesh<Grid> mesh;             // 场景的二维网格(二维数组存储)

    public GridMgr(final Scene scene, final ProtoCommon.VisionLevelType rangeType) {
        this.scene = scene;
        this.rangeType = rangeType;
    }

    @Override
    public String toString() {
        return "[scene_id:" + scene.getSceneId() + ",range_type:" + rangeType
                + ",length:" + length + ",width:" + width + ",sightRadius:" + sightRadius
                + "]";
    }

    public void init(int length, int width, int sightRadius)
    {
        this.length = length;
        this.width = width;
        this.sightRadius = sightRadius;
        this.mesh = Mesh.createMesh(Grid.class, length, width);
    }

    public int placeEntity(Entity entity, Coordinate coord)
    {
        Grid grid = getGrid(coord);
        if (null == grid)
        {
            log.warn("getGrid fails. entity:" + entity + " coord:" + coord);
            return -1;
        }
        if (0 != grid.addEntity(entity))
        {
            log.warn("addEntity fails. entity:" + entity + " coord:" + coord);
            return -1;
        }
        entity.setCoordinate(coord);
        if (entity.getEntityType() == ProtoScene.ProtEntityType.PROT_ENTITY_AVATAR)
        {
            log.debug("[GRID] place avatar:" + entity + " @ " + grid);
        }
        if (log.isDebugEnabled()) {
            log.debug("[FY] place entity_id:" + entity.getEntityId() + " @ " + entity.getCoordinate());
        }
        return 0;
    }

    public int placeRegionEntity(Region region,  Collection<Coordinate> coords)
    {
        for (Coordinate coord : coords)
        {
            Grid grid = getGrid(coord);
            if (null == grid)
            {
                log.warn("getGrid fails. region:" + region + " coord:" + coord);
                continue;
            }
            if (0 != grid.addRegion(region))
            {
                log.warn("addRegion fails. region:" + region + " coord:" + coord);
            }
            if (null == region.getGrid())
            {
                region.setGrid(grid);
            }
        }
        // ZT_TODO
        // 添加范围内的avatar到region内
//        scene_.foreachPlayer([&region](Player & player)->ForeachPolicy
//        {
//            do {
//                AvatarPtr avatar_ptr = player.getCurAvatar();
//                if (avatar_ptr == null)
//                {
//                    LOG_DEBUG << "[REGION] place region:" << region << " foreachPlayer. uid:" << player.getUid() << " cur_avatar is null";
//                    break;
//                }
//                if (!avatar_ptr->isOnScene())
//                {
//                    LOG_DEBUG << "[REGION] place region:" << region << " foreachPlayer. uid:" << player.getUid() << " cur_avatar is not on scene";
//                    break;
//                }
//                if (!region.isInRegion(avatar_ptr->getPosition()))
//                {
//                    LOG_DEBUG << "[REGION] place region:" << region << " foreachPlayer. uid:" << player.getUid() << " cur_avatar is not in region";
//                    break;
//                }
//                LOG_DEBUG << "[REGION] place region:" << region << " foreachPlayer. uid:" << player.getUid() << " add cur_avatar:" << *avatar_ptr;
//                region.addEntity(*avatar_ptr, true);
//            } while(0);
//            return FOREACH_CONTINUE;
//        });
        log.debug("[REGION] place region:" + region + " vision_level:" + region.getVisionLevelType() + " coords size:" + coords.size());
        return 0;
    }

    public int removeEntity(Entity entity)
    {
        Grid grid = entity.getGrid();
        if (null == grid)
        {
            log.error("grid is null: " + entity);
            return 0;
        }
        if (0 != grid.delEntity(entity))
        {
            log.error("delEntity fails:" + entity);
            return -1;
        }
        if (entity.getEntityType() == ProtoScene.ProtEntityType.PROT_ENTITY_AVATAR)
        {
            log.debug("[GRID] remove avatar:" + entity + " @ " + grid);
        }
        if (log.isDebugEnabled()) {
            log.debug("[FY] remove entity_id:" + entity.getEntityId() + " @ " + entity.getCoordinate());
        }
        return 0;
    }

    public int removeRegionEntity(Region region, Collection<Coordinate> coords)
    {
        for (Coordinate coord : coords)
        {
            Grid grid = getGrid(coord);
            if (null == grid)
            {
                log.warn("getGrid fails. region:" + region + " coord:" + coord);
                continue;
            }
            if (0 != grid.delRegion(region))
            {
                log.warn("delRegion fails" + region);
            }
        }
        region.setGrid(null);
        log.debug("[REGION] remove region:" + region + " vision_level:" + region.getVisionLevelType() + " coords size:" + coords.size());
        return 0;
    }

    public int entityMoveTo(Entity entity, Coordinate destCoord)
    {
        Coordinate curCoord = entity.getCoordinate();
        Grid curGrid = getGrid(curCoord);
        if (null == curGrid)
        {
            log.warn("curCoord:" + curCoord + " grid doesn't exist! grid_mgr:" + this);
            return -1;
        }
        Grid destGrid = getGrid(destCoord);
        if (null == destGrid)
        {
            log.warn("destCoord:" + destCoord + " grid doesn't exist! grid_mgr:" + this);
            return -1;
        }
        if (curGrid == destGrid)
        {
            return 0;
        }
        // 进入新的格子
        if (0 != curGrid.delEntity(entity))
        {
            log.warn("delEntity fails" + entity);
        }
        if (0 != destGrid.addEntity(entity))
        {
            log.warn("addEntity fails" + entity);
            return -1;
        }
        entity.setCoordinate(destCoord);
        return 0;
    }

    // 获取整个场景中满足visitor条件的entity集合
    public void visitGridsInSight(Coordinate center, Visitor visitor, int sightRadius)
    {
        if (0 == sightRadius)
        {
            sightRadius = this.sightRadius;
        }
        for (int i = Math.max(0, center.getX() - sightRadius); i <= center.getX() + sightRadius; ++i)
        {
            for (int j = Math.max(0, center.getY() - sightRadius); j <= center.getY() + sightRadius; ++j)
            {
                visitGrid(i, j, visitor);
            }
        }
    }

    /**
     * 计算从from_coord到to_coord，entity集合的减少和新增
     * center1: from_coord, center2: to_coord
     * t1: miss_visitor, t2: add_visitor
     **/
    public void visitDiffGrids(Coordinate center1, Coordinate center2, Visitor t1, Visitor t2)
    {
        int left_x1 = center1.getX() - sightRadius, right_x1 = center1.getX() + sightRadius;
        int low_y1 = center1.getY() - sightRadius, up_y1 = center1.getY() + sightRadius;
        int left_x2 = center2.getX() - sightRadius, right_x2 = center2.getX() + sightRadius;
        int low_y2 = center2.getY() - sightRadius, up_y2 = center2.getY() + sightRadius;

        if (right_x1 < left_x2 || right_x2 < left_x1 || up_y1 < low_y2 || up_y2 < low_y1)
        {
            for (int i = left_x1; i <= right_x1; ++i)
            {
                for (int j = low_y1; j <= up_y1; ++j)
                    visitGrid(i, j, t1);
            }
            for (int i = left_x2; i <= right_x2; ++i)
            {
                for (int j = low_y2; j <= up_y2; ++j)
                    visitGrid(i, j, t2);
            }
            return;
        }
        int low_y = low_y1, up_y = up_y1;
        if (center1.getY() < center2.getY())
        {
            for (int j = low_y1; j < low_y2; ++j)
            {
                for (int i = left_x1; i <= right_x1; ++i)
                    visitGrid(i, j, t1);
            }
            for (int j = up_y2; j > up_y1; --j)
            {
                for (int i = left_x2; i <= right_x2; ++i)
                    visitGrid(i, j, t2);
            }
            low_y = low_y2;
            up_y = up_y1;
        }
        else if (center1.getY() > center2.getY())
        {
            for (int j = up_y1; j > up_y2; --j)
            {
                for (int i = left_x1; i <= right_x1; ++i)
                    visitGrid(i, j, t1);
            }
            for (int j = low_y2; j < low_y1; ++j)
            {
                for (int i = left_x2; i <= right_x2; ++i)
                    visitGrid(i, j, t2);
            }
            low_y = low_y1;
            up_y = up_y2;
        }
        for (int j = low_y; j <= up_y; ++j)
        {
            if (center1.getX() < center2.getX())
            {
                for (int i = left_x1; i < left_x2; ++i)
                    visitGrid(i, j, t1);
                for (int i = right_x2; i > right_x1; --i)
                    visitGrid(i, j, t2);
            }
            else if (center1.getX() > center2.getX())
            {
                for (int i = left_x2; i < left_x1; ++i)
                    visitGrid(i, j, t2);
                for (int i = right_x1; i > right_x2; --i)
                    visitGrid(i, j, t1);
            }
        }
    }

    private void visitGrid(int x, int y, Visitor visitor)
    {
        Grid grid = getGrid(x, y);
        if (grid != null)
        {
            grid.accept(visitor);
        }
    }

    public Grid getGrid(int x, int y)
    {
        if (mesh == null)
            return null;
        return mesh.getGrid(x, y);
    }

    public Grid getGrid(Coordinate coord)
    {
        return getGrid(coord.getX(), coord.getY());
    }
}
