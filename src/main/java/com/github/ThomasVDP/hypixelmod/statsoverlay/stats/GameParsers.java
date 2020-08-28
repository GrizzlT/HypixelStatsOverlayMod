package com.github.ThomasVDP.hypixelmod.statsoverlay.stats;

import com.github.ThomasVDP.hypixelmod.statsoverlay.HypixelStatsOverlayMod;
import com.github.ThomasVDP.hypixelmod.statsoverlay.stats.parser.bedwars.BedwarsParser;
import com.github.ThomasVDP.hypixelpublicapi.error.PublicAPIKeyMissingException;
import com.github.ThomasVDP.shadowedLibs.net.hypixel.api.util.GameType;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class is the main Entry points for the {@link HypixelStatsOverlayMod}
 */
public class GameParsers
{
    /**
     * The main collection of {@link IGameParser}
     */
    private static final Map<GameType, IGameParser> PARSERS = new HashMap<>();

    /**
     * The current {@link GameType}
     */
    private GameType currentGameType;
    private String mode = "";
    /**
     * A boolean declaring we are waiting/handling a status request
     */
    private final AtomicBoolean isJoiningWorld = new AtomicBoolean(false);

    /**
     * Initialize all the supported {@link GameType}
     */
    public GameParsers()
    {
        PARSERS.put(GameType.BEDWARS, new BedwarsParser());
    }

    /**
     * Get the {@link IGameParser} that belongs to the current {@link GameType}
     *
     * @return the current {@link IGameParser} or {@code null} when we don't support a {@link GameType} or when we don't know what game we're playing yet
     */
    public IGameParser getCurrentGameParser()
    {
        if (this.isJoiningWorld.get()) return null;

        return PARSERS.get(this.currentGameType);
    }

    /**
     * Detects when the player changes worlds (i.e. when they switch lobby)
     * and passes the event to the appropriate GameParser
     * Useful for removing cache
     *
     * @param event
     */
    @SubscribeEvent
    public void onPlayerJoinWorld(EntityJoinWorldEvent event)
    {
        //only act if a player joined
        if (!(event.entity instanceof EntityPlayer)) return;
        //only act if WE joined the world
        EntityPlayer player = (EntityPlayer)event.entity;
        if (!player.getUniqueID().equals(Minecraft.getMinecraft().thePlayer.getUniqueID())) return;

        if (this.isJoiningWorld.get()) return;

        this.isJoiningWorld.set(true);
        try {
            System.out.println("Sent status request!");
            HypixelStatsOverlayMod.apiContainer.getAPI().handleHypixelAPIRequest(api ->
                api.getStatus(player.getUniqueID())).whenComplete((statusReply, throwable) -> {
                    if (onThrowableResult(throwable)) return;

                    //if (this.currentGameType == statusReply.getSession().getGameType() && this.mode.equals(statusReply.getSession().getMode()))
                    //    return;

                    if (PARSERS.get(statusReply.getSession().getGameType()) != null) {
                        PARSERS.get(statusReply.getSession().getGameType()).onPlayerSwitchWorld(statusReply, event);
                    }
                    this.currentGameType = statusReply.getSession().getGameType();
                    this.isJoiningWorld.set(false);
                });
        } catch (PublicAPIKeyMissingException e) {
            //e.printStackTrace();
            System.out.println("Don't forget to set your public key!");
            this.isJoiningWorld.set(false);
        }
    }

    @SubscribeEvent
    public void onChatReceived(ClientChatReceivedEvent event)
    {
        if (this.getCurrentGameParser() == null) return;

        this.getCurrentGameParser().onChatReceived(event);
    }

    public static boolean onThrowableResult(Throwable throwable)
    {
        if (throwable != null) {
            throwable.printStackTrace();
        }
        return throwable != null;
    }
}
