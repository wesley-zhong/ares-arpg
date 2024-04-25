package com.ares.dal.redis;

import com.ares.core.json.transcoder.JsonObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.ScoredValue;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
public abstract class RedisFactory {
    protected final static ObjectMapper objectMapper = JsonObjectMapper.getInstance();
    protected static String CAS_SET_LUA = " local strDataObj = redis.call('get',KEYS[1]);" + " if(strDataObj ==  false) then" + "    redis.call('set',KEYS[1],ARGV[2])" + "  return 1 " + " end" + " local  dataObj = cjson.decode(strDataObj);" + " if(dataObj.ver ==  (ARGV[1] -1)) then" + "  redis.call('set',KEYS[1],ARGV[2])" + "  return 1" + " else" + "   return 0 " + " end";
    protected static String CAS_SETEX_LUA = " local strDataObj = redis.call('get',KEYS[1]);" + " if(strDataObj ==  false) then" + "    redis.call('set',KEYS[1],ARGV[2])" + "  return 1 " + " end" + " local  dataObj = cjson.decode(strDataObj);" + " if(dataObj.ver ==  (ARGV[1] -1)) then" + "  redis.call('setex',KEYS[1],ARGV[3], ARGV[2])" + "  return 1" + " else" + "   return 0 " + " end";
    protected String SHA_CAS_LUS;
    protected String SHA_CAS_SETEX_LUA;

    public abstract void initPool(String redisIp);

    public abstract String set(final String key, final Object value);

    public abstract <T> T get(final String key, Class<T> objClass);

    public abstract String get(final String key);

    public abstract <T> List<T> getList(final String key, Type type);

    public abstract boolean setnx(final String key, final Object value);

    public abstract String setex(final String key, final int seconds, final Object value);

    public abstract boolean setCas(final String key, final RedisCasDO value);

    public abstract boolean setexCas(final String key, final RedisCasDO value, long timeOut);

    public abstract long zadd(final String key, final double score, final Object member);

    public abstract long zrem(final String key, Object member);

    public abstract List<ScoredValue<String>> zrangeByScoreWithScores(String key, final double min, final double max, int offset, int count);

    public abstract Double zscore(String key, Object member);

    public abstract Long del(final String key);

    public abstract boolean expire(final String key, final int seconds);

    public abstract String set(String key, final Object value, String nxxx, String expx, long time);

    public abstract Object eval(String script, String[] keys, String... values);

    public abstract Object evalsha(String script, String[] keys, String... values);

    public abstract int evalshaInteger(String script, String[] keys, String... values);

    public abstract String scriptLoad(String script);

    public abstract Long del(final String... keys);

    public abstract Map<String, String> mget(final String... keys);

    public abstract Long srem(String key, String member);

    public abstract long incr(String key);

    public abstract Long decr(String key);

    public abstract Set<String> smembers(String key);

    public abstract long sadd(String key, String member);

    public abstract <T> Map<String, T> mget(Type type, final String... keys);

    public abstract <T> T hget(String htable, String key, Type type);

    public abstract boolean hexists(String htable, String key);

    public abstract boolean hset(String htable, String key, Object obj);

    public abstract boolean hset(String htable, Map<String, String> objectMap);

    public abstract long hdel(String htable, String key);

    public abstract boolean sismember(String key, String member);

    public abstract Map<String, String> hgetAll(String htable);

    public abstract <K, T> Map<K, T> hgetAll(String htable, Type kType, Type type);

    public abstract long hinc(String htable, String key, long value);
}
