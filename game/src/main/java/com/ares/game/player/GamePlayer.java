package com.ares.game.player;

import com.ares.game.DO.RoleDO;
import com.ares.game.player.modules.basic.PlayerBasicModule;
import com.ares.game.player.modules.item.PlayerItemModule;
import com.ares.game.player.modules.scene.PlayerSceneModule;
import com.game.protoGen.BinServer;
import com.game.protoGen.ProtoCommon;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;


@Getter
@Setter
@Slf4j
public class GamePlayer extends PlayerModuleContainer {
    private long uid;
    private long sceneId;

    private final PlayerBasicModule basicModule = new PlayerBasicModule(this);
    private final PlayerSceneModule sceneModule = new PlayerSceneModule(this);
    private final PlayerItemModule itemModule = new PlayerItemModule(this);

    public GamePlayer(long id) {
        this.uid = id;
    }

    // 在登录线程执行
    public void fromBin(RoleDO roleDO) {
        try {
            modulesFromBin(BinServer.PlayerDataBin.parseFrom(roleDO.getBin()));
        }
        catch (InvalidProtocolBufferException e) {
            throw new RuntimeException("parse bin error. uid:" + uid, e);
        }
    }

    public void toBin(RoleDO roleDO) {
        roleDO.setUid(uid);

        BinServer.PlayerDataBin.Builder builder = BinServer.PlayerDataBin.newBuilder();
        modulesToBin(builder);
        roleDO.setBin(builder.build().toByteArray());
    }

    // 在登录线程执行
    public void init() {
        modulesInit();
    }

    // 在逻辑线程执行
    // 在这里执行启动定时器等操作
    public void start() {
       // modulesStart();
    }

    // 在登录线程执行
    public void onFirstLogin() {
        modulesOnFirstLogin();
    }

    // 在逻辑线程执行
    public void onLogin(boolean isNewPlayer) {
        modulesOnLogin(isNewPlayer);
    }

    public void notifyAllData() {
        modulesNotifyAllData();
    }

    public void onDisconnect() {
        modulesOnDisconnect();
    }

    public void onReconnect() {
        modulesOnReconnect();
    }

    public void onLogout() {
        modulesOnLogout();
    }

    public void onDailyRefresh() {
        modulesOnDailyRefresh();
    }

    public void onLoginDailyRefresh() {
        modulesOnLoginDailyRefresh();
    }

    public void onLeaveScene() {
        modulesOnLeaveScene();
    }

    public void sendMessage(ProtoCommon.MsgId msgId, Message message) {

    }
}
