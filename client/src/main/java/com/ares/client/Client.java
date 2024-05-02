package com.ares.client;

import com.ares.client.bean.ClientPlayer;
import com.ares.client.bean.PlayerMgr;
import com.ares.transport.client.AresTcpClientConn;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Client implements InitializingBean {
    private static final Logger log = LoggerFactory.getLogger(Client.class);
    @Autowired
    private AresTcpClientConn aresTcpClientConn;
    @Autowired
    private LoginService loginService;
    @Value("${uidStart:100}")
    private int fromUid;
    @Value("${playerCount:1}")
    public int PLAYER_COUNT;
    @Value("${gatewayIp:localhost}")
    private String gatewayIp;
    @Value("${port:6080}")
    private int port;
  //  public final static int PLAYER_COUNT = 8000;
    // private String serverIp = "172.18.2.101";

    @Override
    public void afterPropertiesSet() throws Exception {
      // multiPlayerLogin();
        samePlayerLogin();
    }

    private void samePlayerLogin(){
        for (int i = 0; i < PLAYER_COUNT; ++i) {
            ClientPlayer clientPlayer = new ClientPlayer(100);
            Channel channel = aresTcpClientConn.connect(gatewayIp, port);
            clientPlayer.setContext(channel);
            PlayerMgr.Instance.addClientPlayer(clientPlayer);
            loginService.loginRequest(clientPlayer);
        }
    }

    private void multiPlayerLogin(){
        for (int i = 0; i < PLAYER_COUNT; ++i) {
            ClientPlayer clientPlayer = new ClientPlayer(fromUid + i);
            Channel channel = aresTcpClientConn.connect(gatewayIp, port);
            clientPlayer.setContext(channel);
            PlayerMgr.Instance.addClientPlayer(clientPlayer);
            loginService.loginRequest(clientPlayer);
        }
    }
}
