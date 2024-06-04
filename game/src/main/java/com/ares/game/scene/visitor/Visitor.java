package com.ares.game.scene.visitor;

import com.ares.game.scene.entity.Entity;

import java.util.ArrayList;
import java.util.List;

// 格子访问者(Visitor设计模式)
public abstract class Visitor {
    private static final int VISITOR_RESULT_LIST_INIT_SIZE = 10;

    protected final Entity selfEntity;            // 发起访问的实体, 获取其周边相关的entity
    protected final List<Entity> resultList;  // 访问结果列表

    public Visitor(Entity selfEntity) {
        this.selfEntity = selfEntity;
        this.resultList = new ArrayList<>(VISITOR_RESULT_LIST_INIT_SIZE);
    }

    public Entity getSelfEntity() {
        return selfEntity;
    }

    public abstract VisitorType getType();

    // 访问一个实体
    public int visitEntity(Entity entity) {
        if (canAddEntity(entity))
        {
            resultList.add(entity);
        }
        return 0;
    }

    // 获取结果实体列表
    public List<Entity> getResultList() {
        return resultList;
    }

    // 在结果中获取某一类型的所有实体
    public void getResultList(List<Entity> resultList, Class<? extends Entity> clazz)
    {
        for(Entity entity : this.resultList) {
            if (clazz.isAssignableFrom(entity.getClass())) {
                resultList.add(entity);
            }
        }
    }

    protected abstract boolean canAddEntity(Entity entity);
}
