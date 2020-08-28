package com.github.ThomasVDP.hypixelmod.statsoverlay;

import com.github.ThomasVDP.hypixelmod.statsoverlay.events.HypixelAPIReceiver;
import com.github.ThomasVDP.hypixelmod.statsoverlay.events.RenderOverlayEventHandler;
import com.github.ThomasVDP.hypixelmod.statsoverlay.stats.GameParsers;
import com.github.ThomasVDP.hypixelmod.statsoverlay.util.PartyManager;
import com.github.ThomasVDP.servermodsfoundation.library.ServerModBase;
import com.github.ThomasVDP.servermodsfoundation.library.command.ICommandRegister;
import com.github.ThomasVDP.servermodsfoundation.library.events.IEventSubscribeRegister;
import net.minecraftforge.common.MinecraftForge;

/**
 * The Main-Class of this little addon/mod
 * that will be used as an entry point when the ServerModsFoundation loads all its addons
 */
public class HypixelStatsOverlayMod implements ServerModBase
{
    /**
     * The {@link HypixelAPIReceiver} that will hold a reference to the apiManager instance
     */
    public static HypixelAPIReceiver apiContainer = new HypixelAPIReceiver();
    /**
     * The {@link GameParsers} instance
     */
    public static GameParsers gameParsers = new GameParsers();
    /**
     * The {@link PartyManager} instance
     */
    public static PartyManager partyManager = new PartyManager();

    /**
     * Register all the EventListeners
     * We need a {@link RenderOverlayEventHandler}, a {@link GameParsers} to run only on mc.hypixel.net
     * We need the {@link HypixelAPIReceiver} apiContainer to be able to receive the apiManager after all the mods are loaded
     *
     * @param subscribeRegister
     */
    @Override
    public void onRegisterEventSubscribers(IEventSubscribeRegister subscribeRegister)
    {
        subscribeRegister.registerSubscriber(new RenderOverlayEventHandler());
        subscribeRegister.registerSubscriber(gameParsers);
        subscribeRegister.registerSubscriber(partyManager);

        //only do this because of the api
        MinecraftForge.EVENT_BUS.register(apiContainer);

        KeyBindManager.init();
    }

    /**
     * Register all the Commands (client-side only)
     * Currently doing nothing
     *
     * @param commandRegister
     */
    @Override
    public void onRegisterCommands(ICommandRegister commandRegister)
    {
        
    }
}
