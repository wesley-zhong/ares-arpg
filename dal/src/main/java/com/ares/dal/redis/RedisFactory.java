package com.ares.dal.redis;

import com.ares.core.constdata.FConst;
import com.ares.core.json.transcoder.JsonObjectMapper;
import com.ares.core.utils.JsonUtil;
import com.ares.core.utils.StringUtils;
import com.ares.dal.exception.RedisException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.*;
import io.lettuce.core.api.sync.RedisCommands;
import lombok.extern.slf4j.Slf4j;

import org.springframework.util.CollectionUtils;

import java.lang.reflect.Type;
import java.util.*;


@Slf4j
public class RedisFactory {
    private final static ObjectMapper objectMapper = JsonObjectMapper.getInstance();
    private static String CAS_SET_LUA = " local strDataObj = redis.call('get',KEYS[1]);" + " if(strDataObj ==  false) then" + "    redis.call('set',KEYS[1],ARGV[2])" + "  return 1 " + " end" + " local  dataObj = cjson.decode(strDataObj);" + " if(dataObj.ver ==  (ARGV[1] -1)) then" + "  redis.call('set',KEYS[1],ARGV[2])" + "  return 1" + " else" + "   return 0 " + " end";
    private static String CAS_SETEX_LUA = " local strDataObj = redis.call('get',KEYS[1]);" + " if(strDataObj ==  false) then" + "    redis.call('set',KEYS[1],ARGV[2])" + "  return 1 " + " end" + " local  dataObj = cjson.decode(strDataObj);" + " if(dataObj.ver ==  (ARGV[1] -1)) then" + "  redis.call('setex',KEYS[1],ARGV[3], ARGV[2])" + "  return 1" + " else" + "   return 0 " + " end";
    private RedisCommands<String, String> redisCommands;
    private String SHA_CAS_LUS;
    private String SHA_CAS_SETEX_LUA;

    public void initPool(String redisIp) {
        log.info("####### redis  ip: {}", redisIp);
        RedisClient redisClient = RedisClient.create(redisIp);
        redisCommands = redisClient.connect().sync();
        SHA_CAS_LUS = scriptLoad(CAS_SET_LUA);
        SHA_CAS_SETEX_LUA = scriptLoad(CAS_SETEX_LUA);
    }

    public String set(final String key, final Object value) {
        try {
            String body = objectMapper.writeValueAsString(value);
            return redisCommands.set(key, body);
        } catch (Exception e) {
            throw new RedisException(e.getMessage());
        }
    }

    public <T> T get(final String key, Class<T> objClass) {
        try {
            String keyBody = redisCommands.get(key);
            if (StringUtils.isNullOrEmpty(keyBody)) return null;
            return objectMapper.readValue(keyBody, objClass);
        } catch (Exception e) {
            log.error(FConst.ERROR_REDIS_ERROR_MSG, e);
        }
        return null;
    }

    public String get(final String key) {
        return redisCommands.get(key);
    }

    public <T> List<T> getList(final String key, Type type) {
        try {
            String keyBody = redisCommands.get(key);
            if (StringUtils.isNullOrEmpty(keyBody)) return null;
            JavaType javaType = objectMapper.getTypeFactory().constructParametricType(List.class, (Class<?>) type);
            return (List<T>) objectMapper.readValue(keyBody, javaType);
        } catch (Exception e) {
            log.error(FConst.ERROR_REDIS_ERROR_MSG, e);
        }
        return null;
    }


    public boolean setnx(final String key, final Object value) {
        try {
            String strValue = objectMapper.writeValueAsString(value);
            return redisCommands.setnx(key, strValue);
        } catch (Exception e) {
            log.error(FConst.ERROR_REDIS_ERROR_MSG, e);
        }
        return false;
    }

    public String setex(final String key, final int seconds, final Object value) {
        try {
            String strValue = objectMapper.writeValueAsString(value);
            return redisCommands.setex(key, seconds, strValue);
        } catch (Exception e) {
            log.error(FConst.ERROR_REDIS_ERROR_MSG, e);
        }
        return null;
    }

    public boolean setCas(final String key, final RedisCasDO value) {
        try {
            String strValue = objectMapper.writeValueAsString(value);
            String keys[] = new String[1];
            keys[0] = key;

            String values[] = new String[2];
            values[0] = value.getVer() + "";
            values[1] = strValue;
            int ret = evalshaInteger(SHA_CAS_LUS, keys, values);
            return ret == 1;

        } catch (Exception e) {
            log.error(FConst.ERROR_REDIS_ERROR_MSG, e);
        }
        return false;
    }

    public boolean setexCas(final String key, final RedisCasDO value, long timeOut) {
        try {
            String strValue = objectMapper.writeValueAsString(value);
            String keys[] = new String[1];
            keys[0] = key;

            String values[] = new String[3];
            values[0] = value.getVer() + "";
            values[1] = strValue;
            values[2] = String.valueOf(timeOut);
            int ret = evalshaInteger(SHA_CAS_SETEX_LUA, keys, values);
            return ret == 1;
        } catch (Exception e) {
            log.error(FConst.ERROR_REDIS_ERROR_MSG, e);
        }
        return false;
    }


    public long zadd(final String key, final double score, final Object member) {
        try {
            String strMember = objectMapper.writeValueAsString(member);
            return redisCommands.zadd(key, score, strMember);
        } catch (Exception e) {
            log.error(FConst.ERROR_REDIS_ERROR_MSG, e);
        }
        return -1L;
    }


    public long zrem(final String key, Object member) {
        try {
            String strMember = objectMapper.writeValueAsString(member);
            return redisCommands.zrem(key, strMember);
        } catch (Exception e) {
            log.error(FConst.ERROR_REDIS_ERROR_MSG, e);
        }
        return -1L;
    }

    public List<ScoredValue<String>> zrangeByScoreWithScores(String key, final double min, final double max, int offset, int count) {
        Limit limit = Limit.create(offset, count);
        Range range = Range.create(min, max);
        return redisCommands.zrangebyscoreWithScores(key, range, limit);
        //return redisClusterCommands.zrangeByScoreWithScores(key, min, max, offset, count);
    }

    public Double zscore(String key, Object member) {
        try {
            String strMember = objectMapper.writeValueAsString(member);
            return redisCommands.zscore(key, strMember);
        } catch (Exception e) {
            log.error("exception in zscore", e);
            return 0d;
        }
    }

    public Long del(final String key) {
        return redisCommands.del(key);
    }

    public boolean expire(final String key, final int seconds) {
        return redisCommands.expire(key, seconds);
    }

    public String set(String key, final Object value, String nxxx, String expx, long time) {
        try {
            String strValue = objectMapper.writeValueAsString(value);
            SetArgs setArgs = SetArgs.Builder.nx();
            setArgs.px(time);
            return redisCommands.set(key, strValue, setArgs);
        } catch (Exception e) {
            log.error(FConst.ERROR_REDIS_ERROR_MSG, e);
        }
        return null;
    }

    public Object eval(String script, String[] keys, String... values) {
        return redisCommands.eval(script, ScriptOutputType.MULTI, keys, values);
        // return redisClusterCommands.eval(script, key, values);
    }

    public Object evalsha(String script, String[] keys, String... values) {
        return redisCommands.evalsha(script, ScriptOutputType.MULTI, keys, values);
    }

    public int evalshaInteger(String script, String[] keys, String... values) {
        Long ret = redisCommands.evalsha(script, ScriptOutputType.INTEGER, keys, values);
        return ret.intValue();
    }


    public String scriptLoad(String script) {
        return redisCommands.scriptLoad(script);
    }

    public Long del(final String... keys) {
        return redisCommands.del(keys);
    }

    public Map<String, String> mget(final String... keys) {
        List<KeyValue<String, String>> keyValues = redisCommands.mget(keys);
        if (CollectionUtils.isEmpty(keyValues)) return null;
        Map<String, String> keyValuesMap = new HashMap<String, String>(keyValues.size() * 4 / 3);
        for (KeyValue<String, String> keyValue : keyValues) {
            if (keyValue.hasValue()) {
                keyValuesMap.put(keyValue.getKey(), keyValue.getValue());
            }
        }
        return keyValuesMap;
    }

    public Long srem(String key, String member) {
        try {
            return redisCommands.srem(key, member);
        } catch (Exception e) {
            log.error("exception in eval", e);
        }
        return -1L;
    }

    public long incr(String key) {
        try {
            return redisCommands.incr(key);
        } catch (Exception e) {
            log.error("exception in eval", e);
        }
        return -1L;
    }

    public Long decr(String key) {
        try {
            return redisCommands.decr(key);
        } catch (Exception e) {
            log.error("exception in eval", e);
        }
        return -1L;
    }

    public Set<String> smembers(String key) {
        try {
            return redisCommands.smembers(key);
        } catch (Exception e) {
            log.error("exception in eval", e);
            return null;
        }
    }

    public long sadd(String key, String member) {
        try {
            return redisCommands.sadd(key, member);
        } catch (Exception e) {
            log.error("exception in eval", e);
        }
        return -1L;
    }


    public <T> Map<String, T> mget(Type type, final String... keys) {
        Map<String, String> strObjs = mget(keys);
        if (CollectionUtils.isEmpty(strObjs)) return null;
        Map<String, T> objList = new HashMap<>(strObjs.size() * 4 / 3);
        Iterator<Map.Entry<String, String>> keyValuesIter = strObjs.entrySet().iterator();
        try {
            while (keyValuesIter.hasNext()) {
                Map.Entry<String, String> strObj = keyValuesIter.next();
                T obj = objectMapper.readValue(strObj.getValue(), JsonUtil.createJavaType(type));
                objList.put(strObj.getKey(), obj);
            }
        } catch (Exception e) {
            log.error(FConst.ERROR_REDIS_ERROR_MSG, e);
        }
        return objList;
    }

    public <T> T hget(String htable, String key, Type type) {
        String body = redisCommands.hget(htable, key);
        if (body == null) return null;
        try {
            return objectMapper.readValue(body, JsonUtil.createJavaType(type));
        } catch (Exception e) {
            log.error(FConst.ERROR_REDIS_ERROR_MSG, e);
        }
        return null;
    }


    public boolean hexists(String htable, String key) {
        return redisCommands.hexists(htable, key);
    }

    public boolean hset(String htable, String key, Object obj) {
        try {
            String strObj = objectMapper.writeValueAsString(obj);
            return redisCommands.hset(htable, key, strObj);
        } catch (Exception e) {
            log.error(FConst.ERROR_REDIS_ERROR_MSG, e);
        }
        return false;
    }

    public boolean hset(String htable, Map<String, String> objectMap) {
        try {
            return redisCommands.hmset(htable, objectMap).equals("OK");
        } catch (Exception e) {
            log.error(FConst.ERROR_REDIS_ERROR_MSG, e);
        }
        return false;

    }

    public long hdel(String htable, String key) {
        return redisCommands.hdel(htable, key);
    }

    public boolean sismember(String key, String member) {
        return redisCommands.sismember(key, member);
    }

    public Map<String, String> hgetAll(String htable) {
        return redisCommands.hgetall(htable);
    }


    public <K, T> Map<K, T> hgetAll(String htable, Type kType, Type type) {
        Map<String, String> ret = redisCommands.hgetall(htable);
        Map<K, T> newRet = new HashMap<K, T>(ret.size() * 4 / 3);
        Iterator<Map.Entry<String, String>> entryIterator = ret.entrySet().iterator();
        try {
            while (entryIterator.hasNext()) {
                Map.Entry<String, String> entry = entryIterator.next();
                newRet.put(objectMapper.readValue(entry.getKey(), JsonUtil.createJavaType(kType)), objectMapper.readValue(entry.getValue(), JsonUtil.createJavaType(type)));
            }
        } catch (Exception e) {
            log.error(FConst.ERROR_REDIS_ERROR_MSG, e);
        }
        return newRet;
    }


    public long hinc(String htable, String key, long value) {
        return this.redisCommands.hincrby(htable, key, value);
    }
}
