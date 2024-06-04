package com.ares.game.scene;

import com.ares.common.gamemodule.GameModule;
import com.game.protoGen.ProtoInner;

public class SceneModule extends GameModule.Module {
    private final ProtoInner.GameModuleId moduleId;
    protected final Scene scene;

    public SceneModule(ProtoInner.GameModuleId moduleId, Scene scene) {
        this.moduleId = moduleId;
        this.scene = scene;
        this.scene.addModule(this);
    }

    @Override
    public final ProtoInner.GameModuleId getModuleId() {
        return moduleId;
    }

    public final Scene getScene() {
        return scene;
    }
}
