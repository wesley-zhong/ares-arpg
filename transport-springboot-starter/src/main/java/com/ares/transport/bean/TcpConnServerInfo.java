package com.ares.transport.bean;

import io.netty.channel.Channel;
import lombok.Getter;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
public class TcpConnServerInfo {
    private List<Channel> channels = new CopyOnWriteArrayList<>();
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

    public  void addTcpConn(Channel channel) {
        channels.add(channel);
    }

    public  void delTcpConn(Channel delChannel) {
        channels.removeIf((channel -> channel.id().asShortText().equals(delChannel.id().asShortText())));
    }

    public  Channel roubinChannel() {
        selectIndex++;
        if (selectIndex == channels.size()) {
            selectIndex = 0;
        }
        return channels.get(selectIndex);
    }

    public  void close() {
        for (Channel channel : channels) {
            channel.close();
        }
    }

    public int checkLostConnect() {
        for (int i = 0; i < channels.size(); ++i) {
            Channel channel = channels.get(i);
            if (channel.isActive()) {
                continue;
            }
            channels.remove(i);
            i--;
        }
        return maxCount - channels.size();
    }
}

