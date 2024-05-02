package com.ares.game.player;

import com.ares.common.gamemodule.GameModule;
import com.game.protoGen.BinServer;
import com.game.protoGen.ProtoCommon;

public abstract class PlayerModule extends GameModule.Module {
    private final ProtoCommon.GameModuleId moduleId;
    protected final GamePlayer player;

    public PlayerModule(ProtoCommon.GameModuleId moduleId, GamePlayer player) {
        this.moduleId = moduleId;
        this.player = player;
        this.player.addModule(this);
    }

    @Override
    public final ProtoCommon.GameModuleId getModuleId() {
        return moduleId;
    }

    public GamePlayer getPlayer() {
        return player;
    }

    // 数据反序列化
    public void fromBin(BinServer.PlayerDataBin bin) {}

    // 数据序列化
    public void toBin(BinServer.PlayerDataBin.Builder bin) {}

    // 初始化，完成各种数据检查、补全，使得comp进入可工作的状态
    // 不做业务逻辑操作（比如发放登录奖励）
    public void init() {}

    // 初始化完成后的后续，初始化在单独的线程完成，start主要用来启动定时器等
    public void start() {}

    // 玩家首次登录时要执行的逻辑
    public void onFirstLogin() {}

    // 正常每次登录要实现的逻辑
    public void onLogin(boolean isNewPlayer) {}

    // 登录时推送数据
    public void notifyAllData() {}

    // 连接断开
    public void onDisconnect() {}

    // 断线重连
    public void onReconnect() {}

    // 退出时要执行的逻辑
    public void onLogout() {}

    // 每天固定刷新时间进行的刷新操作
    public void onDailyRefresh() { }

    // 登录时如果错过了当天的 onDailyRefresh 会进行的刷新操作, onLogin之后调用
    public void onLoginDailyRefresh() { }

    // 玩家离开场景
    public void onLeaveScene() { }
}
