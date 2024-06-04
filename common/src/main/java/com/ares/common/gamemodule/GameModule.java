package com.ares.common.gamemodule;

import com.game.protoGen.ProtoInner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

public class GameModule {
    public static abstract class ModuleContainer<T extends Module> {
        private static final Logger LOGGER = LoggerFactory.getLogger(ModuleContainer.class);
        private Map<ProtoInner.GameModuleId, T> modules = new LinkedHashMap<>();

        public void addModule(T module) {
            if (modules.put(module.getModuleId(), module) != null) {
                throw new RuntimeException("Duplicate module id: " + module.getModuleId());
            }
        }

        public T getModule(ProtoInner.GameModuleId moduleId) {
            return modules.get(moduleId);
        }

        protected int forEachModule(Function<T, Boolean> function, String flag) {
            for (T module : modules.values()) {
                if (!function.apply(module)) {
                    LOGGER.error("call module {} method error", module);
                    return -1;
                }
            }
            return 0;
        }
    }

    public static abstract class Module  {
        /**
         * 返回模块Id
         *
         * @return 模块Id枚举
         */
        public abstract ProtoInner.GameModuleId getModuleId();
    }
}