package com.ares.game.scene;

import com.ares.common.gamemodule.GameModule;
import com.game.protoGen.ProtoCommon;

public class SceneModule extends GameModule.Module{
    private final ProtoCommon.GameModuleId moduleId;
    protected final Scene scene;

    public SceneModule(ProtoCommon.GameModuleId moduleId, Scene scene) {
        this.moduleId = moduleId;
        this.scene = scene;
        this.scene.addModule(this);
    }

    @Override
    public final ProtoCommon.GameModuleId getModuleId() {
        return moduleId;
    }

    public final Scene getScene() {
        return scene;
    }
}
