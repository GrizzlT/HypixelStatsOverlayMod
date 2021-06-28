package com.github.grizzlt.hypixelstatsoverlay;

import com.github.grizzlt.hypixelpublicapi.HypixelPublicAPIModApi;
import com.github.grizzlt.hypixelstatsoverlay.commands.PartyInspectCommand;
import com.github.grizzlt.hypixelstatsoverlay.commands.PartyResetCommand;
import com.github.grizzlt.hypixelstatsoverlay.events.HypixelAPIReceiver;
import com.github.grizzlt.hypixelstatsoverlay.events.RenderOverlayEventHandler;
import com.github.grizzlt.hypixelstatsoverlay.stats.GameParsers;
import com.github.grizzlt.hypixelstatsoverlay.util.PartyManager;
import com.github.grizzlt.serverbasedmodlibrary.ServerBasedRegisterUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.event.ClickEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, clientSideOnly = true, updateJSON = "https://github.com/GrizzlT/HypixelStatsOverlayMod/raw/master/updater/updater.json")
public class HypixelStatsOverlayMod
{
    private final HypixelAPIReceiver apiContainer = new HypixelAPIReceiver();
    private final GameParsers gameParsers = new GameParsers();
    private final PartyManager partyManager = new PartyManager();

    private final ServerBasedRegisterUtil serverBasedRegisterUtil = new ServerBasedRegisterUtil(address -> address.getHostName().contains("hypixel.net"));

    private boolean isConnectingToServer = false;

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
        MinecraftForge.EVENT_BUS.register(this);

        KeyBindManager.init();
    }

    @SubscribeEvent
    public void onJoinServer(FMLNetworkEvent.ClientConnectedToServerEvent event)
    {
        isConnectingToServer = true;
    }

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (!(event.entity instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer)event.entity;
        if (!player.getUniqueID().equals(Minecraft.getMinecraft().thePlayer.getUniqueID())) return;
        if (!isConnectingToServer) return;
        isConnectingToServer = false;

        ModContainer modContainer = Loader.instance().getIndexedModList().get(Reference.MOD_ID);
        ForgeVersion.CheckResult result = ForgeVersion.getResult(modContainer);
        if (result.status == ForgeVersion.Status.FAILED) {
            System.out.println("Update checker failed to verify!");
        } else if (result.status == ForgeVersion.Status.UP_TO_DATE) {
            System.out.println("Mod is up do date!");
        } else if (result.status == ForgeVersion.Status.OUTDATED) {
            System.out.println("New mod is available!");
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("A ")
                    .appendSibling(new ChatComponentText("new version")
                            .setChatStyle(new ChatStyle()
                                    .setColor(EnumChatFormatting.BLUE)
                                    .setUnderlined(true).setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, result.url))))
                    .appendSibling(new ChatComponentText(" of the HypixelStatsOverlayMod is available!")));
        }
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
