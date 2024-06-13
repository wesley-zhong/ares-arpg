package com.ares.game.controller;

import com.ares.core.annotation.MsgId;
import com.ares.core.exception.FyLogicException;
import com.ares.core.exception.UnknownLogicException;
import com.ares.core.service.AresController;
import com.ares.game.gm.AbstractGMAction;
import com.ares.game.player.Player;
import com.ares.game.service.PlayerRoleService;
import com.game.protoGen.ProtoErrorCode;
import com.game.protoGen.ProtoGame;
import com.game.protoGen.ProtoMsgId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Slf4j
@Component
public class DebugCmdController implements AresController {
    @Autowired
    PlayerRoleService playerRoleService;

    @MsgId(ProtoMsgId.MsgId.DEBUG_CMD_REQ_VALUE)
    public ProtoGame.DebugCmdRes useItem(long uid, ProtoGame.DebugCmdReq req) {
        ProtoGame.DebugCmdReq.Builder reqMsg = req.toBuilder();
        ProtoGame.DebugCmdRes.Builder res = ProtoGame.DebugCmdRes.newBuilder();

        //空字符串转0
        ArrayList<String> paramList = new ArrayList<>();
        if ("".equals(reqMsg.getParam1())) {
            reqMsg.setParam1("0");
            paramList.add(reqMsg.getParam1());
        }
        if ("".equals(reqMsg.getParam2())) {
            reqMsg.setParam2("0");
            paramList.add(reqMsg.getParam2());
        }
        if ("".equals(reqMsg.getParam3())) {
            reqMsg.setParam3("0");
            paramList.add(reqMsg.getParam3());
        }
        if ("".equals(reqMsg.getParam4())) {
            reqMsg.setParam4("0");
            paramList.add(reqMsg.getParam4());
        }
        if ("".equals(reqMsg.getParam5())) {
            reqMsg.setParam5("0");
            paramList.add(reqMsg.getParam5());
        }

        Player player = playerRoleService.getPlayer(uid);
        try {
            AbstractGMAction GMAction = (AbstractGMAction) (Class.forName("com.ares.game.gm." + reqMsg.getCmd()).newInstance());
            GMAction.doAction(player, reqMsg.build(), res);
        } catch (ClassNotFoundException e) {
            throw new FyLogicException(ProtoErrorCode.ErrCode.GM_ACTION_NOT_FOUND_VALUE, "GM action not found " + reqMsg.getCmd(), e);
        } catch (InstantiationException | IllegalAccessException e) {
            log.error(e.getMessage(), e);
            throw new UnknownLogicException("GM do action " + reqMsg.getCmd() + " exception", e);
        }
        res.setCmd(reqMsg.getCmd());
        return res.build();
    }
}
