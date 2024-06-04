package com.ares.game.scene;

import com.game.protoGen.ProtoScene;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VisionContext {
    public static VisionContext MEET = new VisionContext(ProtoScene.VisionType.VISION_MEET);
    public static VisionContext MISS = new VisionContext(ProtoScene.VisionType.VISION_MISS);

    ProtoScene.VisionType type;
    long param = 0;
    long excludeUid = 0;

    public VisionContext(ProtoScene.VisionType type) {
        this.type = type;
    }

    public VisionContext(ProtoScene.VisionType type, long param, long excludeUid) {
        this.type = type;
        this.param = param;
        this.excludeUid = excludeUid;
    }

    public VisionContext(VisionContext context) {
        this.type = context.type;
        this.param = context.param;
        this.excludeUid = context.excludeUid;
    }
}
