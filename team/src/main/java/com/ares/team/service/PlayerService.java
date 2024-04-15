package com.ares.team.service;

import com.ares.team.bean.BeanTest;
import com.ares.team.network.PeerConn;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PlayerService {
    @Autowired
    private TeamPlayerMgr teamPlayerMgr;
    @Autowired
    private PeerConn peerConn;


    public void timerTaskTest(BeanTest beanTest) {
        log.info("------ come from timer task {}", beanTest);
    }
}
