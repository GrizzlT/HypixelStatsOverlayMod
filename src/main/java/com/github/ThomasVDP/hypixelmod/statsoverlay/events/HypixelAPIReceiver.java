package com.github.ThomasVDP.hypixelmod.statsoverlay.events;

import com.github.ThomasVDP.hypixelpublicapi.HypixelPublicAPIModLibrary;
import com.github.ThomasVDP.hypixelpublicapi.event.OnHpPublicAPIReadyEvent;
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
