package com.ares.nk2.component;

import com.ares.nk2.component.optional.ComponentOptional;
import com.ares.nk2.tool.NKStringFormater;

/**
 * @author jeffreyzhou
 * @date 2021/6/21
 */
public class ComponentOwner {
    private final BaseEntity ownerEntity;

    public ComponentOwner(BaseEntity ownerEntity) {
        this.ownerEntity = ownerEntity;
    }

    public long getEntityId() {
        return ownerEntity.getEntityId();
    }

    public <T extends BaseComponent> ComponentOptional<T> tryGetCompt(Class<T> clazz) {
        return ownerEntity.tryGetCompt(clazz);
    }

    public <T extends BaseComponent> boolean hasCompt(Class<T> clazz) {
        return ownerEntity.hasCompt(clazz);
    }

    public boolean isDestroyed() {
        return ownerEntity.isDestroyed();
    }

    public boolean isDestroying() {
        return ownerEntity.isDestroying();
    }

    @Override
    public final String toString() {
        return NKStringFormater.format("[{}, id={}]", ownerEntity.getClass().getSimpleName(), ownerEntity.getEntityId());
    }

    void comptDependencyConfig(BaseComponentDependency componentDependency) {
        componentDependency.setDepEntity(ownerEntity);
    }

    public boolean shouldPrintLog() {
        return ownerEntity.shouldPrintLog();
    }

    public BaseEntity getEntity() {
        return ownerEntity;
    }
}
