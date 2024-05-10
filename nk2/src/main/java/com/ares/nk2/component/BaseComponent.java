package com.ares.nk2.component;

import com.ares.nk2.component.optional.ComponentOptional;
import com.ares.nk2.tool.NKStringFormater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jeffreyzhou
 * @date 2021/7/5
 */
public abstract class BaseComponent implements DependableInterface {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseComponent.class);

    /**
     * 框架层不做任何保证，仅提供一个接口
     */
    private boolean isDisabled = false;

    private ComponentOwner componentOwner = null;

    public BaseComponent() {
    }

    public final void setOwner(BaseEntity entityOwner) {
        componentOwner = new ComponentOwner(entityOwner);
    }

    public final ComponentOwner getOwner() {
        return componentOwner;
    }

    public final long getEntityId() {
        return componentOwner.getEntityId();
    }

    public final <T extends BaseComponent> ComponentOptional<T> tryGetEntityCompt(Class<T> clazz) {
        return componentOwner.tryGetCompt(clazz);
    }

    public final <T extends BaseComponent> boolean hasCompt(Class<T> clazz) {
        return componentOwner.hasCompt(clazz);
    }


    /**
     * 框架层不做任何保证，仅提供一个接口
     */

    public boolean isDisabled() {
        return isDisabled;
    }

    /**
     * 框架层不做任何保证，仅提供一个接口
     */

    public void setDisabled(boolean disabled) {
        isDisabled = disabled;
    }

    public final void comptDependencyConfig(BaseComponentDependency componentDependency) {
        componentOwner.comptDependencyConfig(componentDependency);
    }

    public BaseEntity getOwnerEntity() {
        return getOwner().getEntity();
    }

    /**
     * 禁止新增使用
     *
     * @return
     */
    @Deprecated
    protected final <T> T doNotCallDeprecatedGetEntity(Class<T> clazz) {
        BaseEntity baseEntity = getOwner().getEntity();
        if (clazz.isInstance(baseEntity)) {
            return clazz.cast(baseEntity);
        } else {
            LOGGER.error("baseEntity: {} is not instance of clazz: {}",
                    baseEntity.getClass().getSimpleName(), clazz.getSimpleName());
            return null;
        }
    }

    @Override
    public String toString() {
        long entityId = getEntityId();
        Class<? extends BaseComponent> clazz = getClass();
        return NKStringFormater.format("compt[entityId-{},clazz-{}]",
                entityId, clazz.getSimpleName());
    }
}
