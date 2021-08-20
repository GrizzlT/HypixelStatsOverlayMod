package com.github.grizzlt.hypixelstatsoverlay.stats;

import com.github.grizzlt.serverbasedmodlibrary.ServerBasedRegisterUtil;
import net.hypixel.api.reply.StatusReply;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.config.Configuration;

public interface IGameParser
{
    void onPlayerSwitchWorld(StatusReply statusReply);

    void onRenderGameOverlayEvent(RenderGameOverlayEvent event);

    void registerEvents(ServerBasedRegisterUtil serverBasedRegisterUtil);

    void loadConfig(Configuration config);

    void activate();

    void deActivate();
}
