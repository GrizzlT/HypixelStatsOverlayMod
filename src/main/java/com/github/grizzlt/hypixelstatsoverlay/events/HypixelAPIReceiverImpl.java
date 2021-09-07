package com.github.grizzlt.hypixelstatsoverlay.events;

import com.github.grizzlt.hypixelapimod.api.HypixelPublicAPIModApi;
import com.github.grizzlt.hypixelapimod.api.event.HypixelAPIReadyEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class HypixelAPIReceiverImpl implements HypixelAPIReadyEvent.HypixelAPIReceiver
{
    private HypixelPublicAPIModApi apiWrapper = null;

    @NotNull
    public HypixelPublicAPIModApi getAPI()
    {
        return Objects.requireNonNull(this.apiWrapper);
    }

    @Override
    public void onReceiveAPI(HypixelPublicAPIModApi apiWrapper)
    {
        this.apiWrapper = apiWrapper;
    }
}
