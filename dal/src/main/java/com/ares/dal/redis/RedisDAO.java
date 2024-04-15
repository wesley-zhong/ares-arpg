package com.ares.dal.redis;

import com.fasterxml.jackson.databind.JavaType;
import io.lettuce.core.ScoredValue;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RedisDAO implements IRedisDAO {
    private RedisFactory redis;

    public RedisDAO(RedisFactory redisFactory) {
        this.redis = redisFactory;
    }

    public boolean set(final String key, final Object value) {
        return redis.set(key, value).equals("OK");
    }

    public <T> T get(final String key, Class<T> type) {
        return redis.get(key, type);
    }

    @Override
    public String get(String key) {
        return redis.get(key);
    }

    @Override
    public <T> Map<String, T> mget(Type type, String... keys) {
        return redis.mget(type, keys);
    }

    @Override
    public <T> Map<String, T> mget(Type type, List<String> keys) {
        String[] args = new String[keys.size()];
        keys.toArray(args);
        return redis.mget(type, args);
    }

    public <T> Map<String, T> mget(Class<T> type, final String... keys) {
        return redis.mget(type, keys);
    }


    public <T> List<T> getList(final String key, Type type) {
        return redis.getList(key, type);
    }

    public boolean setnx(final String key, final Object value) {
        return redis.setnx(key, value);
    }

    public boolean setex(final String key, final int seconds, final Object value) {
        return redis.setex(key, seconds, value).equals("OK");
    }


    public boolean setCas(final String key, final RedisCasDO value) {
        return redis.setCas(key, value);
    }

    public boolean setexCas(final String key, final RedisCasDO value, long seconds) {
        return redis.setexCas(key, value, seconds);
    }

    public long zadd(final String key, final double score, final Object member) {
        return redis.zadd(key, score, member);
    }

    public long zrem(final String key, Object member) {
        return redis.zrem(key, member);
    }

    public Double zscore(final String key, Object member) {
        return redis.zscore(key, member);
    }

    @Override
    public boolean hset(String htable, String key, Object obj) {
        return redis.hset(htable, key, obj);
    }

    @Override
    public boolean hset(String htable, Map<String, String> objMaps) {
        return redis.hset(htable, objMaps);
    }

    @Override
    public long hinc(String htable, String key, long value) {
        return redis.hinc(htable, key, value);
    }

    @Override
    public boolean hmset(String htable, Map<String, String> objMaps) {
        ///throw  new L
        return redis.hset(htable, objMaps);
    }

    private final HashMap<String, String> emptyMap = new HashMap<String, String>() {{
        put("0", "0");
    }};

    @Override
    public boolean hmEmpty(String htable) {
        return redis.hset(htable, emptyMap);
    }

    @Override
    public <T> T hget(String htable, String key, Type type) {
        return redis.hget(htable, key, type);
    }

    @Override
    public boolean hexists(final String htable, String key) {
        return redis.hexists(htable, key);
    }


    @Override
    public <K, T> Map<K, T> hgetAll(final String htable, Class<K> kType, Class<T> type) {
        return redis.hgetAll(htable, kType, type);
    }

    @Override
    public <T> Map<String, T> hgetAll(final String htable, JavaType type) {
        return redis.hgetAll(htable, String.class, type);
    }

    @Override
    public Map<String, String> hgetAll(final String htable) {
        return redis.hgetAll(htable);
    }

    @Override
    public long hdel(String htable, String key) {
        return redis.hdel(htable, key);
    }

    public List<ScoredValue<String>> zrangeByScoreWithScores(String key, final double min, final double max, int offset, int count) {
        return redis.zrangeByScoreWithScores(key, min, max, offset, count);
    }

    public Long del(final String key) {
        return redis.del(key);
    }

    @Override
    public boolean lock(String key, Object member, long expire) {
        return false;
    }

    @Override
    public boolean unLock(String key) {
        return redis.del(key) == 1L;
    }

    public boolean lock(String key, long expire) {
        String result = redis.set(key, "", "NX", "EX", expire);
        return "OK".equalsIgnoreCase(result);
    }

    public boolean expire(final String key, final int seconds) {
        return redis.expire(key, seconds);
    }

    @Override
    public long pttl(String key) {
        return 0;
    }

    @Override
    public boolean exist(String key) {
        return false;
    }

    public boolean set(String key, final Object value, String nxxx, String expx, long time) {
        return redis.set(key, value, nxxx, expx, time).equals("OK");
    }


    public Object eval(String script, String[] key, String... values) {
        return redis.eval(script, key, values);
    }

    public Object evalsha(String sha1, String[] key, String... values) {
        return redis.evalsha(sha1, key, values);
    }

    public String scriptLoad(String script) {
        return redis.scriptLoad(script);
    }

    public Long del(final String... keys) {
        return redis.del(keys);
    }

    public Map<String, String> mget(final String... keys) {
        return redis.mget(keys);
    }

    public Long srem(String key, String member) {
        return redis.srem(key, member);
    }

    @Override
    public long incr(String key) {
        return redis.incr(key);
    }

    @Override
    public long decr(String key) {
        return redis.decr(key);
    }

    public double zscore(String key, String member) {
        return redis.zscore(key, member);
    }

    public Set<String> smembers(String key) {
        return redis.smembers(key);
    }

    public void sadd(String key, String member) {
        redis.sadd(key, member);
    }

    @Override
    public boolean sismember(String key, String member) {
        return redis.sismember(key, member);
    }
}
