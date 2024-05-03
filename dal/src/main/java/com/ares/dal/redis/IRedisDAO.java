package com.ares.dal.redis;

import com.fasterxml.jackson.databind.JavaType;
import io.lettuce.core.ScoredValue;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IRedisDAO {
    boolean set(final String key, final Object value);

    <T> T get(final String key, Class<T> type);

    String get(final String key);

    <T> Map<String, T> mget(Type type, final String... keys);

    <T> Map<String, T> mget(Type type, List<String> keys);

    Map<String, String> mget(final String... keys);

    <T> List<T> getList(final String key, Type type);

    boolean setnx(final String key, final Object value);

    boolean setex(final String key, final int seconds, final Object value);

    boolean setCas(final String key, final RedisCasDO value);

    boolean setexCas(final String key, final RedisCasDO value, long seconds);

    long zadd(final String key, final double score, final Object member);

    long zrem(final String key, Object member);

    Double zscore(final String key, Object member);

    boolean hset(final String htable, String key, Object obj);

    boolean hset(final String htable, Map<String, String> objectMaps);

    long hinc(final String htable, String key, long value);

    boolean hmset(final String htable, Map<String, String> objMaps);

    boolean hmEmpty(final String htable);


    <T> T hget(final String htable, String key, Type type);

    boolean hexists(final String htable, String key);

    //    <T> Map<String, T> hgetAll(final String htable, Class<T> type);
    <K, T> Map<K, T> hgetAll(final String htable, Class<K> kType, Class<T> type);

    <T> Map<String, T> hgetAll(final String htable, JavaType type);

    Map<String, String> hgetAll(final String htable);

    long hdel(String htable, String key);

    List<ScoredValue<String>> zrangeByScoreWithScores(String key, final double min, final double max, int offset, int count);

    Long del(final String key);

    boolean lock(String key, Object member, long expire);

    boolean unLock(String key);

    boolean expire(final String key, final int seconds);

    long pttl(final String key);

    boolean exist(String key);

    boolean set(String key, final Object value, String nxxx, String expx, long time);

    Object eval(String script, String[] key, String... values);

    Object evalsha(String sha1, String[] key, String... values);

    String scriptLoad(String scriptLoad);

    Long del(final String... keys);

    Long srem(String key, String member);

    long incr(String key);

    long decr(String key);

    double zscore(String key, String member);

    Set<String> smembers(String key);

    void sadd(String key, String member);

    <T> T getAndSet(String key, T obj);

    /**
     * 如果key 存在 返回key value， 如果key 不存在 set Key  value，并返回最新VALUE
     * @param key
     * @param obj
     * @return
     * @param <T>
     */
    <T> T getAndSetN(String key, T obj);

    boolean sismember(String allAppidsKey, String value);
}
