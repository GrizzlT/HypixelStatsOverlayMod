package com.github.grizzlt.hypixelstatsoverlay.events;

import com.github.grizzlt.hypixelpublicapi.HypixelPublicAPIModApi;
import com.github.grizzlt.hypixelpublicapi.event.OnHpPublicAPIReadyEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class HypixelAPIReceiver
{
    private static HypixelPublicAPIModApi apiWrapper = null;

    @SubscribeEvent
    public void onApiReceived(OnHpPublicAPIReadyEvent event)
    {
        apiWrapper = event.getApiManager();
    }

    @Nullable
    public HypixelPublicAPIModApi getAPI()
    {
        return Objects.requireNonNull(apiWrapper);
    }
}
