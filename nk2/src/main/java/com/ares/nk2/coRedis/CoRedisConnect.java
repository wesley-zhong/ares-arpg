package com.ares.nk2.coRedis;

import io.lettuce.core.api.StatefulRedisConnection;

/**
 * Created by levoneliu on 2018/9/5.
 */
public class CoRedisConnect<K ,V> {
    private StatefulRedisConnection<K ,V> nativeConnect;

    public CoRedisConnect(StatefulRedisConnection<K ,V> nativeConnect) {
        this.nativeConnect = nativeConnect;
    }

    public static CoRedisConnect wrap(StatefulRedisConnection nativeConnect){
        return new CoRedisConnect(nativeConnect);
    }

    public CoRedisCmd<K, V> async(){
        return CoRedisCmd.wrap(nativeConnect.async());
    }
}
