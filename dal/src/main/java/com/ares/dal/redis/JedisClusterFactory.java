package com.ares.dal.redis;


import com.ares.core.constdata.FConst;
import com.ares.core.json.transcoder.JsonObjectMapper;
import com.ares.core.utils.JsonUtil;
import com.ares.core.utils.StringUtils;
import com.ares.dal.exception.RedisException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.*;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.cluster.api.sync.RedisClusterCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Type;
import java.util.*;


/**
 * jedisCluseter操作封装类
 * zhongwq 20190129
 */
public class JedisClusterFactory {
    private static ObjectMapper objectMapper = JsonObjectMapper.getInstance();
    private static Logger logger = LoggerFactory.getLogger(JedisClusterFactory.class);
    private static String CAS_SET_LUA = " local strDataObj = redis.call('get',KEYS[1]);" +
            " if(strDataObj ==  false) then" +
            "    redis.call('set',KEYS[1],ARGV[2])" +
            "  return 1 " +
            " end" +
            " local  dataObj = cjson.decode(strDataObj);" +
            " if(dataObj.ver ==  (ARGV[1] -1)) then" +
            "  redis.call('set',KEYS[1],ARGV[2])" +
            "  return 1" +
            " else" +
            "   return 0 " +
            " end";
    private static String CAS_SETEX_LUA = " local strDataObj = redis.call('get',KEYS[1]);" +
            " if(strDataObj ==  false) then" +
            "    redis.call('set',KEYS[1],ARGV[2])" +
            "  return 1 " +
            " end" +
            " local  dataObj = cjson.decode(strDataObj);" +
            " if(dataObj.ver ==  (ARGV[1] -1)) then" +
            "  redis.call('setex',KEYS[1],ARGV[3], ARGV[2])" +
            "  return 1" +
            " else" +
            "   return 0 " +
            " end";
    private String redisClusterIp;
    private String redisClusterPassword;
    private RedisClusterCommands<String, String> redisClusterCommands;
    private String SHA_CAS_LUS;
    private String SHA_CAS_SETEX_LUA;

    public void initPool() {
        logger.info("####### redis cluster ip: {}", redisClusterIp);
        List<String> confList = Arrays.asList(this.redisClusterIp.split("(?:\\s|,)+"));
        List<RedisURI> redisURIS = new ArrayList<RedisURI>(confList.size());
        for (String address : confList) {
            String[] addressArr = address.split(":");
            RedisURI node = RedisURI.create(addressArr[0], Integer.parseInt(addressArr[1]));
            node.setPassword(redisClusterPassword);
            redisURIS.add(node);
        }
        RedisClusterClient redisCluster = RedisClusterClient.create(redisURIS);
        StatefulRedisClusterConnection<String, String> connection = redisCluster.connect();
        redisClusterCommands = connection.sync();
        SHA_CAS_LUS = scriptLoad(CAS_SET_LUA);
        SHA_CAS_SETEX_LUA = scriptLoad(CAS_SETEX_LUA);
    }

    public String set(final String key, final Object value) {
        try {
            String body = objectMapper.writeValueAsString(value);
            return redisClusterCommands.set(key, body);
        } catch (Exception e) {
            throw new RedisException(e.getMessage());
        }
    }

    public <T> T get(final String key, Class<T> objClass) {
        try {
            String keyBody = redisClusterCommands.get(key);
            if (StringUtils.isNullOrEmpty(keyBody))
                return null;
            return objectMapper.readValue(keyBody, objClass);
        } catch (Exception e) {
            logger.error(FConst.ERROR_REDIS_ERROR_MSG, e);
        }
        return null;
    }

    public String get(final String key) {
        return redisClusterCommands.get(key);
    }

    public <T> List<T> getList(final String key, Type type) {
        try {
            String keyBody = redisClusterCommands.get(key);
            if (StringUtils.isNullOrEmpty(keyBody))
                return null;
            JavaType javaType = objectMapper.getTypeFactory().constructParametricType(List.class, (Class<?>) type);
            return (List<T>) objectMapper.readValue(keyBody, javaType);
        } catch (Exception e) {
            logger.error(FConst.ERROR_REDIS_ERROR_MSG, e);
        }
        return null;
    }


    public boolean setnx(final String key, final Object value) {
        try {
            String strValue = objectMapper.writeValueAsString(value);
            return redisClusterCommands.setnx(key, strValue);
        } catch (Exception e) {
            logger.error(FConst.ERROR_REDIS_ERROR_MSG, e);
        }
        return false;
    }

    public String setex(final String key, final int seconds, final Object value) {
        try {
            String strValue = objectMapper.writeValueAsString(value);
            return redisClusterCommands.setex(key, seconds, strValue);
        } catch (Exception e) {
            logger.error(FConst.ERROR_REDIS_ERROR_MSG, e);
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
            logger.error(FConst.ERROR_REDIS_ERROR_MSG, e);
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
            logger.error(FConst.ERROR_REDIS_ERROR_MSG, e);
        }
        return false;
    }


    public long zadd(final String key, final double score, final Object member) {
        try {
            String strMember = objectMapper.writeValueAsString(member);
            return redisClusterCommands.zadd(key, score, strMember);
        } catch (Exception e) {
            logger.error(FConst.ERROR_REDIS_ERROR_MSG, e);
        }
        return -1L;
    }


    public long zrem(final String key, Object member) {
        try {
            String strMember = objectMapper.writeValueAsString(member);
            return redisClusterCommands.zrem(key, strMember);
        } catch (Exception e) {
            logger.error(FConst.ERROR_REDIS_ERROR_MSG, e);
        }
        return -1L;
    }

    public List<ScoredValue<String>> zrangeByScoreWithScores(String key, final double min, final double max, int offset, int count) {
        Limit limit = Limit.create(offset, count);
        Range range = Range.create(min, max);
        return redisClusterCommands.zrangebyscoreWithScores(key, range, limit);
        //return redisClusterCommands.zrangeByScoreWithScores(key, min, max, offset, count);
    }

    public Double zscore(String key, Object member) {
        try {
            String strMember = objectMapper.writeValueAsString(member);
            return redisClusterCommands.zscore(key, strMember);
        } catch (Exception e) {
            logger.error("exception in zscore", e);
            return 0d;
        }
    }

    public Long del(final String key) {
        return redisClusterCommands.del(key);
    }

    public boolean expire(final String key, final int seconds) {
        return redisClusterCommands.expire(key, seconds);
    }

    public String set(String key, final Object value, String nxxx, String expx, long time) {
        try {
            String strValue = objectMapper.writeValueAsString(value);
            SetArgs setArgs = SetArgs.Builder.nx();
            setArgs.px(time);
            return redisClusterCommands.set(key, strValue, setArgs);
        } catch (Exception e) {
            logger.error(FConst.ERROR_REDIS_ERROR_MSG, e);
        }
        return null;
    }

    public Object eval(String script, String[] keys, String... values) {
        return redisClusterCommands.eval(script, ScriptOutputType.MULTI, keys, values);
        // return redisClusterCommands.eval(script, key, values);
    }

    public Object evalsha(String script, String[] keys, String... values) {
        return redisClusterCommands.evalsha(script, ScriptOutputType.MULTI, keys, values);
    }

    public int evalshaInteger(String script, String[] keys, String... values) {
        Long ret = redisClusterCommands.evalsha(script, ScriptOutputType.INTEGER, keys, values);
        return ret.intValue();
    }


    public String scriptLoad(String script) {
        return redisClusterCommands.scriptLoad(script);
    }

    public Long del(final String... keys) {
        return redisClusterCommands.del(keys);
    }

    public Map<String, String> mget(final String... keys) {
        List<KeyValue<String, String>> keyValues = redisClusterCommands.mget(keys);
        if (CollectionUtils.isEmpty(keyValues))
            return null;
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
            return redisClusterCommands.srem(key, member);
        } catch (Exception e) {
            logger.error("exception in eval", e);
        }
        return -1L;
    }

    public long incr(String key) {
        try {
            return redisClusterCommands.incr(key);
        } catch (Exception e) {
            logger.error("exception in eval", e);
        }
        return -1L;
    }

    public Long decr(String key) {
        try {
            return redisClusterCommands.decr(key);
        } catch (Exception e) {
            logger.error("exception in eval", e);
        }
        return -1L;
    }

    public Set<String> smembers(String key) {
        try {
            return redisClusterCommands.smembers(key);
        } catch (Exception e) {
            logger.error("exception in eval", e);
            return null;
        }
    }

    public long sadd(String key, String member) {
        try {
            return redisClusterCommands.sadd(key, member);
        } catch (Exception e) {
            logger.error("exception in eval", e);
        }
        return -1L;
    }


    public <T> Map<String, T> mget(Type type, final String... keys) {
        Map<String, String> strObjs = mget(keys);
        if (CollectionUtils.isEmpty(strObjs))
            return null;
        Map<String, T> objList = new HashMap<>(strObjs.size() * 4 / 3);
        Iterator<Map.Entry<String, String>> keyValuesIter = strObjs.entrySet().iterator();
        try {
            while (keyValuesIter.hasNext()) {
                Map.Entry<String, String> strObj = keyValuesIter.next();
                T obj = objectMapper.readValue(strObj.getValue(), JsonUtil.createJavaType(type));
                objList.put(strObj.getKey(), obj);
            }
        } catch (Exception e) {
            logger.error(FConst.ERROR_REDIS_ERROR_MSG, e);
        }
        return objList;
    }

    public <T> T hget(String htable, String key, Type type) {
        String body = redisClusterCommands.hget(htable, key);
        if (body == null)
            return null;
        try {
            return objectMapper.readValue(body, JsonUtil.createJavaType(type));
        } catch (Exception e) {
            logger.error(FConst.ERROR_REDIS_ERROR_MSG, e);
        }
        return null;
    }


    public boolean hexists(String htable, String key) {
        return redisClusterCommands.hexists(htable, key);
    }

    public boolean hset(String htable, String key, Object obj) {
        try {
            String strObj = objectMapper.writeValueAsString(obj);
            return redisClusterCommands.hset(htable, key, strObj);
        } catch (Exception e) {
            logger.error(FConst.ERROR_REDIS_ERROR_MSG, e);
        }
        return false;
    }

    public boolean hset(String htable, Map<String, String> objectMap) {
        try {
            return redisClusterCommands.hmset(htable, objectMap).equals("OK");
        } catch (Exception e) {
            logger.error(FConst.ERROR_REDIS_ERROR_MSG, e);
        }
        return false;

    }

    public long hdel(String htable, String key) {
        return redisClusterCommands.hdel(htable, key);
    }

    public boolean sismember(String key, String member) {
        return redisClusterCommands.sismember(key, member);
    }

    public Map<String, String> hgetAll(String htable) {
        return redisClusterCommands.hgetall(htable);
    }


    public <K, T> Map<K, T> hgetAll(String htable, Type kType, Type type) {
        Map<String, String> ret = redisClusterCommands.hgetall(htable);
        Map<K, T> newRet = new HashMap<K, T>(ret.size() * 4 / 3);
        Iterator<Map.Entry<String, String>> entryIterator = ret.entrySet().iterator();
        try {
            while (entryIterator.hasNext()) {
                Map.Entry<String, String> entry = entryIterator.next();
                newRet.put(objectMapper.readValue(entry.getKey(), JsonUtil.createJavaType(kType)), objectMapper.readValue(entry.getValue(), JsonUtil.createJavaType(type)));
            }
        } catch (Exception e) {
            logger.error(FConst.ERROR_REDIS_ERROR_MSG, e);
        }
        return newRet;
    }


    public String getRedisClusterIp() {
        return redisClusterIp;
    }

    public void setRedisClusterIp(String redisClusterIp) {
        this.redisClusterIp = redisClusterIp;
    }

    public String getRedisClusterPassword() {
        return redisClusterPassword;
    }

    public void setRedisClusterPassword(String redisClusterPassword) {
        this.redisClusterPassword = redisClusterPassword;
    }

    public long hinc(String htable, String key, long value) {
        return this.redisClusterCommands.hincrby(htable, key, value);
    }
}
