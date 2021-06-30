package com.github.grizzlt.hypixelstatsoverlay.stats.bedwars;

import com.github.grizzlt.hypixelstatsoverlay.HypixelStatsOverlayMod;
import com.github.grizzlt.hypixelstatsoverlay.KeyBindManager;
import com.github.grizzlt.hypixelstatsoverlay.events.PlayerListUpdateEvent;
import com.github.grizzlt.hypixelstatsoverlay.stats.IGameParser;
import com.github.grizzlt.hypixelstatsoverlay.stats.gui.IPlayerGameData;
import com.github.grizzlt.hypixelstatsoverlay.util.JsonHelper;
import com.github.grizzlt.hypixelstatsoverlay.util.ReflectionContainer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.hypixel.api.reply.PlayerReply;
import net.hypixel.api.reply.StatusReply;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import reactor.core.Disposable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class BedwarsParser implements IGameParser
{
    private final BedwarsGameGuiOverlay bwGameTabRenderer = new BedwarsGameGuiOverlay(this);

    private final ConcurrentMap<UUID, BedwarsProfile> playersInList = new ConcurrentHashMap<>();
    private final List<Disposable> sentRequests = new ArrayList<>();

    private boolean isInLobby = false;

    @Override
    public void onPlayerSwitchWorld(@NotNull StatusReply statusReply)
    {
        this.isInLobby = statusReply.getSession().getMode().equals("LOBBY"); //change isInLobby default to true

        this.sentRequests.forEach(Disposable::dispose);
        this.sentRequests.clear();
        this.playersInList.clear();

        System.out.println("In lobby? " + this.isInLobby);
    }

    @Override
    public void onRenderGameOverlayEvent(RenderGameOverlayEvent event)
    {
        if (!(Minecraft.getMinecraft().gameSettings.keyBindPlayerList.isKeyDown() || !KeyBindManager.TAB_KEY_BIND.isKeyDown()))
        {
            Scoreboard scoreboard = Minecraft.getMinecraft().theWorld.getScoreboard();
            ScoreObjective scoreObjective = scoreboard.getObjectiveInDisplaySlot(0);
            int width = event.resolution.getScaledWidth();

            if (this.isInLobby) {
                try {
                    ((GuiPlayerTabOverlay)ReflectionContainer.guiTabOverlayField.get(Minecraft.getMinecraft().ingameGUI)).updatePlayerList(true);
                    ((GuiPlayerTabOverlay)ReflectionContainer.guiTabOverlayField.get(Minecraft.getMinecraft().ingameGUI)).renderPlayerlist(width, scoreboard, scoreObjective);
                } catch (IllegalAccessException ex) {
                    //ex.printStackTrace();
                }
                return;
            }

            this.bwGameTabRenderer.renderPlayerList(width, scoreObjective);
        } else {
            try {
                ((GuiPlayerTabOverlay)ReflectionContainer.guiTabOverlayField.get(Minecraft.getMinecraft().ingameGUI)).updatePlayerList(false);
            } catch (IllegalAccessException e) {
                //e.printStackTrace();
            }
        }
    }

    @Override
    public void registerEvents()
    {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onPlayerListUpdate(PlayerListUpdateEvent event)
    {
        this.bwGameTabRenderer.markDirty();
    }

    public void lookupPlayer(UUID playerId)
    {
        if (!this.playersInList.containsKey(playerId))
        {
            System.out.println("Gathering stats for " + playerId.toString());
            this.playersInList.put(playerId, new BedwarsProfile(false));
            this.sentRequests.add(HypixelStatsOverlayMod.instance.getHypixelApiMod().handleHypixelAPIRequest(api -> api.getPlayerByUuid(playerId))
                    .filter(player -> player.getPlayer() != null)
                    .map(PlayerReply::getPlayer)
                    .map(json -> new BedwarsProfile(getBwLevel(json), getWinStreak(json), getFKDR(json), getWinLossRatio(json), getBBLR(json)))
                    .defaultIfEmpty(BedwarsProfile.NICKED)
                    .subscribe(profile -> this.playersInList.put(playerId, profile)));
        }
    }

    public static class BedwarsProfile implements IPlayerGameData
    {
        public static BedwarsProfile NICKED = new BedwarsProfile(true);

        public int level = -1;
        public int winstreak = -1;
        public double fkdr = -2.0;
        public double wlr = -2.0;
        public double bblr = -2.0;

        public boolean isNicked;
        public double score = 1.0;

        public BedwarsProfile(boolean nicked)
        {
            this.isNicked = nicked;
        }

        public BedwarsProfile(int level, int ws, double fkdr, double wlr, double bblr)
        {
            this.level = level;
            this.winstreak = ws;
            this.fkdr = fkdr;
            this.wlr = wlr;
            this.bblr = bblr;

            calculateScore();
        }

        public void calculateScore()
        {
            double fkdrMax = Math.max(0, fkdr);
            this.score = (10 + Math.max(level, 0)) * fkdrMax * fkdrMax * (winstreak > 1 ? winstreak * 1.625 : 1);
        }

        @Override
        public boolean isNicked()
        {
            return isNicked;
        }
    }

    public Map<UUID, BedwarsProfile> getPlayerDataMap()
    {
        return this.playersInList;
    }

    /**
     * @return BW level: -1 = unknown
     */
    private static int getBwLevel(JsonObject playerObject)
    {
        JsonElement bwLevelElement = JsonHelper.getObjectUsingPath(playerObject, "achievements.bedwars_level");
        if (bwLevelElement != null) {
            return bwLevelElement.getAsInt();
        }
        return -1;
    }

    /**
     * @return FKDR: -2 = unknown, -1 = no final deaths
     */
    private static double getFKDR(JsonObject playerObject)
    {
        JsonElement finalDeathsObj = JsonHelper.getObjectUsingPath(playerObject, "stats.Bedwars.final_deaths_bedwars");
        if (finalDeathsObj == null) {
            return -1.0;
        }
        JsonElement finalKillsObj = JsonHelper.getObjectUsingPath(playerObject, "stats.Bedwars.final_kills_bedwars");
        if (finalKillsObj == null) {
            return -2.0;
        }
        int fk = finalKillsObj.getAsInt();
        int fd = finalDeathsObj.getAsInt();
        if (fd == 0) {
            return -1.0;
        }
        return (double)fk / (double)fd;
    }

    /**
     * @return WS: -1 = unknown
     */
    private static int getWinStreak(JsonObject playerObject)
    {
        JsonElement wsElement = JsonHelper.getObjectUsingPath(playerObject, "stats.Bedwars.winstreak");
        if (wsElement == null) {
            return -1;
        }
        return wsElement.getAsInt();
    }

    /**
     * @return WLR: -2.0 = unknown, -1.0 = no losses
     */
    private static double getWinLossRatio(JsonObject playerObject)
    {
        JsonElement lossesObj = JsonHelper.getObjectUsingPath(playerObject, "stats.Bedwars.losses_bedwars");
        if (lossesObj == null) {
            return -1.0;
        }
        JsonElement winsObj = JsonHelper.getObjectUsingPath(playerObject, "stats.Bedwars.wins_bedwars");
        if (winsObj == null) {
            return -2.0;
        }
        int wins = winsObj.getAsInt();
        int losses = lossesObj.getAsInt();
        if (losses == 0) {
            return -1.0;
        }
        return (double)wins / (double)losses;
    }

    /**
     * @return BBLR: -2.0 = unknown, -1.0 = no beds lost
     */
    private static double getBBLR(JsonObject playerObject)
    {
        JsonElement lostObj = JsonHelper.getObjectUsingPath(playerObject, "stats.Bedwars.beds_lost_bedwars");
        if (lostObj == null) {
            return -1.0;
        }
        JsonElement brokenObj = JsonHelper.getObjectUsingPath(playerObject, "stats.Bedwars.beds_broken_bedwars");
        if (brokenObj == null) {
            return -2.0;
        }
        int lost = lostObj.getAsInt();
        int broken = brokenObj.getAsInt();
        if (lost == 0) {
            return -1.0;
        }
        return (double)broken / (double)lost;
    }
}
