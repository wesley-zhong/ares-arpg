package com.ares.nk2.component;

import com.ares.core.exception.FyLogicException;
import com.ares.nk2.component.optional.ComponentOptional;
import com.ares.nk2.tool.NKStringFormater;
import com.game.protoGen.ProtoErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.function.Consumer;

/**
 * Entity-Ability系统
 * 实体-能力 系统
 * BaseComponent代表Ability
 * 同一种Ability, 同一个Entity只需要也只可以有一个
 * 不包括BaseComponent, 有extends关系的Component属于同一种Ability
 */
public abstract class BaseEntity implements DependableInterface {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseEntity.class);
    private long entityId = 0L;
    ComponentOwner componentOwner = new ComponentOwner(this);

    private final EntityMetaData entityMetaDataRef;
    private final ArrayList<ComponentHolder> comptHolderList = new ArrayList<>();

    /**
     * <h>entity 状态</h>
     *
     * <p> warn:不要改成protected或public, 通过get setXXX 访问
     *
     * @see State
     */
    private State entityState = State.none;

    public BaseEntity() {
        entityMetaDataRef = EntityMetaDataMgr.getInstance().getEntityMetaData(getClass());
        if (entityMetaDataRef == null) {
            throw new FyLogicException(ProtoErrorCode.ErrCode.ENTITY_NOT_CONSTRUCT_CORRECT_VALUE
                    , NKStringFormater.format("entity-{} can not find meta data", getClass().getSimpleName()));
        }

        entityMetaDataRef.createCompts(this, comptHolderList);
    }

    public enum State {
        none,

        building,
        builtDone,
        builtFailed,

        destroying,
        destroyed,
    }

    @Override
    public String toString() {
        return NKStringFormater.format("[{}, id={}]", getClass().getSimpleName(), getEntityId());
    }

    public ComponentOwner getComponentOwner() {
        return componentOwner;
    }


    public <T extends BaseComponent> boolean hasCompt(Class<T> clazz) {
        return entityMetaDataRef.hasCompt(clazz);
    }

    /**
     * 请使用代码生成，或者tryget
     */
    @Deprecated
    protected final <T extends BaseComponent> T doNotCallDeprecatedGetCompt(Class<T> clazz) {
        int index = entityMetaDataRef.getComptIndex(clazz);
        if (index < 0 || index > comptHolderList.size()) {
            throw new FyLogicException(ProtoErrorCode.ErrCode.COMPONENT_NOT_EXIST_VALUE
                    , NKStringFormater.format("entity-{} not have component-{}", this, clazz.getSimpleName()));
        }

        ComponentHolder comptHolder = comptHolderList.get(index);
        return (T) comptHolder.getComponent();
    }

    /**
     * 尝试获取组件
     */
    public <T extends BaseComponent> ComponentOptional<T> tryGetCompt(Class<T> clazz) {
        ComponentHolder comptHolder = getComptHolder(clazz);
        if (comptHolder == null) {
            return ComponentOptional.of(null);
        } else {
            return ComponentOptional.of((T) comptHolder.getComponent());
        }
    }

    public <T extends BaseComponent> T getComptThrowExIfNotExists(Class<T> clazz) {
        ComponentOptional<T> comptOpt = tryGetCompt(clazz);
        if (comptOpt.isNull()) {
            throw new FyLogicException(ProtoErrorCode.ErrCode.COMPONENT_NOT_EXIST_VALUE
                    , NKStringFormater.format("entity:[{}] doesnt has component:[{}]", this, clazz));
        }
        return comptOpt.get();
    }

    private <T extends BaseComponent> ComponentHolder getComptHolder(Class<T> clazz) {
        int index = entityMetaDataRef.getComptIndex(clazz);
        if (index < 0 || index > comptHolderList.size()) {
            return null;
        }

        return comptHolderList.get(index);
    }

    public boolean isDestroyed() {
        return entityState == State.destroyed;
    }

    public boolean isDestroying() {
        return entityState == State.destroying;
    }

    public <T> void componentsForEach(Class<T> clazz, Consumer<T> function) {
        for (ComponentHolder comptHolder : comptHolderList) {
            BaseComponent c = comptHolder.getComponent();
            if (!clazz.isInstance(c)) {
                continue;
            }
            function.accept((T) c);
        }
    }

    public <T> void componentsForEachSafe(Class<T> clazz, Consumer<T> function) {
        for (ComponentHolder comptHolder : comptHolderList) {
            BaseComponent c = comptHolder.getComponent();
            if (!clazz.isInstance(c)) {
                continue;
            }
            try {
                function.accept((T) c);
            } catch (Exception e) {
                LOGGER.error("entity-{} component-{} run function {} failed.", this, c, function, e);
            }
        }
    }

    public void setEntityId(long entityId) {
        this.entityId = entityId;
    }

    public long getEntityId() {
        return entityId;
    }

    public boolean shouldPrintLog() {
        return true;
    }

    public State getEntityState() {
        return entityState;
    }

    public void setDestroyed() {
        entityState = State.destroyed;
    }

    public void setDestroying() {
        entityState = State.destroying;
    }

    public void setBuilding() {
        entityState = State.building;
    }

    public void setBuiltDone() {
        entityState = State.builtDone;
    }

    public void clearState() {
        entityState = State.none;
    }
}
