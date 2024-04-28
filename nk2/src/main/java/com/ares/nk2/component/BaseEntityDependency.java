package com.ares.nk2.component;

import com.ares.nk2.component.optional.ComponentOptional;
import com.ares.nk2.tool.NKStringFormater;

/**
 * @author jeffreyzhou
 * @date 2021/6/22
 */
public class BaseEntityDependency {
    private final BaseEntity ownerEntity;

    public BaseEntityDependency(BaseEntity ownerEntity) {
        this.ownerEntity = ownerEntity;
    }


    protected <T extends BaseComponent> T getCompt(Class<T> clazz) {
        ComponentOptional<T> compt = ownerEntity.tryGetCompt(clazz);
        if (compt.isNull()) {
            throw new NullPointerException(NKStringFormater.format("entity-{} depend on component-{}, but not found"
                    , ownerEntity.getClass().getSimpleName(), clazz.getSimpleName()));
        } else {
            return compt.get();
        }
    }
}
