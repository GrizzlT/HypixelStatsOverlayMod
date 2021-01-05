package com.github.grizzlt.hypixelstatsoverlay;

import com.github.grizzlt.hypixelstatsoverlay.commands.PartyInspectCommand;
import com.github.grizzlt.hypixelstatsoverlay.commands.PartyResetCommand;
import com.github.grizzlt.hypixelstatsoverlay.events.HypixelAPIReceiver;
import com.github.grizzlt.hypixelstatsoverlay.events.RenderOverlayEventHandler;
import com.github.grizzlt.hypixelstatsoverlay.stats.GameParsers;
import com.github.grizzlt.hypixelstatsoverlay.util.PartyManager;
import com.github.grizzlt.serverbasedmodlibrary.ServerBasedRegisterUtil;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

/**
 * The Main-Class of this little addon/mod
 * that will be used as an entry point when the ServerModsFoundation loads all its addons
 */
@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.VERSION)
public class HypixelStatsOverlayMod
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
     * the ServerBasedRegisterUtil instance;
     */
    private ServerBasedRegisterUtil serverBasedRegisterUtil = new ServerBasedRegisterUtil(
            address -> address.getHostName().contains("hypixel.net")
    );

    @Mod.Instance
    public static HypixelStatsOverlayMod instance;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        this.serverBasedRegisterUtil.registerCommand(new PartyInspectCommand());
        this.serverBasedRegisterUtil.registerCommand(new PartyResetCommand());
    }

    @Mod.EventHandler
    public void Init(FMLInitializationEvent event)
    {
        this.serverBasedRegisterUtil.Init();

        this.serverBasedRegisterUtil.registerToEventBus(new RenderOverlayEventHandler());
        this.serverBasedRegisterUtil.registerToEventBus(gameParsers);
        this.serverBasedRegisterUtil.registerToEventBus(partyManager);

        MinecraftForge.EVENT_BUS.register(apiContainer);

        KeyBindManager.init();
    }
}
