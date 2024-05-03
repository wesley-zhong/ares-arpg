package com.ares.nk2.component;

import com.ares.nk2.component.annotation.AddComponent;
import com.ares.nk2.component.annotation.ComponentRoot;
import com.ares.nk2.tool.ReflectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Modifier;
import java.util.*;

/**
 * @author jeffreyzhou
 * @date 2021/6/18
 */
public class EntityMetaDataMgr {
    private static final Logger LOGGER = LoggerFactory.getLogger(EntityMetaDataMgr.class);

    private final Map<Class<? extends BaseEntity>, EntityMetaData> entityMetaDataMap = new HashMap<>();

    private EntityMetaDataMgr() {
    }

    public static EntityMetaDataMgr getInstance() {
        return INSTANCE_HOLDER.INSTANCE.get();
    }

    private static class INSTANCE_HOLDER {
        private static final ThreadLocal<EntityMetaDataMgr> INSTANCE = ThreadLocal.withInitial(() -> new EntityMetaDataMgr());
    }

    public EntityMetaData getEntityMetaData(Class<? extends BaseEntity> entityClazz) {
        if (!entityMetaDataMap.containsKey(entityClazz)) {
            buildEntityMetaData(entityClazz);
        }
        return entityMetaDataMap.get(entityClazz);
    }

    public int entityCheck(String prefix) {
        int ret = 0;
        try {
            Collection<Class<? extends BaseEntity>> subtypeCollection = ReflectionUtil.getPrefixSubtypeClazzSet(prefix, BaseEntity.class);
            for (Class<? extends BaseEntity> clazz : subtypeCollection) {
                try {
                    EntityMetaData metaData = getEntityMetaData(clazz);
                    if (metaData == null) {
                        --ret;
                        LOGGER.error("entity-{} can not find meta data", clazz.getSimpleName());
                        continue;
                    }
                    ret -= metaData.checkDependency();
                } catch (Exception e) {
                    --ret;
                    LOGGER.error("entity-{} dependency check failed.", clazz.getSimpleName(), e);
                }
            }
        } catch (Exception e) {
            --ret;
            LOGGER.error("entityCheck error", e);
        }
        return ret;
    }

    private int addInheritTree(Class<? extends BaseEntity> entityClazz,
                               ArrayList<Class<? extends BaseComponent>> componentList,
                               IdentityHashMap<Class<? extends BaseComponent>, Integer> comptClazzIndexMap,
                               Class<? extends BaseComponent> leafComponent, int comptIndex) {
        Class<? extends BaseComponent> baseComponent = leafComponent;
        while (baseComponent != BaseComponent.class) {
            if (comptClazzIndexMap.containsKey(baseComponent)) {
                int oldIndex = comptClazzIndexMap.get(baseComponent);
                LOGGER.error("entity-{} component-{} and component-{} has same base component-{}",
                        entityClazz.getSimpleName(), leafComponent.getSimpleName(), componentList.get(oldIndex).getSimpleName(), baseComponent.getSimpleName());
                return -1;
            }
            ComponentRoot componentRoot = baseComponent.getAnnotation(ComponentRoot.class);
            if (componentRoot != null) {
                if (!componentRoot.entity().isAssignableFrom(entityClazz)) {
                    LOGGER.error("entity-{} not support component-{}:{} ",
                            entityClazz.getSimpleName(), leafComponent.getSimpleName(), componentRoot.entity());
                    return -1;
                }
                break;
            }
            comptClazzIndexMap.put(baseComponent, comptIndex);
            baseComponent = (Class<? extends BaseComponent>) baseComponent.getSuperclass();
        }
        return 0;
    }

    private int addEntityComponent(Class<? extends BaseEntity> entityClazz
            , IdentityHashMap<Class<? extends BaseComponent>, Integer> comptClazzIndexMap
            , ArrayList<Class<? extends BaseComponent>> componentList) {
        int ret = 0;
        AddComponent addComponentAnnotation = entityClazz.getAnnotation(AddComponent.class);
        if (addComponentAnnotation != null) {
            for (Class<? extends BaseComponent> componentClazz : addComponentAnnotation.value()) {
                if (comptClazzIndexMap.containsKey(componentClazz)) {
                    continue;
                }
                if (Modifier.isAbstract(componentClazz.getModifiers()) && !Modifier.isAbstract(entityClazz.getModifiers())) {
                    LOGGER.error("component-{} is abstract, entity-{} should be abstract", componentClazz.getSimpleName(), entityClazz.getSimpleName());
                    --ret;
                    continue;
                }
                int comptIndex = componentList.size();
                componentList.add(componentClazz);
                ret -= addInheritTree(entityClazz, componentList, comptClazzIndexMap, componentClazz, comptIndex);
            }
        }
        return ret;
    }

    private void buildEntityMetaData(Class<? extends BaseEntity> entityClazz) {
        if (entityMetaDataMap.containsKey(entityClazz)) {
            return;
        }
        IdentityHashMap<Class<? extends BaseComponent>, Integer> comptClazzIndexMap = new IdentityHashMap<>();
        //所有叶子节点，按定义顺序添加
        ArrayList<Class<? extends BaseComponent>> componentList = new ArrayList<>();

        int ret = 0;
        ret += addEntityComponent((Class<? extends BaseEntity>) entityClazz, comptClazzIndexMap, componentList);

        if (ret != 0) {
            return;
        }
        EntityMetaData entityMetaData = new EntityMetaData(entityClazz, comptClazzIndexMap, componentList);
        entityMetaDataMap.put(entityClazz, entityMetaData);
        LOGGER.warn("entity-{} build meta data success. ", entityClazz.getSimpleName());
    }

}
