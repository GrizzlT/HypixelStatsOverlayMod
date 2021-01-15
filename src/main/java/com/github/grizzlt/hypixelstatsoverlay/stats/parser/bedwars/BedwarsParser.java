package com.github.grizzlt.hypixelstatsoverlay.stats.parser.bedwars;

import com.github.grizzlt.hypixelstatsoverlay.HypixelStatsOverlayMod;
import com.github.grizzlt.hypixelstatsoverlay.KeyBindManager;
import com.github.grizzlt.hypixelstatsoverlay.stats.IGameParser;
import com.github.grizzlt.hypixelstatsoverlay.stats.parser.RequestWrapper;
import com.github.grizzlt.hypixelstatsoverlay.util.ReflectionContainer;
import com.github.grizzlt.shadowedLibs.net.hypixel.api.reply.PlayerReply;
import com.github.grizzlt.shadowedLibs.net.hypixel.api.reply.StatusReply;
import com.google.common.collect.ComparisonChain;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.Tuple;
import net.minecraft.world.WorldSettings;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;

import java.util.*;

public class BedwarsParser implements IGameParser
{
    private final Comparator<NetworkPlayerInfo> playerComparator = new BedwarsComparator(this);
    private final BedwarsGameGuiOverlay bwGameTabRenderer = new BedwarsGameGuiOverlay(this);

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

    /**
     * Used to collect and process all data of the players in playersInTabList
     *
     * @param playersInTabList the {@link Collection} of players we need to process
     */
    public void gatherPlayers(Collection<NetworkPlayerInfo> playersInTabList)
    {
        NetworkPlayerInfo[] playerInfoList = playersInTabList.toArray(new NetworkPlayerInfo[0]);

        for (int i = 0; i < playerInfoList.length; ++i)
        {
            if (!this.playersInList.containsKey(playerInfoList[i].getGameProfile().getId())) {
                int finalI = i;
                BedwarsProfile bwProfile = new BedwarsProfile();
                RequestWrapper requestWrapper = new RequestWrapper(HypixelStatsOverlayMod.apiContainer.getAPI().handleHypixelAPIRequest(api ->
                        api.getPlayerByUuid(playerInfoList[finalI].getGameProfile().getId())
                ), wrapper -> {
                    JsonObject playerObject = ((PlayerReply)wrapper.getReply()).getPlayer();
                    bwProfile.level = getBwLevel(playerObject);
                    bwProfile.winstreak = getWinStreak(playerObject);
                    bwProfile.wlr = getWinLossRatio(playerObject);
                    bwProfile.fkdr = getFKDR(playerObject);
                    bwProfile.bblr = getBBLR(playerObject);
                });
                this.playersInList.put(playerInfoList[i].getGameProfile().getId(), new Tuple<>(requestWrapper, bwProfile));
            }
        }
    }

    static class BedwarsProfile
    {
        public int level = -1;
        public int winstreak = -1;
        public double fkdr = -2;
        public double wlr = -2;
        public double bblr = -2;
    }

    static class BedwarsComparator implements Comparator<NetworkPlayerInfo>
    {
        private final BedwarsParser bwParser;

        private BedwarsComparator(BedwarsParser bwParser) { this.bwParser = bwParser; }

        @Override
        public int compare(NetworkPlayerInfo o1, NetworkPlayerInfo o2) {
            ScorePlayerTeam scoreplayerteam = o1.getPlayerTeam();
            ScorePlayerTeam scoreplayerteam1 = o2.getPlayerTeam();

            BedwarsProfile bwProfile1 = this.bwParser.playersInList.get(o1.getGameProfile().getId()).getSecond();
            BedwarsProfile bwProfile2 = this.bwParser.playersInList.get(o2.getGameProfile().getId()).getSecond();
            int index1 = (int)(bwProfile1.level * bwProfile1.fkdr * bwProfile1.fkdr);
            int index2 = (int)(bwProfile2.level * bwProfile2.fkdr * bwProfile2.fkdr);
            return ComparisonChain.start()
                    //.compareFalseFirst(HypixelStatsOverlayMod.partyManager.getPartyMembers().containsKey(o1.getGameProfile().getName()), HypixelStatsOverlayMod.partyManager.getPartyMembers().containsKey(o2.getGameProfile().getName()))
                    .compareTrueFirst(o1.getGameType() != WorldSettings.GameType.SPECTATOR, o2.getGameType() != WorldSettings.GameType.SPECTATOR)
                    .compare(scoreplayerteam != null ? scoreplayerteam.getColorPrefix() : "", scoreplayerteam1 != null ? scoreplayerteam1.getColorPrefix() : "")
                    /*.compareTrueFirst(bwProfile1.hax > 0, bwProfile2.hax > 0) //first get the hackers
                    .compareTrueFirst(bwProfile1.sniper, bwProfile2.sniper) // then get the snipers*/
                    .compareTrueFirst(bwProfile1.fkdr == -3, bwProfile2.fkdr == -3) // then get the nicked players
                    .compare(index2, index1) //swapped values to get highest one first
                    .compare(o1.getGameProfile().getName(), o2.getGameProfile().getName())
                    .result();
        }
    }

    public Comparator<NetworkPlayerInfo> getBwComparator()
    {
        return this.playerComparator;
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
                if (bedsBroken != 0) {
                    return (double)bedsBroken / (double)bedsLost;
                } else {
                    return -1;
                }
            }
        }
        return -2;
    }
}
