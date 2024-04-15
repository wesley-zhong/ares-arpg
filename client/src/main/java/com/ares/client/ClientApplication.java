package com.ares.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.CountDownLatch;

@SpringBootApplication
public class ClientApplication {
    public static void main(String[] args) throws InterruptedException {
      // CountDownLatch  countDownLatch = new CountDownLatch(1);
       SpringApplication.run(ClientApplication.class, args);



       //countDownLatch.await();
    }
}
