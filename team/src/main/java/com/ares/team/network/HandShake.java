package com.ares.team.network;

import com.ares.discovery.DiscoveryService;
import com.ares.transport.bean.ServerNodeInfo;
import com.ares.transport.peer.InnerHandShake;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class HandShake extends InnerHandShake {
    @Autowired
    private DiscoveryService discoveryService;
    @Override
    protected ServerNodeInfo getMyselfNodeInfo() {
        return discoveryService.getEtcdRegister().getMyselfNodeInfo();
    }
}
