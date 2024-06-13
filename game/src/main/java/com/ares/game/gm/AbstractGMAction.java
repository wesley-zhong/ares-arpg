package com.ares.game.gm;

import com.ares.game.player.Player;
import com.game.protoGen.ProtoGame;

public interface AbstractGMAction {
    void doAction(Player player, ProtoGame.DebugCmdReq reqMsg, ProtoGame.DebugCmdRes.Builder response);
}
