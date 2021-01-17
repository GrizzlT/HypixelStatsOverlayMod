package com.github.grizzlt.hypixelstatsoverlay.stats.parser.bedwars;

import com.github.grizzlt.hypixelstatsoverlay.HypixelStatsOverlayMod;
import com.github.grizzlt.hypixelstatsoverlay.KeyBindManager;
import com.github.grizzlt.hypixelstatsoverlay.stats.IGameParser;
import com.github.grizzlt.hypixelstatsoverlay.stats.parser.RequestWrapper;
import com.github.grizzlt.hypixelstatsoverlay.util.ReflectionContainer;
import com.github.grizzlt.shadowedLibs.net.hypixel.api.reply.PlayerReply;
import com.github.grizzlt.shadowedLibs.net.hypixel.api.reply.StatusReply;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.Tuple;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BedwarsParser implements IGameParser
{
    private final BedwarsGameGuiOverlay bwGameTabRenderer = new BedwarsGameGuiOverlay(this);
    //private final BedwarsLobbyGuiOverlay bwLobbyTabRenderer = new BedwarsLobbyGuiOverlay(this);

    /**
     * main storage of the playerdata
     */
    private final Map<UUID, Tuple<RequestWrapper, BedwarsProfile>> playersInList = new HashMap<>();

    private boolean isInLobby = false;

    @Override
    public void onPlayerSwitchWorld(StatusReply statusReply, EntityJoinWorldEvent event)
    {
        this.isInLobby = statusReply.getSession().getMode().equals("LOBBY"); //change isInLobby default to true
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

            this.bwGameTabRenderer.renderPlayerList(width, scoreboard, scoreObjective);
        } else {
            try {
                ((GuiPlayerTabOverlay)ReflectionContainer.guiTabOverlayField.get(Minecraft.getMinecraft().ingameGUI)).updatePlayerList(false);
            } catch (IllegalAccessException e) {
                //e.printStackTrace();
            }
        }
    }

    @Override
    public void onChatReceived(ClientChatReceivedEvent event)
    {

    }

    public void gatherPlayers(List<UUID> uuids)
    {
        for (UUID id : uuids)
        {
            if (!this.playersInList.containsKey(id))
            {
                BedwarsProfile profile = new BedwarsProfile();
                RequestWrapper requestWrapper = new RequestWrapper(HypixelStatsOverlayMod.apiContainer.getAPI().handleHypixelAPIRequest(api ->
                        api.getPlayerByUuid(id)
                ), wrapper -> {
                    JsonObject playerObject = ((PlayerReply)wrapper.getReply()).getPlayer();
                    profile.level = getBwLevel(playerObject);
                    profile.winstreak = getWinStreak(playerObject);
                    profile.wlr = getWinLossRatio(playerObject);
                    profile.fkdr = getFKDR(playerObject);
                    profile.bblr = getBBLR(playerObject);
                    profile.calculateScore();
                });
                this.playersInList.put(id, new Tuple<>(requestWrapper, profile));
            }
        }
    }

    public static class BedwarsProfile
    {
        public int level = -1;
        public int winstreak = -1;
        public double fkdr = -2;
        public double wlr = -2;
        public double bblr = -2;

        public double score = 1.0;

        public void calculateScore()
        {
            double fkdrMax = Math.max(0, fkdr);
            this.score = (10 + Math.max(level, 0)) * fkdrMax * fkdrMax * (winstreak > 0 ? winstreak * 0.625 : 1);
        }
    }

    public Map<UUID, Tuple<RequestWrapper, BedwarsProfile>> getPlayerDataMap()
    {
        return this.playersInList;
    }

    /**
     * returns the bedwars level of the player
     *
     * @param playerObject the {@link JsonObject} received from {@link com.github.grizzlt.hypixelpublicapi.HypixelPublicAPIModLibrary}
     * @return bw level when found, otherwise -1, -2 when nicked
     */
    private static int getBwLevel(JsonObject playerObject)
    {
        if (playerObject == null) return -2;

        if (playerObject.has("achievements")) {
            JsonObject achievementsObject = playerObject.getAsJsonObject("achievements");
            if (achievementsObject.has("bedwars_level")) {
                return achievementsObject.get("bedwars_level").getAsInt();
            }
        }
        return -1;
    }

    /**
     * returns the fkdr of the player
     *
     * @param playerObject the {@link JsonObject} received from {@link com.github.grizzlt.hypixelpublicapi.HypixelPublicAPIModLibrary}
     * @return the fkdr when found, -1 when no final deaths, -2 when not found, -3 when nicked
     */
    private static double getFKDR(JsonObject playerObject)
    {
        if (playerObject == null) return -3;

        if (playerObject.has("stats")) {
            JsonObject statsObj = playerObject.getAsJsonObject("stats");
            if (statsObj.has("Bedwars")) {
                JsonObject bwObj = statsObj.getAsJsonObject("Bedwars");
                int fk = bwObj.get("final_kills_bedwars").getAsInt();
                int fd = bwObj.get("final_deaths_bedwars").getAsInt();
                if (fd != 0) {
                    return (double)fk / (double)fd;
                } else {
                    return -1;
                }
            }
        }
        return -2;
    }

    /**
     * returns the current winstreak of the player
     *
     * @param playerObject the {@link JsonObject} received from {@link com.github.grizzlt.hypixelpublicapi.HypixelPublicAPIModLibrary}
     * @return the winstreak when found, otherwise -1, -2 when nicked
     */
    private static int getWinStreak(JsonObject playerObject)
    {
        if (playerObject == null) return -2;

        if (playerObject.has("stats")) {
            JsonObject statsObj = playerObject.getAsJsonObject("stats");
            if (statsObj.has("Bedwars")) {
                JsonObject bwObj = statsObj.getAsJsonObject("Bedwars");
                return bwObj.get("winstreak").getAsInt();
            }
        }
        return -1;
    }

    /**
     * returns the win-loss ration of the player
     *
     * @param playerObject the {@link JsonObject} received from {@link com.github.grizzlt.hypixelpublicapi.HypixelPublicAPIModLibrary}
     * @return the wlr when found, -1 when no losses, -2 when not found, -3 when nicked
     */
    private static double getWinLossRatio(JsonObject playerObject)
    {
        if (playerObject == null) return -3;

        if (playerObject.has("stats")) {
            JsonObject statsObj = playerObject.getAsJsonObject("stats");
            if (statsObj.has("Bedwars")) {
                JsonObject bwObj = statsObj.getAsJsonObject("Bedwars");
                int wins = bwObj.get("wins_bedwars").getAsInt();
                int losses = bwObj.get("losses_bedwars").getAsInt();
                if (losses != 0) {
                    return (double)wins / (double)losses;
                } else {
                    return -1;
                }
            }
        }
        return -2;
    }

    /**
     * returns the beds-broken-beds-lost-ratio of the player
     *
     * @param playerObject the {@link JsonObject} received from {@link com.github.grizzlt.hypixelpublicapi.HypixelPublicAPIModLibrary}
     * @return the bblr when found, -1 when no beds lost, -2 when not found, -3 when nicked
     */
    private static double getBBLR(JsonObject playerObject)
    {
        if (playerObject == null) return -3;

        if (playerObject.has("stats")) {
            JsonObject statsObj = playerObject.getAsJsonObject("stats");
            if (statsObj.has("Bedwars")) {
                JsonObject bwObj = statsObj.getAsJsonObject("Bedwars");
                int bedsBroken = bwObj.get("beds_broken_bedwars").getAsInt();
                int bedsLost = bwObj.get("beds_lost_bedwars").getAsInt();
                if (bedsLost != 0) {
                    return (double)bedsBroken / (double)bedsLost;
                } else {
                    return -1;
                }
            }
        }
        return -2;
    }
}
