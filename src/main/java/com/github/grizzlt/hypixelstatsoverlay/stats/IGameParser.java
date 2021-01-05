package com.github.grizzlt.hypixelstatsoverlay.stats;

import com.github.grizzlt.shadowedLibs.net.hypixel.api.reply.StatusReply;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;

public interface IGameParser
{
    void onPlayerSwitchWorld(StatusReply statusReply, EntityJoinWorldEvent event);

    void onRenderGameOverlayEvent(RenderGameOverlayEvent event);

    void onChatReceived(ClientChatReceivedEvent event);
}
