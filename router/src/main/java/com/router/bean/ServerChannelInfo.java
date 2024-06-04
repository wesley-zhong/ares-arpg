package com.router.bean;


import io.netty.channel.Channel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ServerChannelInfo {
    private String serviceId;
    private Channel channel;
}
