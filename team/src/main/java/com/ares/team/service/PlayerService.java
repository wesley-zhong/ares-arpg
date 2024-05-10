package com.ares.team.service;

import com.ares.team.bean.BeanTest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PlayerService {

    public void timerTaskTest(BeanTest beanTest) {
        log.info("------ come from timer task {}", beanTest);
    }
}
