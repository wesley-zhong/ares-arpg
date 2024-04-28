package com.ares.gateway.service;

import com.ares.core.bean.AresPacket;
import com.ares.core.tcp.AresTKcpContext;
import com.ares.gateway.bean.PlayerSession;
import com.game.protoGen.ProtoGame;
import com.game.protoGen.ProtoInner;
import com.google.protobuf.Message;
import io.netty.buffer.ByteBuf;

public interface SessionService {
    void gameLogin(AresTKcpContext aresTKcpContext, ProtoGame.GameLoginReq loginRequest);

    void gameSrvLoginResponse(ProtoInner.InnerGameLoginResponse innerWorldLoginResponse);

    void kickOutPlayer(long uid);

    void sendPlayerMsg(long uid, int msgId, Message body);

    void sendPlayerErrMsg(PlayerSession playerSession, int msgId, int errCode);

    void sendPlayerMsg(PlayerSession playerSession, int msgId, Message body);

    void sendPlayerMsg(long uid, AresPacket aresPacket);

    void sendPlayerMsg(long uid, ByteBuf body);

    void playerDisconnect(PlayerSession playerSession);

    PlayerSession getPlayerSession(long uid);
}
