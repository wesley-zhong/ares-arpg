package com.ares.dal.redis;

import com.ares.core.json.transcoder.JsonObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


public abstract class RedisTaskDispatch<T> extends RedisTaskDisPatchBase<T> implements RedisTaskCallBack<T> {
    private static Logger logger = LoggerFactory.getLogger(RedisTaskDispatch.class);

    protected void onRedisTask(List<String> taskList) {
        for (String key : taskList) {
            T task = null;
            try {
                task = JsonObjectMapper.getInstance().readValue(key, this.getObjClass());
                onTask(task);
                if (this.isAutoDelTask()) {
                    deleteTask(task);
                }
            } catch (Exception e) {
                if (task != null) {
                    deleteTask(task);
                }
                logger.error("task = {} faild ", key, e);
            }
        }
    }
}
