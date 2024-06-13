package com.ares.game.controller;

import com.ares.common.quadtree.Entity;
import com.ares.core.annotation.MsgId;
import com.ares.core.service.AresController;
import com.ares.dal.redis.RedisDAO;
import com.ares.game.DO.RoleDO;
import com.ares.game.bean.MonsterTestBean;
import com.ares.game.network.PeerConn;
import com.ares.game.player.Player;
import com.ares.game.service.PlayerRoleService;
import com.ares.game.service.QuadTreeTestService;
import com.game.protoGen.ProtoCommon;
import com.game.protoGen.ProtoGame;
import com.game.protoGen.ProtoMsgId;
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
    @Autowired
    private PeerConn peerConn;
    @Autowired
    private PlayerRoleService playerRoleService;

    @MsgId(ProtoMsgId.MsgId.RPC_REQ_TEST_VALUE)
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

    @MsgId(ProtoMsgId.MsgId.MAP_AOI_TEST_REQ_VALUE)
    public ProtoCommon.AoiTestRes onAoiReq(long uid, ProtoCommon.AoiTestReq aoiTestReq) {
        ProtoCommon.AoiTestRes.Builder builder = ProtoCommon.AoiTestRes.newBuilder();
        List<Entity<MonsterTestBean>> intersectList = quadTreeTestService.intersect(aoiTestReq.getPosX(), aoiTestReq.getPosY(), aoiTestReq.getHeight());
        for (Entity<MonsterTestBean> entity : intersectList) {
            MonsterTestBean obj = entity.getObj();
            ProtoCommon.Entity pbEntity = ProtoCommon.Entity.newBuilder()
                    .setBody(String.valueOf(obj.data))
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

    private static AtomicInteger atomicCount1 = new AtomicInteger(0);
    private volatile long start1 = System.currentTimeMillis();

    @MsgId(ProtoMsgId.MsgId.PERFORMANCE_TEST_REQ_VALUE)
    public void performanceTest(long uid, ProtoGame.PerformanceTestReq req) {
        Player player = playerRoleService.getPlayer(uid);
        if (player == null) {
            log.error(" uid ={} not found", uid);
            return;
        }

        RoleDO roleDO = new RoleDO();
        player.toBin(roleDO);

        //for test
        //  long begin = System.currentTimeMillis();
        //playerRoleService.asynUpdateTest(roleDO);
        //   long now = System.currentTimeMillis();
        //   log.info("-----performanceTest  uid ={} body={}  dis ={} ", uid, req, (now - begin));
        ProtoGame.PerformanceTestRes performanceBOyd = ProtoGame.PerformanceTestRes.newBuilder()
                .setResBody("performanceBody000000000000000000000000000000000000000000000fff00f0d0f")
                .setSendTime(req.getSendTime())
                .setSomeId(44444).build();
        peerConn.sendGateWayMsg(uid, ProtoMsgId.MsgId.PERFORMANCE_TEST_RES_VALUE, performanceBOyd);


        //for timer test
        //  log.info("==================================== start timer call begin");
//        TimerBeanTest timerBeanTest = new TimerBeanTest();
//        timerBeanTest.msg = "timerTest";
        // ScheduleService.INSTANCE.executeTimerTaskWithMS(uid, playerRoleService::onTimerTest, timerBeanTest, 100L);
        int nowCount = atomicCount1.incrementAndGet();
        long now = System.currentTimeMillis();
        long dis = now - start1;
        if (dis >= 10000) {
            atomicCount1.set(0);
            start1 = now;
            log.info("================ time ={}  count ={}", dis, nowCount);
        }
    }
}
