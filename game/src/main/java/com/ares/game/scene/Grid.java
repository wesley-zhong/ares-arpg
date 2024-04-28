package com.ares.game.scene;

import com.ares.common.util.ForeachPolicy;
import com.ares.game.scene.entity.Entity;
import com.ares.game.scene.visitor.Visitor;
import io.netty.util.collection.LongObjectHashMap;
import io.netty.util.collection.LongObjectMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public final class Grid {
    private static final Logger log = LoggerFactory.getLogger(Grid.class);

    private LongObjectMap<Entity> entityMap = null;
    private Set<Region> regions = null;

    // 接受访问者(Visitor设计模式)
    public void accept(Visitor t)
    {
        if (entityMap == null || entityMap.isEmpty())
            return;

        for (Entity e : entityMap.values()) {
            e.accept(t);
        }
    }

    // 清理
    public void clear()
    {
        entityMap = null;
        regions = null;
    }

    // 添加实体
    public int addEntity(Entity entity)
    {
        log.debug("[GRID] add entity:entity_id:" + entity.getEntityId() + " grid:" + this);
        if (entity.getGrid() != null)
        {
            log.error("[GRID] cur_grid not null. entity:" + entity + " cur_grid:" + entity.getGrid() + " and this_grid:" + this);
        }

        if (entityMap == null)
        {
            entityMap = new LongObjectHashMap<>();
        }
        Entity curEntity = entityMap.get(entity.getEntityId());
        if (curEntity != null)
        {
            // 这个分支都是异常情况，为了排查bug，日志记录详细一点
            log.error("[GRID] entity already exists. this_entity:" + entity + " grid:" + this);
            if (curEntity == entity)
            {
                // 当前entity是有效，并且和进入的entity相同，setGrid就行，对于调用方是成功的
                log.error("[GRID] cur_entity == this_entity. this_entity:" + entity + " grid:" + this);

            }
            else
            {
                // 当前entity是有效的，并且和进入的entity不相同，不能进入，对于调用方是失败的
                log.error("[GRID] cur_entity != this_entity. cur_entity:" + curEntity + " this_entity:" + entity + " grid:" + this);
                return -1;
            }
        }
        else
        {
            entityMap.put(entity.getEntityId(), entity);
        }
        entity.setGrid(this);
        return 0;
    }

    // 删除实体
    public int delEntity(Entity entity)
    {
        log.debug("[GRID] del entity:entity_id:" + entity.getEntityId() + " grid:" + this);
        do {
            if (entity.getGrid() != this)
            {
                log.error("[GRID] grid is different. entity:" + entity + " cur_grid:" + entity.getGrid() + " and this_grid:" + this);
            }
            if (entityMap == null)
            {
                log.error("[GRID] entity_info_ptr_ is null");
                break;
            }
            Entity curEntity = entityMap.get(entity.getEntityId());
            if (curEntity != null)
            {
                if (curEntity == entity)
                {
                    // 正常情况
                    entityMap.remove(entity.getEntityId());
                }
                else
                {
                    // 当前entity是有效的，并且和进入的entity不相同，不能删除
                    log.error("[GRID] cur_entity != this_entity. cur_entity:" + curEntity + " this_entity:" + entity + " grid:" + this);
                }
            }
            else
            {
                log.error("[GRID] entity doesn't exist. entity " + entity + " grid:" + this);
            }
        } while(false);
        // 删除entity对于调用方肯定成功
        entity.setGrid(null);
        return 0;
    }

    // 添加区域
    public int addRegion(Region region)
    {
        if (regions == null)
        {
            regions = new HashSet<>();
        }
        if (!regions.add(region))
        {
            log.error("[GRID] region already exists" + region);
            return -1;
        }
        return 0;
    }

    // 删除区域
    public int delRegion(Region region)
    {
        if (regions == null)
        {
            log.error("[GRID] region_info_ptr_ is null");
            return -1;
        }
        if (!regions.remove(region))
        {
            log.error("[GRID] region not exists" + region);
            return -1;
        }
        return 0;
    }

    // 是否有实体
    public boolean hasEntity()
    {
        if (entityMap == null)
            return false;
        return !entityMap.isEmpty();
    }

    // 获取所有实体
    public Collection<Entity> getAllEntity()
    {
        if (entityMap == null)
            return Collections.emptySet();
        return entityMap.values();
    }

    // 获取所有区域
    public Set<Region> getAllRegion()
    {
        if (regions == null)
            return Collections.emptySet();
        return regions;
    }

    // 遍历所有的区域
    public int foreachRegion(Function<Region, ForeachPolicy> func)
    {
        if (regions == null || regions.isEmpty()) {
            return 0;
        }
        for (Region region : regions) {
            ForeachPolicy policy = func.apply(region);
            if (policy != ForeachPolicy.CONTINUE) {
                return 1;
            }
        }
        return 0;
    }
}
