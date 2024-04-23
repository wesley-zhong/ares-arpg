package com.ares.client.configuration;

import com.ares.client.network.ClientMsgHandler;
import com.ares.core.tcp.AresTcpHandler;
import com.ares.transport.client.AresTcpClientConn;
import com.ares.transport.encode.AresPacketMsgEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@org.springframework.context.annotation.Configuration
@ComponentScan({"com.ares.core","com.ares.client"})
public class Configuration {

    @Bean
    public AresTcpClientConn aresTcpClient(@Autowired  AresTcpHandler  aresTcpHandler){
        AresTcpClientConn aresTcpClientConn = new AresTcpClientConn(aresTcpHandler,new AresPacketMsgEncoder());
        return aresTcpClientConn;
    }

    @Bean
    public AresTcpHandler aresTcpHandler(){
        return new ClientMsgHandler();
    }
}
