package com.ares.gateway.controller;

import com.ares.core.annotation.MsgId;
import com.ares.core.service.AresController;
import com.ares.core.tcp.AresTKcpContext;
import com.ares.core.utils.AresContextThreadLocal;
import com.ares.gateway.service.SessionService;
import com.game.protoGen.ProtoGame;
import com.game.protoGen.ProtoInner;
import com.game.protoGen.ProtoMsgId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
public class LoginController implements AresController {
    @Autowired
    private SessionService sessionService;

    @MsgId(ProtoMsgId.MsgId.GAME_LOGIN_PUSH_VALUE)
    public void gameLogin(long uid, ProtoGame.GameLoginPush gameLoginReq) {
        // first check player token
        //    boolean ret = userTokenService.checkToken(gameLoginReq.getUid(), gameLoginReq.getGameToken());

        AresTKcpContext aresTKcpContext = AresContextThreadLocal.get();
        sessionService.gameLogin(aresTKcpContext, gameLoginReq);
    }


    @MsgId(ProtoInner.InnerMsgId.INNER_TO_GAME_LOGIN_RES_VALUE)
    public void onGameLoginRes(long uid, ProtoInner.InnerGameLoginResponse loginResponse) {
        sessionService.gameSrvLoginResponse(loginResponse);
    }

    @MsgId(ProtoInner.InnerMsgId.INNER_PLAYER_KICK_OUT_REQ_VALUE)
    public void onGameKickOutPlayer(long uid, ProtoInner.InnerGameKickOutReq innerGameKickOutReq) {
        sessionService.kickOutPlayer(uid, innerGameKickOutReq.getSid());
    }


    private volatile long start = System.currentTimeMillis();
    private AtomicInteger reqCount = new AtomicInteger(0);

    @MsgId(ProtoInner.InnerMsgId.INNER_GATEWAY_PERFORMANCE_REQ_VALUE)
    public void gatewayEchoTest(long uid, ProtoGame.PerformanceTestReq req) {
        ProtoGame.PerformanceTestRes performanceBody = ProtoGame.PerformanceTestRes.newBuilder()
                .setResBody("performanceBody")
                .setSendTime(req.getSendTime())
                .setSomeId(5555555).build();

        int count = reqCount.incrementAndGet();
        long now = System.currentTimeMillis();
        long dis = now - start;
        if (dis >= 10000) {
            start = now;
            reqCount.set(0);
            log.info("================ time ={}  count ={}", dis, count);
        }
        sessionService.sendPlayerMsg(uid, ProtoInner.InnerMsgId.INNER_GATEWAY_PERFORMANCE_RES_VALUE, performanceBody);
    }
}
