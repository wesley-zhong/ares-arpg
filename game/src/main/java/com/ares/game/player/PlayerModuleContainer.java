package com.ares.game.player;

import com.ares.common.gamemodule.GameModule;
import com.game.protoGen.BinServer;

public class PlayerModuleContainer extends GameModule.ModuleContainer<PlayerModule>{

    // 数据反序列化
    public void modulesFromBin(BinServer.PlayerDataBin bin) {
        forEachModule(module -> {
            module.fromBin(bin);
            return true;
        }, "fromBin");
    }

    // 数据序列化
    public void modulesToBin(BinServer.PlayerDataBin.Builder bin) {
        forEachModule(module -> {
            module.toBin(bin);
            return true;
        }, "fromBin");
    }

    // 初始化，完成各种数据检查、补全，使得module进入可工作的状态
    // 不做业务逻辑操作（比如发放登录奖励）
    public void modulesInit() {
        forEachModule(module -> {
            module.init();
            return true;
        }, "fromBin");
    }

    // 初始化完成后的后续，初始化在单独的线程完成，start主要用来启动定时器等
    public void modulesStart() {
        forEachModule(module -> {
            module.start();
            return true;
        }, "fromBin");
    }

    // 玩家首次登录时要执行的逻辑
    public void modulesOnFirstLogin() {
        forEachModule(module -> {
            module.onFirstLogin();
            return true;
        }, "fromBin");
    }

    // 正常每次登录要实现的逻辑
    public void modulesOnLogin(boolean isNewPlayer) {
        forEachModule(module -> {
            module.onLogin(isNewPlayer);
            return true;
        }, "fromBin");
    }

    // 登录时推送数据
    public void modulesNotifyAllData() {
        forEachModule(module -> {
            module.notifyAllData();
            return true;
        }, "fromBin");
    }

    // 连接断开
    public void modulesOnDisconnect() {
        forEachModule(module -> {
            module.onDisconnect();
            return true;
        }, "fromBin");
    }

    // 断线重连
    public void modulesOnReconnect()  {
        forEachModule(module -> {
            module.onReconnect();
            return true;
        }, "fromBin");
    }

    // 退出时要执行的逻辑
    public void modulesOnLogout() {
        forEachModule(module -> {
            module.onLogout();
            return true;
        }, "fromBin");
    }

    // 每天固定刷新时间进行的刷新操作
    public void modulesOnDailyRefresh() {
        forEachModule(module -> {
            module.onDailyRefresh();
            return true;
        }, "fromBin");
    }

    // 登录时如果错过了当天的 onDailyRefresh 会进行的刷新操作, onLogin之后调用
    public void modulesOnLoginDailyRefresh() {
        forEachModule(module -> {
            module.onLoginDailyRefresh();
            return true;
        }, "fromBin");
    }

    // 玩家离开场景
    public void modulesOnLeaveScene() {
        forEachModule(module -> {
            module.onLeaveScene();
            return true;
        }, "onLeaveScene");
    }
}
