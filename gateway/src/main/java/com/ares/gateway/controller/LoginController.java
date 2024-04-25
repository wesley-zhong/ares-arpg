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

import java.util.concurrent.atomic.AtomicInteger;

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
    @MsgId(ProtoCommon.MsgId.ACCOUNT_LOGIN_REQ_VALUE)
    public ProtoGame.AccountLoginRes accountLoginRequest(long uid, ProtoGame.AccountLoginReq loginRequest) {
        AresTKcpContext aresTKcpContext = AresContextThreadLocal.get();
        log.info("-------receive from ={} msg ={}", aresTKcpContext, loginRequest);
        // sessionService.loginRequest(aresTKcpContext, loginRequest);
        return ProtoGame.AccountLoginRes.newBuilder()
                .setUid(1000)
                .setGameSrvIp(NetUtils.getIpAddress().get(0))
                .setGameSrvPort(8081).build();
    }

    @MsgId(ProtoCommon.MsgId.GAME_LOGIN_REQ_VALUE)
    public void gameLogin(long uid, ProtoGame.GameLoginReq gameLoginReq) {
        long userId = gameLoginReq.getUid();

        AresTKcpContext aresTKcpContext = AresContextThreadLocal.get();
        sessionService.gameLogin(aresTKcpContext, gameLoginReq);
    }


    @MsgId(ProtoInner.InnerMsgId.INNER_TO_GAME_LOGIN_RES_VALUE)
    public void onGameLoginRes(long uid, ProtoInner.InnerGameLoginResponse loginResponse) {
        log.info(" INNER_TO_GAME_LOGIN_RES_VALUE :{} ", loginResponse);
        sessionService.gameLoginSuccess(loginResponse);
        ProtoGame.GameLoginRes response = ProtoGame.GameLoginRes.newBuilder()
                .setErrCode(0)
                .setUid(loginResponse.getUid())
                .setServerTime(System.currentTimeMillis()).build();
        sessionService.sendPlayerMsg(loginResponse.getUid(), ProtoCommon.MsgId.GAME_LOGIN_RES_VALUE, response);
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
