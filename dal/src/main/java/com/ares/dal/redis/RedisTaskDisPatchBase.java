package com.ares.dal.redis;


import com.ares.core.json.transcoder.JsonObjectMapper;
import com.ares.core.utils.JsonUtil;
import com.fasterxml.jackson.databind.JavaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

public abstract class RedisTaskDisPatchBase<T> {
    private static Logger logger = LoggerFactory.getLogger(RedisTaskDispatch.class);
    private final String REDIS_LUA =
            "local taskList = redis.pcall('ZRANGEBYSCORE', KEYS[1], ARGV[1], ARGV[2], 'limit', ARGV[3], ARGV[4]); " +
                    " for k,v  in pairs( taskList) do " +
                    "    redis.call('ZADD',KEYS[1], ARGV[5], v)" +
                    "  end;" +
                    "  return taskList ";


    @Autowired
    protected RedisClusterDAO redisDAO;

    @Value("${redis.task.interval.time:20000}")
    private int taskIntervalTime = 20000;

    private boolean distributeSync;

    @Value("${redis.task.thread:2}")
    private int taskThreadCount;
    @Value("${redis.eachmaxtaskcount:2}")
    private int eachMaxTaskCount;

    @Value("${redis.task.max.excute.time:1000}")
    private long taskMaxExcuteTime;

    @Value("${redis.task.wait.queue.name:TASK_WAIT_QUE}")
    private String taskWaitName;
    private boolean autoDelTask = true;
    private JavaType objClass;
    protected String taskLuaScript;

    private String luaScriptSha1;

    private void loadSha(String script) {
        luaScriptSha1 = redisDAO.scriptLoad(script);
    }

    protected void setScript(String script) {
        this.taskLuaScript = script;
    }

    protected JavaType getObjClass() {
        return objClass;
    }


    public void pushTask(T task, long timeOut) {
        if (distributeSync) {
            distributeAddTask(task, timeOut);
            return;
        }
        redisDAO.zadd(taskWaitName, timeOut, task);
    }

    private void distributeAddTask(T task, long timeOut) {
        String distributeKey = JsonObjectMapper.toJSONString(task);
        boolean ret = redisDAO.lock(distributeKey, 15);
        if (ret) {
            try {
                redisDAO.zadd(taskWaitName, timeOut, task);
            } finally {
                redisDAO.unLock(distributeKey);
            }
        }
    }

    public void deleteTask(T task) {
        redisDAO.zrem(taskWaitName, task);
    }

    protected abstract void onRedisTask(List<String> task);


    private void queryProcessRedisTask() {
        long nowTime = System.currentTimeMillis();
        long tagetTime = nowTime + taskMaxExcuteTime;
        String keys[] = new String[1];
        keys[0] = taskWaitName;

        String[] values = new String[5];
        values[0] = "0";
        values[1] = String.valueOf(nowTime);
        values[2] = "0";
        values[3] = Integer.toString(eachMaxTaskCount);
        values[4] = Long.toString(tagetTime);

        List<String> taskList = (List<String>) this.redisDAO.evalsha(luaScriptSha1, keys, values);//(List<String>) this.redisDAO.eval(REDIS_LUA, 1, taskWaitName, 0 + "", Long.toString(nowTime), 0 + "", Integer.toString(eachMaxTaskCount), Long.toString(tagetTime));
        if (CollectionUtils.isEmpty(taskList)) {
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                logger.error("thread sleep exception ", e);
            }
            return;
        }
        onRedisTask(taskList);
    }

    private void asynTask() {
        while (true) {
            try {
                queryProcessRedisTask();
            } catch (Exception e) {
                logger.error("error = ", e);
            }
        }
    }

    private void loadSha() {
        String script = taskLuaScript;
        if (script == null) {
            script = REDIS_LUA;
        }
        loadSha(script);
    }

    protected void start() {
        Class<?> objClass = this.getClass();
        Method[] callMethods = this.getClass().getDeclaredMethods();//("onTask", Object.class)[0];
        Method callMethod = null;
        for (Method method : callMethods) {
            if (method.getName().equals("onTask")) {
                Class<?> methodParam = method.getParameterTypes()[0];
                if (methodParam != Object.class) {
                    callMethod = method;
                    break;
                }
                // break;
            }
        }
        Type methodParamType = callMethod.getGenericParameterTypes()[0];
        this.objClass = JsonUtil.createJavaType(methodParamType);

        loadSha();
        for (int i = 0; i < taskThreadCount; ++i) {
            new AsynOrderDispatch(this).start();
        }
    }

    private static class AsynOrderDispatch extends Thread {
        private RedisTaskDisPatchBase taskDispatch;

        public AsynOrderDispatch(RedisTaskDisPatchBase dispatch) {
            this.taskDispatch = dispatch;
        }

        @Override
        public void run() {
            this.taskDispatch.asynTask();
        }
    }

    public int getTaskIntervalTime() {
        return taskIntervalTime;
    }

    public void setTaskIntervalTime(int taskIntervalTime) {
        this.taskIntervalTime = taskIntervalTime;
    }

    public boolean isDistributeSync() {
        return distributeSync;
    }

    public void setDistributeSync(boolean distributeSync) {
        this.distributeSync = distributeSync;
    }

    public int getTaskThreadCount() {
        return taskThreadCount;
    }

    public void setTaskThreadCount(int taskThreadCount) {
        this.taskThreadCount = taskThreadCount;
    }

    public int getEachMaxTaskCount() {
        return eachMaxTaskCount;
    }

    public void setEachMaxTaskCount(int eachMaxTaskCount) {
        this.eachMaxTaskCount = eachMaxTaskCount;
    }

    public long getTaskMaxExcuteTime() {
        return taskMaxExcuteTime;
    }

    public void setTaskMaxExcuteTime(long taskMaxExcuteTime) {
        this.taskMaxExcuteTime = taskMaxExcuteTime;
    }

    public String getTaskWaitName() {
        return taskWaitName;
    }

    public boolean isAutoDelTask() {
        return autoDelTask;
    }

    public void setAutoDelTask(boolean autoDelTask) {
        this.autoDelTask = autoDelTask;
    }

    public void setTaskWaitName(String taskWaitName) {
        this.taskWaitName = taskWaitName;
    }
}
