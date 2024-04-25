package com.ares;

import com.ares.transport.client.AresTcpClientImpl;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;

@Slf4j
public class LoginTest {
    private AresTcpClientImpl tcpClientImpl;
    @Before
    public void before(){
        log.info("------ before");
       // tcpClient

    }

    @Test
    public void test(){
        assert true;
    }
}
