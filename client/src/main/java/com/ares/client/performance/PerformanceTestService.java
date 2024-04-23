package com.ares.client.performance;

import com.ares.client.bean.ClientPlayer;
import com.ares.client.bean.PlayerMgr;
import com.game.protoGen.ProtoCommon;
import com.game.protoGen.ProtoGame;
import com.game.protoGen.ProtoInner;
import org.springframework.stereotype.Component;

import java.util.Collection;

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


    public static class ClientMsgPerThread implements Runnable {
        private Collection<ClientPlayer> clientPlayerList;

        public ClientMsgPerThread(Collection<ClientPlayer> clientPlayer) {
            this.clientPlayerList = clientPlayer;
        }

        @Override
        public void run() {
            // try {
            //  while (true) {
            for (ClientPlayer clientPlayer : clientPlayerList) {
                ProtoGame.PerformanceTestReq testReq = ProtoGame.PerformanceTestReq.newBuilder()
                        .setSendTime(System.currentTimeMillis())
                        .setSomeBody("ooooooooooooooooooooooooooooooooooooooooooooooo")
                        .setSomeId(111111L).build();
                clientPlayer.send(ProtoCommon.MsgId.PERFORMANCE_TEST_REQ_VALUE, testReq);
                //    Thread.sleep(1);
                //   System.out.println("____________________HHHHHHHHHHHHHHHHHH");
            }
            //  }
//            } catch (Exception e) {
//            }
        }
    }
}
