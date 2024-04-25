package com.ares.dal.redis;

import com.ares.core.constdata.FConst;
import com.ares.core.utils.JsonUtil;
import com.ares.core.utils.StringUtils;
import com.ares.dal.exception.RedisException;
import com.ares.nk2.coRedis.CoRedisCmd;
import com.ares.nk2.coRedis.CoRedisConnect;
import com.fasterxml.jackson.databind.JavaType;
import io.lettuce.core.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Type;
import java.util.*;

@Slf4j
public class CoRedisFactory extends RedisFactory {
    private CoRedisCmd<String, String> redisCommands;

    public void initPool(String redisIp) {
        log.info("####### redis  ip: {}", redisIp);
        RedisClient redisClient = RedisClient.create(redisIp);
        redisCommands = CoRedisConnect.wrap(redisClient.connect()).async();
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
        try {
            return redisCommands.get(key);
        }
        catch (Exception e) {
            log.error(FConst.ERROR_REDIS_ERROR_MSG, e);
        }
        return null;
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
        try {
            return redisCommands.zrangebyscoreWithScores(key, range, limit);
        }
        catch (Exception e) {
            log.error(FConst.ERROR_REDIS_ERROR_MSG, e);
        }
        return null;
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
        try {
            return redisCommands.del(key);
        }
        catch (Exception e) {
            log.error(FConst.ERROR_REDIS_ERROR_MSG, e);
        }
        return -1L;
    }

    public boolean expire(final String key, final int seconds) {
        try {
            return redisCommands.expire(key, seconds);
        }
        catch (Exception e) {
            log.error(FConst.ERROR_REDIS_ERROR_MSG, e);
        }
        return false;
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
        try {
            return redisCommands.eval(script, ScriptOutputType.MULTI, keys, values);
        }
        catch (Exception e) {
            log.error(FConst.ERROR_REDIS_ERROR_MSG, e);
        }
        return null;
    }

    public Object evalsha(String script, String[] keys, String... values) {
        try {
            return redisCommands.evalsha(script, ScriptOutputType.MULTI, keys, values);
        }
        catch (Exception e) {
            log.error(FConst.ERROR_REDIS_ERROR_MSG, e);
        }
        return null;
    }

    public int evalshaInteger(String script, String[] keys, String... values) {
        try {
            Long ret = redisCommands.evalsha(script, ScriptOutputType.INTEGER, keys, values);
            return ret.intValue();
        }
        catch (Exception e) {
            log.error(FConst.ERROR_REDIS_ERROR_MSG, e);
        }
        return 0;
    }

    public String scriptLoad(String script) {
        try {
            return redisCommands.scriptLoad(script);
        }
        catch (Exception e) {
            log.error(FConst.ERROR_REDIS_ERROR_MSG, e);
        }
        return null;
    }

    public Long del(final String... keys) {
        try {
            return redisCommands.del(keys);
        }
        catch (Exception e) {
            log.error(FConst.ERROR_REDIS_ERROR_MSG, e);
        }
        return -1L;
    }

    public Map<String, String> mget(final String... keys) {
        try {
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
        catch (Exception e) {
            log.error(FConst.ERROR_REDIS_ERROR_MSG, e);
        }
        return null;
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
        try {
            String body = redisCommands.hget(htable, key);
            if (body == null) return null;
            return objectMapper.readValue(body, JsonUtil.createJavaType(type));
        } catch (Exception e) {
            log.error(FConst.ERROR_REDIS_ERROR_MSG, e);
        }
        return null;
    }


    public boolean hexists(String htable, String key) {
        try {
            return redisCommands.hexists(htable, key);
        }
        catch (Exception e) {
            log.error(FConst.ERROR_REDIS_ERROR_MSG, e);
        }
        return false;
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
        try {
            return redisCommands.hdel(htable, key);
        }
        catch (Exception e) {
            log.error(FConst.ERROR_REDIS_ERROR_MSG, e);
        }
        return -1L;
    }

    public boolean sismember(String key, String member) {
        try {
            return redisCommands.sismember(key, member);
        }
        catch (Exception e) {
            log.error(FConst.ERROR_REDIS_ERROR_MSG, e);
        }
        return false;
    }

    public Map<String, String> hgetAll(String htable) {
        try {
            return redisCommands.hgetall(htable);
        }
        catch (Exception e) {
            log.error(FConst.ERROR_REDIS_ERROR_MSG, e);
        }
        return null;
    }

    public <K, T> Map<K, T> hgetAll(String htable, Type kType, Type type) {
        Map<K, T> newRet = null;
        try {
            Map<String, String> ret = redisCommands.hgetall(htable);
            newRet = new HashMap<K, T>(ret.size() * 4 / 3);
            Iterator<Map.Entry<String, String>> entryIterator = ret.entrySet().iterator();
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
        try {
            return this.redisCommands.hincrby(htable, key, value);
        }
        catch (Exception e) {
            log.error(FConst.ERROR_REDIS_ERROR_MSG, e);
        }
        return -1L;
    }
}
