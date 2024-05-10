package com.ares.dal.redis;

import com.ares.core.json.transcoder.JsonObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public abstract class RedisTaskWithScoreDispatch<T> extends RedisTaskDisPatchBase<T> implements RedisTaskWithScoreCallBack<T> {
    private static Logger logger = LoggerFactory.getLogger(RedisTaskDispatch.class);

    private static final String LUA_RANK_WITH_SCORE_SCRIPT = "local caisEvents = redis.pcall('ZRANGEBYSCORE', KEYS[1], ARGV[1], ARGV[2], 'limit', ARGV[3], ARGV[4], 'WITHSCORES'); " +
            " for k,v  in pairs( caisEvents) do " +
            " if(k %2 >0) then " +
            "local targetTime = redis.call('ZSCORE',KEYS[1],v)" +
            "targetTime = targetTime + ARGV[5]" +
            "  redis.call('ZADD',KEYS[1],targetTime , v)" +
            "  end;" +
            " end ;" +
            "  return caisEvents ";
    public RedisTaskWithScoreDispatch(){
        taskLuaScript = LUA_RANK_WITH_SCORE_SCRIPT;
    }


    protected void onRedisTask(List<String> taskList) {
        int taskLen = taskList.size() / 2;
        for (int i = 0; i < taskLen; ++i) {
            int index = i * 2;
            String strTask = taskList.get(index);
            String strScore = taskList.get(index + 1);
            Long score = Long.valueOf(strScore);
            T task = null;
            try {
                task = JsonObjectMapper.getInstance().readValue(strTask, this.getObjClass());
                onTask(task, score);
                if (this.isAutoDelTask()) {
                    deleteTask(task);
                }
            } catch (Exception e) {
                if (task != null) {
                    deleteTask(task);
                }
                logger.error("task = {} faild ", strTask);
            }
        }
    }
}
