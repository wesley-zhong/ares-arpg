package com.ares.game.controller;

import ch.qos.logback.core.LogbackException;
import com.ares.core.annotation.MsgId;
import com.ares.core.excetion.LogicException;
import com.ares.core.service.AresController;
import com.ares.dal.redis.RedisDAO;
import com.game.protoGen.ProtoCommon;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GameControllerTest implements AresController {
    @Autowired
    private RedisDAO redisDAO;

    @MsgId(ProtoCommon.MsgId.RPC_REQ_TEST_VALUE)
    public ProtoCommon.RpcReqRespons callRpcTest(long uid, ProtoCommon.RpcReqTest reqTest) {
        log.info("------------------ receive rpc = {}", reqTest);
        String key = "redis_key_test";
        String body = "redis_test_body";
        boolean set = redisDAO.set(key, body);
        String s = redisDAO.get(key);
        assert s.equals(body);

        //  throw new LogicException(66666,"some error");

        return ProtoCommon.RpcReqRespons.newBuilder().setSomeId2(reqTest.getSomeId() + 2)
                .setSomeStr2(reqTest.getSomeStr() + "_haha").build();
    }
}
