package com.ares.game.controller;

import com.ares.common.quadtree.Entity;
import com.ares.core.annotation.MsgId;
import com.ares.core.service.AresController;
import com.ares.dal.redis.RedisDAO;
import com.ares.game.bean.MonsterTestBean;
import com.ares.game.service.QuadTreeTestService;
import com.game.protoGen.ProtoCommon;
import com.google.protobuf.ByteString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class GameControllerTest implements AresController {
    @Autowired
    private RedisDAO redisDAO;
    @Autowired
    private QuadTreeTestService quadTreeTestService;

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

    private static AtomicInteger atomicCount = new AtomicInteger(0);
    private volatile long start = System.currentTimeMillis();

    @MsgId(ProtoCommon.MsgId.MAP_AOI_TEST_REQ_VALUE)
    public ProtoCommon.AoiTestRes onAoiReq(long uid, ProtoCommon.AoiTestReq aoiTestReq) {
        ProtoCommon.AoiTestRes.Builder builder = ProtoCommon.AoiTestRes.newBuilder();
        List<Entity<MonsterTestBean>> intersectList = quadTreeTestService.intersect(aoiTestReq.getPosX(), aoiTestReq.getPosY(), aoiTestReq.getHeight());
        for (Entity<MonsterTestBean> entity : intersectList) {
            MonsterTestBean obj = entity.getObj();
            ProtoCommon.Entity pbEntity = ProtoCommon.Entity.newBuilder()
                    .setBody(ByteString.copyFrom(obj.data))
                    .setId(obj.id).build();
            builder.addEntities(pbEntity);
            builder.addDelIds(pbEntity.getId());
        }
        int nowCount = atomicCount.incrementAndGet();
        long now = System.currentTimeMillis();
        long dis = now - start;
        if (dis >= 10000) {
            atomicCount.set(0);
            start = now;
            log.info("================ time ={}  aoi ={}", dis, nowCount);
        }

        return builder.build();

    }
}
