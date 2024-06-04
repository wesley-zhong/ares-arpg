package com.ares.gateway.localservice;

import com.ares.core.bean.AresMsgIdMethod;
import com.ares.core.tcp.AresTKcpContext;
import com.ares.core.thread.task.PacketEventTask;
import com.ares.nk2.coroutine.LocalService;
import com.game.protoGen.ProtoCommon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlayerLocalService extends LocalService {
    private static final Logger log = LoggerFactory.getLogger(PlayerLocalService.class);

    public static final int EXECUTOR_SERVICE_COUNT = 4;

    private PlayerLocalService() {
        super("Player");

        setNewContainerCount(EXECUTOR_SERVICE_COUNT);
        generateExecutorGroupWithNewContainer("playerService", 10000, 2000);
    }

    private static class InstanceHolder {
        private static final PlayerLocalService instance = new PlayerLocalService();
    }

    public static PlayerLocalService getInstance() {
        return InstanceHolder.instance;
    }

    public void processNetworkMessage(long hashKey, AresTKcpContext aresTKcpContext, AresMsgIdMethod method, long p1, Object p2, ProtoCommon.MsgHeader msgHeader){
        getExecutor(hashKey).runJob(()->{
            PacketEventTask packetEventTask = new PacketEventTask();
            packetEventTask.setAresTKcpContext(aresTKcpContext);
            packetEventTask.setMethod(method);
            packetEventTask.setParam1(p1);
            packetEventTask.setParam2(p2);
            packetEventTask.setMsgHeader(msgHeader);
            packetEventTask.execute();
            return null;
        }, "logic");
    }
}
