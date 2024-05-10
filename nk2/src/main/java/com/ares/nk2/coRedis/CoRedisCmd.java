package com.ares.nk2.coRedis;


import io.lettuce.core.*;
import io.lettuce.core.api.async.RedisAsyncCommands;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;


/**
 * Created by levoneliu on 2018/9/4.
 */
public class CoRedisCmd<K, V> {
    private RedisAsyncCommands<K, V> nativeCmd;
    private long timeoutMs;


    public long getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(long timeoutMs) {
        this.timeoutMs = timeoutMs;
    }


    public CoRedisCmd(RedisAsyncCommands<K, V> nativeCmd) {
        this.nativeCmd = nativeCmd;
        timeoutMs = 3000;
    }

    public static CoRedisCmd wrap(RedisAsyncCommands nativeCmd) {
        return new CoRedisCmd(nativeCmd);
    }

    RedisAsyncCommands<K, V> getNativeCmd() {
        return nativeCmd;
    }


    class CmdGet implements Supplier<RedisFuture<V>> {
        K key;

        CmdGet(K key) {
            this.key = key;
        }

        @Override
        public RedisFuture<V> get() {
            return nativeCmd.get(key);
        }
    }

    public V get(K key) throws InterruptedException, TimeoutException {
        V res = new CoRedisAsync<>(new CmdGet(key)).run(timeoutMs);
        return res;
    }

    class CmdSet implements Supplier<RedisFuture<String>> {
        K key;
        V v;

        CmdSet(K key, V v) {
            this.key = key;
            this.v = v;
        }

        @Override
        public RedisFuture<String> get() {
            return nativeCmd.set(key, v);
        }
    }

    public String set(K k, V v) throws InterruptedException, TimeoutException {
        String res = new CoRedisAsync<>(new CmdSet(k, v)).run(timeoutMs);
        return res;
    }

    class CmdSetByArgs implements Supplier<RedisFuture<String>> {
        K key;
        V v;
        SetArgs args;

        CmdSetByArgs(K key, V v, SetArgs args) {
            this.key = key;
            this.v = v;
            this.args = args;
        }

        @Override
        public RedisFuture<String> get() {
            return nativeCmd.set(key, v, args);
        }
    }

    public String set(K k, V v, SetArgs args) throws InterruptedException, TimeoutException {
        String res = new CoRedisAsync<>(new CmdSetByArgs(k, v, args)).run(timeoutMs);
        return res;
    }


    class CmdDel implements Supplier<RedisFuture<Long>> {
        K key[];

        CmdDel(K key[]) {
            this.key = key;
        }

        @Override
        public RedisFuture<Long> get() {
            return nativeCmd.del(key);
        }
    }


    @SafeVarargs
    public final Long del(K... keys) throws InterruptedException, TimeoutException {
        Long res = new CoRedisAsync<>(new CmdDel(keys)).run(timeoutMs);
        return res;
    }

    class CmdMset implements Supplier<RedisFuture<String>> {
        Map<K, V> map;

        CmdMset(Map<K, V> map) {
            this.map = map;
        }

        @Override
        public RedisFuture<String> get() {
            return nativeCmd.mset(map);
        }
    }

    public String mset(Map<K, V> map) throws InterruptedException, TimeoutException {
        String res = new CoRedisAsync<>(new CmdMset(map)).run(timeoutMs);
        return res;
    }

    class CmdMget implements Supplier<RedisFuture<List<KeyValue<K, V>>>> {
        K key[];

        CmdMget(K key[]) {
            this.key = key;
        }

        @Override
        public RedisFuture<List<KeyValue<K, V>>> get() {
            return nativeCmd.mget(key);
        }
    }

    @SafeVarargs
    public final List<KeyValue<K, V>> mget(K... keys) throws InterruptedException, TimeoutException {
        List<KeyValue<K, V>> res = new CoRedisAsync<>(new CmdMget(keys)).run(timeoutMs);
        return res;
    }

    class CmdSetnx implements Supplier<RedisFuture<Boolean>> {
        K key;
        V value;

        CmdSetnx(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public RedisFuture<Boolean> get() {
            return nativeCmd.setnx(key, value);
        }
    }

    public Boolean setnx(K key, V value) throws InterruptedException, TimeoutException {
        Boolean res = new CoRedisAsync<>(new CmdSetnx(key, value)).run(timeoutMs);
        return res;
    }

    class CmdSetex implements Supplier<RedisFuture<String>> {
        K key;
        int seconds;
        V value;

        CmdSetex(K key, int seconds, V value) {
            this.key = key;
            this.seconds = seconds;
            this.value = value;
        }

        @Override
        public RedisFuture<String> get() {
            return nativeCmd.setex(key, seconds, value);
        }
    }

    public String setex(K key, int seconds, V value) throws InterruptedException, TimeoutException {
        String res = new CoRedisAsync<>(new CmdSetex(key, seconds, value)).run(timeoutMs);
        return res;
    }


    class CmdHSetnx implements Supplier<RedisFuture<Boolean>> {
        K key;
        K field;
        V value;

        CmdHSetnx(K key, K field, V value) {
            this.key = key;
            this.field = field;
            this.value = value;
        }

        @Override
        public RedisFuture<Boolean> get() {
            return nativeCmd.hsetnx(key, field, value);
        }
    }

    public Boolean hsetnx(K key, K field, V value) throws InterruptedException, TimeoutException {
        Boolean res = new CoRedisAsync<>(new CmdHSetnx(key, field, value)).run(timeoutMs);
        return res;
    }

    class CmdSmembers implements Supplier<RedisFuture<Set<V>>> {
        K key;

        CmdSmembers(K key) {
            this.key = key;
        }

        @Override
        public RedisFuture<Set<V>> get() {
            return nativeCmd.smembers(key);
        }
    }

    public Set<V> smembers(K key) throws InterruptedException, TimeoutException {
        Set<V> res = new CoRedisAsync<>(new CmdSmembers(key)).run(timeoutMs);
        return res;
    }

    class CmdSrem implements Supplier<RedisFuture<Long>> {
        K key;
        V[] members;

        CmdSrem(K key, V[] members) {
            this.key = key;
            this.members = members;
        }

        @Override
        public RedisFuture<Long> get() {
            return nativeCmd.srem(key, members);
        }
    }

    @SafeVarargs
    public final Long srem(K key, V... members) throws InterruptedException, TimeoutException {
        Long res = new CoRedisAsync<>(new CmdSrem(key, members)).run(timeoutMs);
        return res;
    }

    class CmdSismember implements Supplier<RedisFuture<Boolean>> {
        K key;
        V member;

        CmdSismember(K key, V member) {
            this.key = key;
            this.member = member;
        }

        @Override
        public RedisFuture<Boolean> get() {
            return nativeCmd.sismember(key, member);
        }
    }

    public Boolean sismember(K key, V member) throws InterruptedException, TimeoutException {
        Boolean res = new CoRedisAsync<>(new CmdSismember(key, member)).run(timeoutMs);
        return res;
    }


    class CmdSadd implements Supplier<RedisFuture<Long>> {
        K key;
        V[] members;

        CmdSadd(K key, V[] members) {
            this.key = key;
            this.members = members;
        }

        @Override
        public RedisFuture<Long> get() {
            return nativeCmd.sadd(key, members);
        }
    }


    @SafeVarargs
    public final Long sadd(K key, V... members) throws InterruptedException, TimeoutException {
        return new CoRedisAsync<>(new CmdSadd(key, members)).run(timeoutMs);
    }

    class CmdEval<T> implements Supplier<RedisFuture<T>> {
        K[] keys;
        V[] values;
        String script;
        ScriptOutputType type;

        CmdEval(String script, ScriptOutputType type, K[] keys, V... values) {
            this.script = script;
            this.type = type;
            this.keys = keys;
            this.values = values;
        }

        @Override
        public RedisFuture<T> get() {
            return nativeCmd.eval(script, type, keys, values);
        }
    }

    @SafeVarargs
    public final <T> T eval(String script, ScriptOutputType type, K[] keys, V... values) throws InterruptedException, TimeoutException {
        return (T) new CoRedisAsync<>(new CmdEval<T>(script, type, keys, values)).run(timeoutMs);
    }

    class CmdEvalSha<T> implements Supplier<RedisFuture<T>> {
        K[] keys;
        V[] values;
        String script;
        ScriptOutputType type;

        CmdEvalSha(String script, ScriptOutputType type, K[] keys, V... values) {
            this.script = script;
            this.type = type;
            this.keys = keys;
            this.values = values;
        }

        @Override
        public RedisFuture<T> get() {
            return nativeCmd.eval(script, type, keys, values);
        }
    }

    public final <T> T evalsha(String digest, ScriptOutputType type, K[] keys, V... values) throws InterruptedException, TimeoutException {
        return (T) new CoRedisAsync<>(new CmdEvalSha<T>(digest, type, keys, values)).run(timeoutMs);
    }

    class CmdScriptLoad implements Supplier<RedisFuture<String >> {
        String script;

        CmdScriptLoad(String script) {
            this.script = script;
        }

        @Override
        public RedisFuture<String> get() {
            return nativeCmd.scriptLoad(script);
        }
    }

    public final String scriptLoad(String script) throws InterruptedException, TimeoutException {
        return new CoRedisAsync<>(new CmdScriptLoad(script)).run(timeoutMs);
    }

    class CmdLpush implements Supplier<RedisFuture<Long>> {
        K key;
        V[] values;

        CmdLpush(K key, V[] values) {
            this.key = key;
            this.values = values;
        }

        @Override
        public RedisFuture<Long> get() {
            return nativeCmd.lpush(key, values);
        }
    }


    @SafeVarargs
    public final Long lpush(K key, V... values) throws InterruptedException, TimeoutException {
        return new CoRedisAsync<>(new CmdLpush(key, values)).run(timeoutMs);
    }

    class CmdLrange implements Supplier<RedisFuture<List<V>>> {
        K key;
        long start, stop;

        CmdLrange(K key, long start, long stop) {
            this.key = key;
            this.start = start;
            this.stop = stop;
        }

        @Override
        public RedisFuture<List<V>> get() {
            return nativeCmd.lrange(key, start, stop);
        }
    }

    public List<V> lrange(K key, long start, long stop) throws InterruptedException, TimeoutException {
        return new CoRedisAsync<>(new CmdLrange(key, start, stop)).run(timeoutMs);
    }

    class CmdRpop implements Supplier<RedisFuture<V>> {
        K key;

        CmdRpop(K key) {
            this.key = key;
        }

        @Override
        public RedisFuture<V> get() {
            return nativeCmd.rpop(key);
        }
    }

    public V rpop(K key) throws InterruptedException, TimeoutException {
        return new CoRedisAsync<>(new CmdRpop(key)).run(timeoutMs);
    }

    class CmdKeys implements Supplier<RedisFuture<List<K>>> {
        K key;

        CmdKeys(K key) {
            this.key = key;
        }

        @Override
        public RedisFuture<List<K>> get() {
            return nativeCmd.keys(key);
        }
    }


    public List<K> keys(K pattern) throws InterruptedException, TimeoutException {
        return new CoRedisAsync<>(new CmdKeys(pattern)).run(timeoutMs);
    }

    class CmdLindex implements Supplier<RedisFuture<V>> {
        K key;
        long index;

        CmdLindex(K key, long index) {
            this.key = key;
            this.index = index;
        }

        @Override
        public RedisFuture<V> get() {
            return nativeCmd.lindex(key, index);
        }
    }


    public V lindex(K key, long index) throws InterruptedException, TimeoutException {
        return new CoRedisAsync<>(new CmdLindex(key, index)).run(timeoutMs);
    }

    class CmdRpush implements Supplier<RedisFuture<Long>> {
        K key;
        V[] values;

        CmdRpush(K key, V[] values) {
            this.key = key;
            this.values = values;
        }

        @Override
        public RedisFuture<Long> get() {
            return nativeCmd.rpush(key, values);
        }
    }

    @SafeVarargs
    public final Long rpush(K key, V... values) throws InterruptedException, TimeoutException {
        return new CoRedisAsync<>(new CmdRpush(key, values)).run(timeoutMs);
    }

    class CmdIncrby implements Supplier<RedisFuture<Long>> {
        K key;
        long amount;

        CmdIncrby(K key, long amount) {
            this.key = key;
            this.amount = amount;
        }

        @Override
        public RedisFuture<Long> get() {
            return nativeCmd.incrby(key, amount);
        }
    }

    public Long incrby(K key, long amount) throws InterruptedException, TimeoutException {
        return new CoRedisAsync<>(new CmdIncrby(key, amount)).run(timeoutMs);
    }

    class CmdIncr implements Supplier<RedisFuture<Long>> {
        K key;

        CmdIncr(K key) {
            this.key = key;
        }

        @Override
        public RedisFuture<Long> get() {
            return nativeCmd.incr(key);
        }
    }

    public Long incr(K key) throws InterruptedException, TimeoutException {
        return new CoRedisAsync<>(new CmdIncr(key)).run(timeoutMs);
    }

    class CmdDecr implements Supplier<RedisFuture<Long>> {
        K key;

        CmdDecr(K key) {
            this.key = key;
        }

        @Override
        public RedisFuture<Long> get() {
            return nativeCmd.decr(key);
        }
    }

    public Long decr(K key) throws InterruptedException, TimeoutException {
        return new CoRedisAsync<>(new CmdDecr(key)).run(timeoutMs);
    }

    class CmdHget implements Supplier<RedisFuture<V>> {
        K key;
        K field;

        CmdHget(K key, K field) {
            this.key = key;
            this.field = field;
        }

        @Override
        public RedisFuture<V> get() {
            return nativeCmd.hget(key, field);
        }
    }


    public V hget(K key, K field) throws InterruptedException, TimeoutException {
        return new CoRedisAsync<>(new CmdHget(key, field)).run(timeoutMs);
    }

    class CmdHmget implements Supplier<RedisFuture<List<KeyValue<K, V>>>> {
        K key;
        K[] fields;

        CmdHmget(K key, K[] fields) {
            this.key = key;
            this.fields = fields;
        }

        @Override
        public RedisFuture<List<KeyValue<K, V>>> get() {
            return nativeCmd.hmget(key, fields);
        }
    }

    @SafeVarargs
    public final List<KeyValue<K, V>> hmget(K key, K... fields) throws InterruptedException, TimeoutException {
        return new CoRedisAsync<>(new CmdHmget(key, fields)).run(timeoutMs);
    }

    class CmdHgetall implements Supplier<RedisFuture<Map<K, V>>> {
        K key;

        CmdHgetall(K key) {
            this.key = key;
        }

        @Override
        public RedisFuture<Map<K, V>> get() {
            return nativeCmd.hgetall(key);
        }
    }

    public Map<K, V> hgetall(K key) throws InterruptedException, TimeoutException {
        return new CoRedisAsync<>(new CmdHgetall(key)).run(timeoutMs);
    }

    class CmdExpire implements Supplier<RedisFuture<Boolean>> {
        K key;
        long seconds;

        CmdExpire(K key, long seconds) {
            this.key = key;
            this.seconds = seconds;
        }

        @Override
        public RedisFuture<Boolean> get() {
            return nativeCmd.expire(key, seconds);
        }
    }

    public Boolean expire(K key, long seconds) throws InterruptedException, TimeoutException {
        return new CoRedisAsync<>(new CmdExpire(key, seconds)).run(timeoutMs);
    }

    class CmdPexpireat implements Supplier<RedisFuture<Boolean>> {
        K key;
        long timestamp;

        CmdPexpireat(K key, long timestamp) {
            this.key = key;
            this.timestamp = timestamp;
        }

        @Override
        public RedisFuture<Boolean> get() {
            return nativeCmd.pexpireat(key, timestamp);
        }
    }


    public Boolean pexpireat(K key, long timestamp) throws InterruptedException, TimeoutException {
        return new CoRedisAsync<>(new CmdPexpireat(key, timestamp)).run(timeoutMs);
    }


    class CmdHdel implements Supplier<RedisFuture<Long>> {
        K key;
        K[] fields;

        CmdHdel(K key, K[] fields) {
            this.key = key;
            this.fields = fields;
        }

        @Override
        public RedisFuture<Long> get() {
            return nativeCmd.hdel(key, fields);
        }
    }

    @SafeVarargs
    public final Long hdel(K key, K... fields) throws InterruptedException, TimeoutException {
        return new CoRedisAsync<>(new CmdHdel(key, fields)).run(timeoutMs);
    }

    class CmdHmset implements Supplier<RedisFuture<String>> {
        K key;
        Map<K, V> map;

        CmdHmset(K key, Map<K, V> map) {
            this.key = key;
            this.map = map;
        }

        @Override
        public RedisFuture<String> get() {
            return nativeCmd.hmset(key, map);
        }
    }

    public String hmset(K key, Map<K, V> map) throws InterruptedException, TimeoutException {
        return new CoRedisAsync<>(new CmdHmset(key, map)).run(timeoutMs);
    }

    class CmdHlen implements Supplier<RedisFuture<Long>> {
        K key;

        CmdHlen(K key) {
            this.key = key;
        }

        @Override
        public RedisFuture<Long> get() {
            return nativeCmd.hlen(key);
        }
    }

    public Long hlen(K key) throws InterruptedException, TimeoutException {
        return new CoRedisAsync<>(new CmdHlen(key)).run(timeoutMs);
    }

    class CmdHexists implements Supplier<RedisFuture<Boolean>> {
        K key;
        K field;

        CmdHexists(K key, K field) {
            this.key = key;
            this.field = field;
        }

        @Override
        public RedisFuture<Boolean> get() {
            return nativeCmd.hexists(key, field);
        }
    }

    public Boolean hexists(K key, K field) throws InterruptedException, TimeoutException {
        return new CoRedisAsync<>(new CmdHexists(key, field)).run(timeoutMs);
    }

    class CmdHset implements Supplier<RedisFuture<Boolean>> {
        K key;
        K field;
        V value;

        CmdHset(K key, K field, V value) {
            this.key = key;
            this.field = field;
            this.value = value;
        }

        @Override
        public RedisFuture<Boolean> get() {
            return nativeCmd.hset(key, field, value);
        }
    }

    public Boolean hset(K key, K field, V value) throws InterruptedException, TimeoutException {
        return new CoRedisAsync<>(new CmdHset(key, field, value)).run(timeoutMs);
    }

    class CmdLtrim implements Supplier<RedisFuture<String>> {
        K key;
        long start, stop;

        CmdLtrim(K key, long start, long stop) {
            this.key = key;
            this.start = start;
            this.stop = stop;
        }

        @Override
        public RedisFuture<String> get() {
            return nativeCmd.ltrim(key, start, stop);
        }
    }

    public String ltrim(K key, long start, long stop) throws InterruptedException, TimeoutException {
        return new CoRedisAsync<>(new CmdLtrim(key, start, stop)).run(timeoutMs);
    }

    class CmdLset implements Supplier<RedisFuture<String>> {
        K key;
        long start;
        V value;

        CmdLset(K key, long start, V value) {
            this.key = key;
            this.start = start;
            this.value = value;
        }

        @Override
        public RedisFuture<String> get() {
            return nativeCmd.lset(key, start, value);
        }
    }

    public String lset(K key, long index, V value) throws InterruptedException, TimeoutException {
        return new CoRedisAsync<>(new CmdLset(key, index, value)).run(timeoutMs);
    }


    class CmdLlen implements Supplier<RedisFuture<Long>> {
        K key;

        CmdLlen(K key) {
            this.key = key;
        }

        @Override
        public RedisFuture<Long> get() {
            return nativeCmd.llen(key);
        }
    }


    public Long llen(K key) throws InterruptedException, TimeoutException {
        return new CoRedisAsync<>(new CmdLlen(key)).run(timeoutMs);
    }

    class CmdLrem implements Supplier<RedisFuture<Long>> {
        K key;
        long start;
        V value;

        CmdLrem(K key, long start, V value) {
            this.key = key;
            this.start = start;
            this.value = value;
        }

        @Override
        public RedisFuture<Long> get() {
            return nativeCmd.lrem(key, start, value);
        }
    }

    public Long lrem(K key, long count, V value) throws InterruptedException, TimeoutException {
        return new CoRedisAsync<>(new CmdLrem(key, count, value)).run(timeoutMs);
    }

    class CmdHincrby implements Supplier<RedisFuture<Long>> {
        K key;
        K field;
        long amount;

        CmdHincrby(K key, K field, long amount) {
            this.key = key;
            this.field = field;
            this.amount = amount;
        }

        @Override
        public RedisFuture<Long> get() {
            return nativeCmd.hincrby(key, field, amount);
        }
    }

    public Long hincrby(K key, K field, long amount) throws InterruptedException, TimeoutException {
        return new CoRedisAsync<>(new CmdHincrby(key, field, amount)).run(timeoutMs);
    }

    class CmdZcard implements Supplier<RedisFuture<Long>> {
        K key;

        CmdZcard(K key) {
            this.key = key;
        }

        @Override
        public RedisFuture<Long> get() {
            return nativeCmd.zcard(key);
        }
    }


    public Long zcard(K key) throws InterruptedException, TimeoutException {
        return new CoRedisAsync<>(new CmdZcard(key)).run(timeoutMs);
    }


    class CmdZadd implements Supplier<RedisFuture<Long>> {
        K key;
        double score;
        V member;

        CmdZadd(K key, double score, V member) {
            this.key = key;
            this.score = score;
            this.member = member;
        }

        @Override
        public RedisFuture<Long> get() {
            return nativeCmd.zadd(key, score, member);
        }
    }


    public Long zadd(K key, double score, V member) throws InterruptedException, TimeoutException {
        return new CoRedisAsync<>(new CmdZadd(key, score, member)).run(timeoutMs);
    }

    class CmdZrem implements Supplier<RedisFuture<Long>> {
        K key;
        V member;

        CmdZrem(K key, V member) {
            this.key = key;
            this.member = member;
        }

        @Override
        public RedisFuture<Long> get() {
            return nativeCmd.zrem(key, member);
        }
    }

    public Long zrem(K key, V member) throws InterruptedException, TimeoutException {
        return new CoRedisAsync<>(new CmdZrem(key, member)).run(timeoutMs);
    }

    class CmdZrangebyscoreWithScores implements Supplier<RedisFuture<List<ScoredValue<V>>>> {
        K key;
        Range range;
        Limit limit;

        CmdZrangebyscoreWithScores(K key, Range range, Limit limit) {
            this.key = key;
            this.range = range;
            this.limit = limit;
        }

        @Override
        public RedisFuture<List<ScoredValue<V>>> get() {
            return nativeCmd.zrangebyscoreWithScores(key, range, limit);
        }
    }

    public List<ScoredValue<V>> zrangebyscoreWithScores(K key, Range range, Limit limit) throws InterruptedException, TimeoutException {
        return new CoRedisAsync<>(new CmdZrangebyscoreWithScores(key, range, limit)).run(timeoutMs);
    }

    class CmdZscore implements Supplier<RedisFuture<Double>> {
        K key;
        V member;

        CmdZscore(K key, V member) {
            this.key = key;
            this.member = member;
        }

        @Override
        public RedisFuture<Double> get() {
            return nativeCmd.zscore(key, member);
        }
    }


    public Double zscore(K key, V member) throws InterruptedException, TimeoutException {
        return new CoRedisAsync<>(new CmdZscore(key, member)).run(timeoutMs);
    }

    class CmdZrank implements Supplier<RedisFuture<Long>> {
        K key;
        V member;

        CmdZrank(K key, V member) {
            this.key = key;
            this.member = member;
        }

        @Override
        public RedisFuture<Long> get() {
            return nativeCmd.zrank(key, member);
        }
    }

    public Long zrank(K key, V member) throws InterruptedException, TimeoutException {
        return new CoRedisAsync<>(new CmdZrank(key, member)).run(timeoutMs);
    }

    class CmdZrevrank implements Supplier<RedisFuture<Long>> {
        K key;
        V member;

        CmdZrevrank(K key, V member) {
            this.key = key;
            this.member = member;
        }

        @Override
        public RedisFuture<Long> get() {
            return nativeCmd.zrevrank(key, member);
        }
    }

    public Long zrevrank(K key, V member) throws InterruptedException, TimeoutException {
        return new CoRedisAsync<>(new CmdZrevrank(key, member)).run(timeoutMs);
    }

    class CmdZremrangebyrank implements Supplier<RedisFuture<Long>> {
        K key;
        long start, stop;

        CmdZremrangebyrank(K key, long start, long stop) {
            this.key = key;
            this.start = start;
            this.stop = stop;
        }

        @Override
        public RedisFuture<Long> get() {
            return nativeCmd.zremrangebyrank(key, start, stop);
        }
    }

    public Long zremrangebyrank(K key, long start, long stop) throws InterruptedException, TimeoutException {
        return new CoRedisAsync<>(new CmdZremrangebyrank(key, start, stop)).run(timeoutMs);
    }

    class CmdTtl implements Supplier<RedisFuture<Long>> {
        K key;

        CmdTtl(K key) {
            this.key = key;
        }

        @Override
        public RedisFuture<Long> get() {
            return nativeCmd.ttl(key);
        }
    }

    public Long ttl(K key) throws InterruptedException, TimeoutException {
        return new CoRedisAsync<>(new CmdTtl(key)).run(timeoutMs);
    }

    class CmdZrange implements Supplier<RedisFuture<List<V>>> {
        K key;
        long start, stop;

        CmdZrange(K key, long start, long stop) {
            this.key = key;
            this.start = start;
            this.stop = stop;
        }

        @Override
        public RedisFuture<List<V>> get() {
            return nativeCmd.zrange(key, start, stop);
        }
    }


    public List<V> zrange(K key, long start, long stop) throws InterruptedException, TimeoutException {
        return new CoRedisAsync<>(new CmdZrange(key, start, stop)).run(timeoutMs);
    }

    class CmdZrangeWithScores implements Supplier<RedisFuture<List<ScoredValue<V>>>> {
        K key;
        long start, stop;

        CmdZrangeWithScores(K key, long start, long stop) {
            this.key = key;
            this.start = start;
            this.stop = stop;
        }

        @Override
        public RedisFuture<List<ScoredValue<V>>> get() {
            return nativeCmd.zrangeWithScores(key, start, stop);
        }
    }


    public List<ScoredValue<V>> zrangeWithScores(K key, long start, long stop) throws InterruptedException, TimeoutException {
        return new CoRedisAsync<>(new CmdZrangeWithScores(key, start, stop)).run(timeoutMs);
    }

    class CmdZrevrangeWithScores implements Supplier<RedisFuture<List<ScoredValue<V>>>> {
        K key;
        long start, stop;

        CmdZrevrangeWithScores(K key, long start, long stop) {
            this.key = key;
            this.start = start;
            this.stop = stop;
        }

        @Override
        public RedisFuture<List<ScoredValue<V>>> get() {
            return nativeCmd.zrevrangeWithScores(key, start, stop);
        }
    }

    public List<ScoredValue<V>> zrevrangeWithScores(K key, long start, long stop) throws InterruptedException, TimeoutException {
        return new CoRedisAsync<>(new CmdZrevrangeWithScores(key, start, stop)).run(timeoutMs);
    }

}
