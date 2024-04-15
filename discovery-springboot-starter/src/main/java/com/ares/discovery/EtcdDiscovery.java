package com.ares.discovery;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;

import com.ares.core.json.transcoder.JsonObjectMapper;
import com.ares.core.utils.JsonUtil;
import com.ares.transport.bean.ServerNodeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.Watch;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.WatchOption;
import io.etcd.jetcd.watch.WatchEvent;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
public class EtcdDiscovery {
    private final Client client;

    private final Object lock = new Object();
    @Getter
    private final HashMap<String, ServerNodeInfo> serverList = new HashMap<>();
    private final BiFunction<WatchEvent.EventType, ServerNodeInfo, Void> onNodeChangeFun;


    public EtcdDiscovery(Client client, BiFunction<WatchEvent.EventType, ServerNodeInfo, Void> onNodeChangeFun) {
        this.client = client;
        this.onNodeChangeFun = onNodeChangeFun;
    }

    public void watchService(List<String> prefixAddresses) {
        for (String prefixAddress : prefixAddresses) {
            //请求当前前缀
            CompletableFuture<GetResponse> getResponseCompletableFuture =
                    client.getKVClient().get(ByteSequence.from(prefixAddress, UTF_8),
                            GetOption.builder().isPrefix(true).build());

            try {
                //获取当前前缀下的服务并存储
                List<KeyValue> kvs = getResponseCompletableFuture.get().getKvs();
                for (KeyValue kv : kvs) {
                    ServerNodeInfo serverNodeInfo = JsonObjectMapper.parseObject(kv.getValue().toString(UTF_8), ServerNodeInfo.class);
                    setServerList(kv.getKey().toString(UTF_8), serverNodeInfo);
                }


            } catch (InterruptedException | ExecutionException e) {
                log.error("---error", e);
            }
        }
        for (String prefixAddress : prefixAddresses) {
            watcher(prefixAddress);
        }
        log.info("--------- watch services = {}", prefixAddresses);
    }

    private void watcher(String prefixAddress) {
        log.info("watching prefix:" + prefixAddress);
        WatchOption watchOpts = WatchOption.builder().isPrefix(true).build();
        //实例化一个监听对象，当监听的key发生变化时会被调用
        Watch.Listener listener = Watch.listener(watchResponse -> {
            watchResponse.getEvents().forEach(watchEvent -> {
                WatchEvent.EventType eventType = watchEvent.getEventType();
                KeyValue keyValue = watchEvent.getKeyValue();
                log.info("type=" + eventType + ",key=" + keyValue.getKey().toString(UTF_8) + ",value=" + keyValue.getValue().toString(UTF_8));

                switch (eventType) {
                    case PUT:  //修改或者新增
                        ServerNodeInfo serverNodeInfo = JsonObjectMapper.parseObject(keyValue.getValue().toString(UTF_8), ServerNodeInfo.class);
                        setServerList(keyValue.getKey().toString(UTF_8), serverNodeInfo);
                        break;
                    case DELETE: //删除
                        delServerList(keyValue.getKey().toString(UTF_8));
                        break;
                }
            });
        });

        client.getWatchClient().watch(ByteSequence.from(prefixAddress, UTF_8), watchOpts, listener);
    }

    private void setServerList(String key, ServerNodeInfo serverNodeInfo) {
        synchronized (lock) {
            serverList.put(key, serverNodeInfo);
            onNodeChangeFun.apply(WatchEvent.EventType.PUT, serverNodeInfo);
        }
    }



    private void delServerList(String key) {
        synchronized (lock) {
            ServerNodeInfo remove = serverList.remove(key);
            log.info("del key:{}", key);
            onNodeChangeFun.apply(WatchEvent.EventType.DELETE, remove);
        }
    }

    public void close() {
        client.close();
    }
}
