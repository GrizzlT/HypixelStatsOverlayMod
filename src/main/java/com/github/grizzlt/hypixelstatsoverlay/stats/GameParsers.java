package com.github.grizzlt.hypixelstatsoverlay.stats;

import com.github.grizzlt.hypixelpublicapi.error.PublicAPIKeyMissingException;
import com.github.grizzlt.hypixelstatsoverlay.HypixelStatsOverlayMod;
import com.github.grizzlt.hypixelstatsoverlay.stats.bedwars.BedwarsParser;
import net.hypixel.api.data.type.GameType;
import net.hypixel.api.data.type.LobbyType;
import net.hypixel.api.data.type.ServerType;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This class is the main Entry points for the {@link HypixelStatsOverlayMod}
 */
public class GameParsers
{
    public static final Map<ServerType, IGameParser> PARSERS = new HashMap<>();

    private final AtomicReference<ServerType> currentGameType = new AtomicReference<>(LobbyType.MAIN);
    //private String mode = "";

    private Disposable prevRequest = null;

    public IGameParser getCurrentGameParser()
    {
        return PARSERS.get(this.currentGameType.get());
    }

    public void onPlayerChangeServerWorld()
    {
        if (this.prevRequest != null) {
            this.prevRequest.dispose();
        }

        System.out.println("Sent status request!");
        this.prevRequest = HypixelStatsOverlayMod.instance.getHypixelApiMod().handleHypixelAPIRequest(api -> Mono.fromFuture(api.getStatus(Minecraft.getMinecraft().thePlayer.getUniqueID())))
                .flatMap(statusReply -> Mono.fromRunnable(() ->{
                    System.out.println("Status request came back!");

                    ServerType serverType = statusReply.getSession().getServerType();
                    IGameParser gameParser = PARSERS.get(serverType);
                    if (gameParser != null) {
                        gameParser.onPlayerSwitchWorld(statusReply);
                    }

                    this.currentGameType.set(serverType);
                })).doOnError(PublicAPIKeyMissingException.class, e -> {
                    System.out.println("API-Key was not set!");
                    Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("§cDon't forget to set your api key! Run §6/hpapiquickstart§r§c once"));
                }).subscribe();
    }

    public static void registerGameParsers()
    {
        for (IGameParser gameParser : PARSERS.values())
        {
            gameParser.registerEvents();
        }
    }

    static {
        PARSERS.put(GameType.BEDWARS, new BedwarsParser());
    }
}
