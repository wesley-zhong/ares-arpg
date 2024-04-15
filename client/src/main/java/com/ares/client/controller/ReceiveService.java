package com.ares.client.controller;

import com.ares.client.Client;
import com.ares.client.performance.PerformanceTestService;
import com.ares.core.annotation.MsgId;
import com.ares.core.bean.AresPacket;
import com.ares.core.service.AresController;
import com.game.protoGen.ProtoCommon;
import com.game.protoGen.ProtoGame;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
public class ReceiveService implements AresController {
    @Autowired
    private Client client;
    @Autowired
    private PerformanceTestService performanceTestService;

    @MsgId(ProtoCommon.ProtoCode.GAME_LOGIN_RES_VALUE)
    public void userLoginResponse(ProtoGame.AccountLoginRes response) {
        log.info("------login response ={}", response);

        ProtoGame.PerformanceTestReq helloPerformance = ProtoGame.PerformanceTestReq.newBuilder()
                .setSomeBody("hello performance")
                .setSendTime(System.currentTimeMillis())
                .setSomeId(11111).build();
        AresPacket aresPacket = AresPacket.create(ProtoCommon.ProtoCode.PERFORMANCE_TEST_REQ_VALUE, helloPerformance);
        client.getChannel().writeAndFlush(aresPacket);

        //ProtoGame.D
        ProtoGame.DirectToWorldReq req = ProtoGame.DirectToWorldReq.newBuilder().setResBody("OOOOOOOOOOOOOOOOO").setSomeId(13223333).build();
        AresPacket directWorld = AresPacket.create(ProtoCommon.ProtoCode.DIRECT_TO_TEAM_REQ_VALUE, req);
        client.getChannel().writeAndFlush(directWorld);

        ProtoCommon.MsgHeader rpcHeader = ProtoCommon.MsgHeader.newBuilder().setMsgId(ProtoCommon.ProtoCode.RPC_REQ_TEST_VALUE)
                .setReqId(991).build();
        ProtoCommon.RpcReqTest rpcTestBody = ProtoCommon.RpcReqTest.newBuilder().setSomeId(881)
                .setSomeStr("rpcTestStr").build();
        AresPacket rpcPacket = AresPacket.create(rpcHeader, rpcTestBody);
        client.getChannel().writeAndFlush(rpcPacket);

        //   performanceTestService.startSend();
    }

    @MsgId(ProtoCommon.ProtoCode.PERFORMANCE_TEST_RES_VALUE)
    public void onGameResponse(ProtoGame.PerformanceTestRes res) {
        long now = System.currentTimeMillis();
        log.info("==============  PERFORMANCE_TEST_RES_VALUE  response ={}  dis ={} ", res, now - res.getSendTime());
    }

    @MsgId(ProtoCommon.ProtoCode.DIRECT_TO_TEAM_RES_VALUE)
    public void onWorldResponse(ProtoGame.DirectToWorldRes res) {
        log.info("==============  DIRECT_TO_TEAM_RES_VALUE response ={} ", res);
    }
}
