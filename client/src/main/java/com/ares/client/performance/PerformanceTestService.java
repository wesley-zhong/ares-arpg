package com.ares.client.performance;

import com.ares.client.bean.ClientPlayer;
import com.ares.client.bean.PlayerMgr;
import com.game.protoGen.ProtoCommon;
import com.game.protoGen.ProtoGame;
import com.game.protoGen.ProtoInner;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Random;

@Component
public class PerformanceTestService {

    public void sendPerformanceMsg() {
        Collection<ClientPlayer> allClientPlayer = PlayerMgr.Instance.getAllClientPlayer();

//        for (int i = 0; i < 8; ++i) {
//            new Thread(new ClientMsgPerThread(allClietnPlayer)).start();
//        }

//        for (ClientPlayer clientPlayer : allClientPlayer) {
//            Thread.ofVirtual().start(new ClientMsgPerThread(clientPlayer));
//        }


    }

    public void sendAllPlayerPerformanceMsg() {
        Collection<ClientPlayer> clientPlayerList = PlayerMgr.Instance.getAllClientPlayer();
        for (ClientPlayer clientPlayer : clientPlayerList) {
            ProtoGame.PerformanceTestReq testReq = ProtoGame.PerformanceTestReq.newBuilder()
                    .setSendTime(System.currentTimeMillis())
                    .setSomeBody("ooooooooooooooooooooooooooooooooooooooooooooooo")
                    .setSomeId(111111L).build();
            clientPlayer.send(ProtoCommon.MsgId.PERFORMANCE_TEST_REQ_VALUE, testReq);
        }
    }

    public void sendAllPlayerToTeamPerformanceMsg() {
        Collection<ClientPlayer> clientPlayerList = PlayerMgr.Instance.getAllClientPlayer();
        for (ClientPlayer clientPlayer : clientPlayerList) {
            ProtoGame.DirectToWorldReq testReq = ProtoGame.DirectToWorldReq.newBuilder()
                    .setSomeIdAdd(System.currentTimeMillis())
                    .setResBody("oooooooooooooooooooooooooooooooooooo0000000000000000ooooooooooo")
                    .setSomeId(111111L).build();
            clientPlayer.send(ProtoCommon.MsgId.DIRECT_TO_TEAM_REQ_VALUE, testReq);
        }
    }

    public void sendGatewayPerformanceMsg() {
        Collection<ClientPlayer> clientPlayerList = PlayerMgr.Instance.getAllClientPlayer();
        for (ClientPlayer clientPlayer : clientPlayerList) {
            ProtoGame.PerformanceTestReq testReq = ProtoGame.PerformanceTestReq.newBuilder()
                    .setSendTime(System.currentTimeMillis())
                    .setSomeBody("ooooooooooooooooooooooooooooooooooooooooooooooo")
                    .setSomeId(111111L).build();
            clientPlayer.send(ProtoInner.InnerMsgId.INNER_GATEWAY_PERFORMANCE_REQ_VALUE, testReq);
        }
    }

    public void sendAoiPerformanceMsg(){
        Collection<ClientPlayer> clientPlayerList = PlayerMgr.Instance.getAllClientPlayer();
        Random random = new Random();
        for (ClientPlayer clientPlayer : clientPlayerList) {
            int x = random.nextInt(300000);
            int y = random.nextInt(300000);
            int r = 1000;
            ProtoCommon.AoiTestReq aoiTestReq = ProtoCommon.AoiTestReq.newBuilder()
                    .setHeight(r)
                    .setWidth(r)
                    .setPosX(x)
                    .setPosY(y).build();
            clientPlayer.send(ProtoCommon.MsgId.MAP_AOI_TEST_REQ_VALUE, aoiTestReq);
        }
    }
}
