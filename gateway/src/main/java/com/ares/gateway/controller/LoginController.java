package com.ares.gateway.controller;

import com.ares.core.annotation.MsgId;
import com.ares.core.service.AresController;
import com.ares.core.tcp.AresTKcpContext;
import com.ares.core.utils.AresContextThreadLocal;
import com.ares.discovery.DiscoveryService;
import com.ares.discovery.utils.NetUtils;
import com.ares.gateway.service.SessionService;
import com.game.protoGen.ProtoCommon;
import com.game.protoGen.ProtoGame;
import com.game.protoGen.ProtoInner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LoginController implements AresController {
    @Autowired
    private SessionService sessionService;
    @Autowired
    private DiscoveryService discoveryService;

    /*
     暂时方案，应该走login server
     */
    @MsgId(ProtoCommon.ProtoCode.ACCOUNT_LOGIN_REQ_VALUE)
    public ProtoGame.AccountLoginRes accountLoginRequest(long uid, ProtoGame.AccountLoginReq loginRequest) {
        AresTKcpContext aresTKcpContext = AresContextThreadLocal.get();
        log.info("-------receive from ={} msg ={}", aresTKcpContext, loginRequest);
        // sessionService.loginRequest(aresTKcpContext, loginRequest);
        return ProtoGame.AccountLoginRes.newBuilder()
                .setUid(1000)
                .setGameSrvIp(NetUtils.getIpAddress().get(0))
                .setGameSrvPort(8081).build();
    }

    @MsgId(ProtoCommon.ProtoCode.GAME_LOGIN_REQ_VALUE)
    public void gameLogin(long uid, ProtoGame.GameLoginReq gameLoginReq) {
        AresTKcpContext aresTKcpContext = AresContextThreadLocal.get();
        sessionService.gameLogin(aresTKcpContext, gameLoginReq);
    }


    @MsgId(ProtoInner.InnerProtoCode.INNER_TO_GAME_LOGIN_RES_VALUE)
    public void onGameLoginRes(long uid, ProtoInner.InnerGameLoginResponse loginResponse) {
        log.info(" INNER_TO_GAME_LOGIN_RES_VALUE :{} ", loginResponse);
        sessionService.gameLoginSuccess(loginResponse);
        ProtoGame.GameLoginRes response = ProtoGame.GameLoginRes.newBuilder()
                .setErrCode(0)
                .setUid(loginResponse.getUid())
                .setServerTime(System.currentTimeMillis()).build();
        sessionService.sendPlayerMsg(loginResponse.getUid(), ProtoCommon.ProtoCode.GAME_LOGIN_RES_VALUE, response);
    }
}
