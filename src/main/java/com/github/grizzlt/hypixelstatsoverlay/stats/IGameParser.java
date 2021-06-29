package com.github.grizzlt.hypixelstatsoverlay.stats;

import net.hypixel.api.reply.StatusReply;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;

public interface IGameParser
{
    void onPlayerSwitchWorld(StatusReply statusReply);

    void onRenderGameOverlayEvent(RenderGameOverlayEvent event);

    void registerEvents();
}
