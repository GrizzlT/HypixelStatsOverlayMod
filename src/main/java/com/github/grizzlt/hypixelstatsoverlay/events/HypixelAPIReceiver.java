package com.github.grizzlt.hypixelstatsoverlay.events;

import com.github.grizzlt.hypixelpublicapi.HypixelPublicAPIModLibrary;
import com.github.grizzlt.hypixelpublicapi.event.OnHpPublicAPIReadyEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class HypixelAPIReceiver
{
    private static HypixelPublicAPIModLibrary apiWrapper;

    @SubscribeEvent
    public void onApiReceived(OnHpPublicAPIReadyEvent event)
    {
        apiWrapper = event.publicAPILibrary;
    }

    public HypixelPublicAPIModLibrary getAPI()
    {
        return apiWrapper;
    }
}
