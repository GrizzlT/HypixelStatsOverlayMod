package com.github.grizzlt.hypixelstatsoverlay.events;

import net.hypixel.api.reply.StatusReply;
import net.minecraftforge.fml.common.eventhandler.Event;

public class PlayerChangedServerWorldEvent extends Event
{
    private final StatusReply statusReply;

    public PlayerChangedServerWorldEvent(StatusReply statusReply)
    {
        this.statusReply = statusReply;
    }

    public StatusReply getStatusReply()
    {
        return this.statusReply;
    }

    public boolean isInLobby()
    {
        return this.statusReply.getSession().isOnline() && this.statusReply.getSession().getMode().equalsIgnoreCase("lobby");
    }
}
