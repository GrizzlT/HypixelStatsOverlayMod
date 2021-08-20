package com.github.grizzlt.hypixelstatsoverlay.stats;

import com.github.grizzlt.hypixelstatsoverlay.HypixelStatsOverlayMod;
import com.github.grizzlt.hypixelstatsoverlay.events.PlayerChangedServerWorldEvent;
import com.github.grizzlt.hypixelstatsoverlay.stats.bedwars.BedwarsParser;
import com.github.grizzlt.serverbasedmodlibrary.ServerBasedRegisterUtil;
import net.hypixel.api.data.type.GameType;
import net.hypixel.api.data.type.LobbyType;
import net.hypixel.api.data.type.ServerType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is the main Entry points for the {@link HypixelStatsOverlayMod}
 */
public class GameParsers
{
    public static final Map<ServerType, IGameParser> PARSERS = new HashMap<>();

    private ServerType currentGameType = LobbyType.MAIN;
    //private String mode = "";

    public IGameParser getCurrentGameParser()
    {
        return PARSERS.get(this.currentGameType);
    }

    @SubscribeEvent
    public void onPlayerChangedServerWorld(PlayerChangedServerWorldEvent event)
    {
        ServerType serverType = event.getServerType();
        IGameParser gameParser = PARSERS.get(serverType);
        if (serverType != this.currentGameType && this.getCurrentGameParser() != null) {
            this.getCurrentGameParser().deActivate();
        }
        if (gameParser != null) {
            if (serverType != this.currentGameType) {
                gameParser.activate();
            }
            gameParser.onPlayerSwitchWorld(event.getStatusReply());
        }
        this.currentGameType = serverType;
    }

    public static void registerGameParsers(ServerBasedRegisterUtil serverBasedRegisterUtil)
    {
        for (IGameParser gameParser : PARSERS.values())
        {
            gameParser.registerEvents(serverBasedRegisterUtil);
        }
    }

    static {
        PARSERS.put(GameType.BEDWARS, new BedwarsParser());
    }
}
