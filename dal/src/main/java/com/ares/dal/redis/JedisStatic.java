package com.ares.dal.redis;

public class JedisStatic {
    public static String CAS_SET_LUA = " local strDataObj = redis.call('get',KEYS[1]);" +
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

    public static String CAS_SETEX_LUA = " local strDataObj = redis.call('get',KEYS[1]);" +
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
}
