package com.ares.transport.bean;

import io.netty.channel.Channel;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Getter
public class TcpConnServerInfo {
    private List<Channel> channels = new ArrayList<>();
    private ServerNodeInfo serverNodeInfo;
    private int selectIndex;
    int maxCount = 0;

    public TcpConnServerInfo(ServerNodeInfo serverNodeInfo, int maxCount) {
        this.serverNodeInfo = serverNodeInfo;
        this.maxCount = maxCount;
        selectIndex = 0;
    }

    public TcpConnServerInfo() {
    }

    public synchronized void addTcpConn(Channel channel) {
        channels.add(channel);
    }

    public synchronized void delTcpConn(Channel delChannel) {
        channels.removeIf((channel -> channel.id().asShortText().equals(delChannel.id().asShortText())));
    }

    public synchronized Channel roubinChannel() {
        int select = selectIndex + 1;
        if (select >= channels.size()) {
            select = 0;
        }
        selectIndex = select;
        return channels.get(select);
    }

    public synchronized Channel hashChannel(long hashCode) {
        int index = (int) (hashCode % channels.size());
        return channels.get(index);
    }

    public synchronized void close() {
        for (Channel channel : channels) {
            channel.close();
        }
    }

    public synchronized int checkLostConnect() {
        Iterator<Channel> iterator = channels.iterator();
        while (iterator.hasNext()) {
            Channel channel = iterator.next();
            if (channel.isActive()) {
                continue;
            }
            iterator.remove();
        }
        return maxCount - channels.size();
    }
}

