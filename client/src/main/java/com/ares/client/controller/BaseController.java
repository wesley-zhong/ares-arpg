package com.ares.client.controller;

import com.ares.client.bean.ClientPlayer;
import com.ares.core.service.AresController;
import com.game.protoGen.ProtoGame;

public interface BaseController extends AresController {
    void onPlayerLoginFinished(ClientPlayer clientPlayer, ProtoGame.GameLoginNtf response);
}
