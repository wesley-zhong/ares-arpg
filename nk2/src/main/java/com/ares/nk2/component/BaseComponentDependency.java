package com.ares.nk2.component;

import com.ares.nk2.component.optional.ComponentOptional;
import com.ares.nk2.tool.NKStringFormater;

/**
 * @author jeffreyzhou
 * @date 2021/6/3
 */
public class BaseComponentDependency {
    private final BaseComponent ownerCompt;
    protected BaseEntity depEntity = null;

    public BaseComponentDependency(BaseComponent ownerCompt) {
        this.ownerCompt = ownerCompt;
    }

    protected BaseComponent getOwnerCompt() {
        return ownerCompt;
    }

    protected <T extends BaseComponent> T getDependencyCompt(Class<T> clazz) {
        ComponentOptional<T> compt = ownerCompt.tryGetEntityCompt(clazz);
        if (compt.isNull()) {
            throw new NullPointerException(NKStringFormater.format("component-{} depend on component-{}, but not found"
                    , ownerCompt.getClass().getSimpleName(), clazz.getSimpleName()));
        } else {
            return compt.get();
        }
    }

    void setDepEntity(BaseEntity depEntity) {
        this.depEntity = depEntity;
    }
}
