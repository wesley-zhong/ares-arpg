package com.ares.game.player;

import com.ares.game.DO.RoleDO;
import com.ares.game.player.modules.basic.PlayerBasicModule;
import com.ares.game.player.modules.item.PlayerItemModule;
import com.ares.game.player.modules.scene.PlayerSceneModule;
import com.game.protoGen.ProtoCommon;
import com.google.protobuf.Message;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class GamePlayer extends PlayerModuleContainer {
    private long uid;
    private long sceneId;

    private RoleDO roleDO;

    private final PlayerBasicModule basicModule = new PlayerBasicModule(this);
    private final PlayerSceneModule sceneModule = new PlayerSceneModule(this);
    private final PlayerItemModule itemModule = new PlayerItemModule(this);

    public GamePlayer(long id) {
        this.uid = id;
    }

    public long getUid() {
        return roleDO.getUid();
    }

    public void fromBin(RoleDO bin) {
        modulesFromBin(bin);
    }

    public void toBin(RoleDO bin) {
        modulesToBin(bin);
    }

    public void init() {
        modulesInit();
    }

    public void start() {
        modulesStart();
    }

    public void onFirstLogin() {
        modulesOnFirstLogin();
    }

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
