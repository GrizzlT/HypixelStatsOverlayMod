package com.github.grizzlt.hypixelstatsoverlay;

import com.github.grizzlt.hypixelpublicapi.HypixelPublicAPIModApi;
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
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME)
public class HypixelStatsOverlayMod
{
    private final HypixelAPIReceiver apiContainer = new HypixelAPIReceiver();
    private final GameParsers gameParsers = new GameParsers();
    private final PartyManager partyManager = new PartyManager();

    private final ServerBasedRegisterUtil serverBasedRegisterUtil = new ServerBasedRegisterUtil(address -> address.getHostName().contains("hypixel.net"));

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

        GameParsers.registerGameParsers();

        MinecraftForge.EVENT_BUS.register(apiContainer);

        KeyBindManager.init();
    }

    @NotNull
    public HypixelPublicAPIModApi getHypixelApiMod()
    {
        return Objects.requireNonNull(this.apiContainer.getAPI());
    }

    @NotNull
    public GameParsers getGameParsers()
    {
        return this.gameParsers;
    }

    @NotNull
    public PartyManager getPartyManager()
    {
        return this.partyManager;
    }
}
