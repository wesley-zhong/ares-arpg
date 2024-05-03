package com.ares.game.service;

import com.ares.common.bean.ServerType;
import com.ares.core.tcp.AresTKcpContext;
import com.ares.core.thread.AresThreadPool;
import com.ares.core.thread.LogicThreadPoolGroup;
import com.ares.core.utils.AresContextThreadLocal;
import com.ares.game.DO.RoleDO;
import com.ares.game.bean.PlayerCreateRet;
import com.ares.game.configuration.ThreadPoolType;
import com.ares.game.dao.RoleDAO;
import com.ares.game.network.GamePlayerInterTransferInfo;
import com.ares.game.network.PeerConn;
import com.ares.game.player.Player;
import com.game.protoGen.ProtoInner;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PlayerLoginService {
    @Autowired
    private PlayerRoleService playerRoleService;
    @Autowired
    private PeerConn peerConn;

    @Autowired
    private RoleDAO roleDAO;

    /**
     * @param gameInnerLoginRequest
     */
    // run on login thread (io)
    public void gamePlayerInnerLogin(long uid, ProtoInner.InnerGameLoginRequest gameInnerLoginRequest) {
        log.info("======== gameInnerLoginRequest  uid ={}", uid);
        AresTKcpContext aresTKcpContext = AresContextThreadLocal.get();
        GamePlayerInterTransferInfo playerInterTransferInfo = peerConn.getPlayerLastGatewayChannel(uid);
        // 通知上一个gateway 踢人
        if (playerInterTransferInfo != null
                && playerInterTransferInfo.getContextByType(ServerType.GATEWAY.getValue()) != aresTKcpContext.getCtx().channel()) {
            sendGatewayToKickoutPlayer(uid, playerInterTransferInfo);
        }
        // 设置 玩家 gateway context , 线程hash
        playerInterTransferInfo = recordPlayerContextAndThreadHash(uid, gameInnerLoginRequest, aresTKcpContext.getCtx());

        //load player data
        PlayerCreateRet playerCreateRet = loadOrCreateGamePlayerData(uid);
        //cache player info
        playerRoleService.cachePlayer(playerCreateRet.player);

        //second run logic thread
        AresThreadPool logicProcessThreadPool = LogicThreadPoolGroup.INSTANCE.INSTANCE.selectThreadPool(ThreadPoolType.LOGIC.getValue());
        long threadHashCode = playerInterTransferInfo.getThreadHashCode();
        logicProcessThreadPool.execute(threadHashCode, this::playerLoginOnDataLoaded, playerInterTransferInfo.getGateSid(), playerCreateRet);
    }


    // run on login thread (io)
    private PlayerCreateRet loadOrCreateGamePlayerData(long uid) {
        PlayerCreateRet playerCreateRet = new PlayerCreateRet();
        // first get from local memory
        Player player = playerRoleService.getPlayer(uid);
        if (player != null) {
            playerCreateRet.loadFromMem = true;
            playerCreateRet.player = player;
            return playerCreateRet;
        }

        // second from db
        player = loadGamePlayerFromDB(uid);
        if (player == null) {
            // third create
            player = createPlayer(uid);
            playerCreateRet.isNew = true;
        }
        player.init();
        playerCreateRet.player = player;
        return playerCreateRet;
    }


    //run on  logic thread  this process login logic after player data loaded
    private void playerLoginOnDataLoaded(long sid, PlayerCreateRet playerCreateRet) {
        Player player = playerCreateRet.player;
        player.start();
        player.login(playerCreateRet.isNew, playerCreateRet.loadFromMem, 0, false);
        sendPlayerLoginResponse(player.getUid(), sid);
        log.info("  ###########  player uid ={} sid ={} login finished", player.getUid(), sid);
    }


    private Player loadGamePlayerFromDB(long uid) {
        RoleDO roleDO = roleDAO.getById(uid);
        if (roleDO == null) {
            return null;
        }
        Player player = new Player(uid, peerConn);
        //   player.fromBin(roleDO);
        return player;
    }

    private Player createPlayer(long uid) {
        Player player = new Player(uid, peerConn);
        player.onFirstLogin();
        player.getBasicModule().setNickName("name _" + uid);

        RoleDO roleDO = new RoleDO();
        roleDO.setId(uid);
        player.toBin(roleDO);
        roleDAO.insert(roleDO);
        return player;
    }

    private GamePlayerInterTransferInfo recordPlayerContextAndThreadHash(long uid, ProtoInner.InnerGameLoginRequest request, ChannelHandlerContext context) {
        GamePlayerInterTransferInfo gamePlayerInterTransferInfo = peerConn.recordPlayerFromContext(ServerType.GATEWAY, uid, request.getSid(), context);
        long threadHashCode = request.getTargetId();
        if (threadHashCode == 0) {
            threadHashCode = uid;
        }
        gamePlayerInterTransferInfo.setThreadHashCode(threadHashCode);
        return gamePlayerInterTransferInfo;
    }


    private void sendPlayerLoginResponse(long pid, long sid) {
        Player player = playerRoleService.getPlayer(pid);
        if (player == null) {
            log.error(" pid={} not found", pid);
            return;
        }
        ProtoInner.InnerGameLoginResponse innerGameLoginRes =
                ProtoInner.InnerGameLoginResponse.newBuilder()
                        .setUid(pid)
                        .setSid(sid).build();
        peerConn.sendGateWayMsg(pid, ProtoInner.InnerMsgId.INNER_TO_GAME_LOGIN_RES_VALUE, innerGameLoginRes);
    }

    private void sendGatewayToKickoutPlayer(long uid, GamePlayerInterTransferInfo transferInfo) {
        Channel gateWayChannel = transferInfo.getContextByType(ServerType.GATEWAY.getValue());
        if (gateWayChannel == null) {
            return;
        }
        log.info("send to gateway ={} kickout playerId ={}", gateWayChannel, uid);
        ProtoInner.InnerGameKickOutReq kickOutReq = ProtoInner.InnerGameKickOutReq.newBuilder()
                .setSid(transferInfo.getGateSid())
                .build();
        peerConn.send(gateWayChannel, uid, ProtoInner.InnerMsgId.INNER_PLAYER_KICK_OUT_REQ_VALUE, kickOutReq);
    }
}
