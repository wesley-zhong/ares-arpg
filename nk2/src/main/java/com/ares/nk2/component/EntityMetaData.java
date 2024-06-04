package com.ares.nk2.component;

import com.ares.core.exception.FyLogicException;
import com.ares.nk2.component.annotation.DependencyComponent;
import com.ares.nk2.tool.NKStringFormater;
import com.game.protoGen.ProtoErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;

/**
 * @author jeffreyzhou
 * @date 2021/6/18
 */
public class EntityMetaData {
    private static final Logger LOGGER = LoggerFactory.getLogger(EntityMetaData.class);

    private final Class<? extends BaseEntity> entityClazz;

    //class到数组下标的映射
    private final IdentityHashMap<Class<? extends BaseComponent>, Integer> comptClazzIndexMap;

    //所有叶子节点，按定义顺序添加
    private final ArrayList<Class<? extends BaseComponent>> comptClazzList;

    public EntityMetaData(Class<? extends BaseEntity> entityClazz,
                          IdentityHashMap<Class<? extends BaseComponent>, Integer> comptClazzIndexMap,
                          ArrayList<Class<? extends BaseComponent>> comptClazzList) {
        this.entityClazz = entityClazz;
        this.comptClazzIndexMap = comptClazzIndexMap;
        this.comptClazzList = comptClazzList;
    }

    public void createCompts(BaseEntity entityInst,
                             List<ComponentHolder> comptHolderList) {
        for (Class<? extends BaseComponent> comptClazz : comptClazzList) {
            try {
                BaseComponent comptInst = comptClazz.newInstance();
                comptInst.setOwner(entityInst);
                comptHolderList.add(new ComponentHolder(comptInst));
            } catch (Exception e) {
                LOGGER.error("entity-{} create component-{} failed.", entityInst, comptClazz.getSimpleName(), e);
                throw new FyLogicException(ProtoErrorCode.ErrCode.COMPONENT_CONSTRUCT_ERROR_VALUE, NKStringFormater.format("entity-{} create component-{} failed. ", entityInst, comptClazz.getSimpleName()), e);
            } catch (Throwable t) {
                LOGGER.error("entity-{} create component-{} failed.", entityInst, comptClazz.getSimpleName());
                throw t;
            }
        }
    }

    public int getComptIndex(Class<? extends BaseComponent> compt) {
        return comptClazzIndexMap.getOrDefault(compt, -1);
    }

    public <T extends BaseComponent> boolean hasCompt(Class<T> clazz) {
        return comptClazzIndexMap.containsKey(clazz);
    }

    private int checkOneComptDependency(Class<? extends BaseComponent> compt) {
        int ret = 0;
        DependencyComponent deppends = compt.getAnnotation(DependencyComponent.class);
        if (deppends == null) {
            return ret;
        }

        boolean isEntityCompoent = false;
        if (BaseEntityComponent.class.isAssignableFrom(compt)) {
            isEntityCompoent = true;
        }

        boolean containComponentDependency = false;
        boolean containEntityDependency = false;
        for (Class<? extends DependableInterface> dependable : deppends.value()) {
            if (BaseComponent.class.isAssignableFrom(dependable)) {
                containComponentDependency = true;
                Class<? extends BaseComponent> depCompt = (Class<? extends BaseComponent>) dependable;
                if ((!isEntityCompoent) && BaseEntityComponent.class.isAssignableFrom(depCompt)) {
                    LOGGER.error("entity-{} commonComponent-{} depend on entityComponent-{}", entityClazz.getSimpleName(), compt.getSimpleName(), depCompt.getSimpleName());
                    --ret;
                }
                if (comptClazzIndexMap.containsKey(depCompt)) {
                    continue;
                }
                --ret;
                LOGGER.error("entity-{} component-{} depend on component-{},but not add", entityClazz.getSimpleName(), compt.getSimpleName(), depCompt.getSimpleName());
            } else {
                containEntityDependency = true;
                Class<? extends BaseEntity> depEntity = (Class<? extends BaseEntity>) dependable;
                if (!isEntityCompoent) {
                    --ret;
                    LOGGER.error("commonComponent-{} can not depend on entity-{}", compt.getSimpleName(), depEntity.getSimpleName());
                    continue;
                }
                if (!depEntity.isAssignableFrom(entityClazz)) {
                    --ret;
                    LOGGER.error("component-{} depend on entity-{}, but add to entity-{}", compt.getSimpleName(), depEntity.getSimpleName(), entityClazz.getSimpleName());
                }
            }

        }

        return ret;
    }

    private int checkParentDependency() {
        int ret = 0;
        if (entityClazz.getSuperclass() == BaseEntity.class) {
            return ret;
        }
        EntityMetaData parentMetaData = EntityMetaDataMgr.getInstance().getEntityMetaData((Class<? extends BaseEntity>) entityClazz.getSuperclass());
        if (parentMetaData == null) {
            return ret;
        }
        for (Class<? extends BaseComponent> depCompt : parentMetaData.comptClazzList) {
            if (comptClazzIndexMap.containsKey(depCompt)) {
                continue;
            }
            --ret;
            LOGGER.error("entity-{} parent-{} depend on component-{},but not add", entityClazz.getSimpleName()
                    , parentMetaData.entityClazz.getSimpleName(), depCompt.getSimpleName());
        }
        return ret;
    }

    /**
     * 基类有的Component子类必须有
     * Component之间的依赖必须满足
     *
     * @return : 0：检查通过，其他：检查失败
     */

    public int checkDependency() {
        int ret = 0;

        for (Class<? extends BaseComponent> compt : comptClazzIndexMap.keySet()) {
            ret -= checkOneComptDependency(compt);
        }
        ret -= checkParentDependency();

        LOGGER.warn("entity-{} dependency check fini. ret = {}", entityClazz.getSimpleName(), ret);
        return ret;
    }

    private Class<?> getParentComponentBindEntity(Class<? extends BaseComponent> component) {
        Type type = component.getGenericSuperclass();
        if (!(type instanceof ParameterizedType)) {
            return null;
        }
        Type[] params = ((ParameterizedType) type).getActualTypeArguments();
        if (params.length < 1) {
            return null;
        }

        return com.google.common.reflect.TypeToken.of(params[0]).getRawType();
    }
}
