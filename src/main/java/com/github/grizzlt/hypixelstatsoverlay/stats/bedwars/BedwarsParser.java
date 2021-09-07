package com.github.grizzlt.hypixelstatsoverlay.stats.bedwars;

import com.github.grizzlt.hypixelstatsoverlay.HypixelStatsOverlayMod;
import com.github.grizzlt.hypixelstatsoverlay.KeyBindManager;
import com.github.grizzlt.hypixelstatsoverlay.events.PlayerListUpdateEvent;
import com.github.grizzlt.hypixelstatsoverlay.stats.IGameParser;
import com.github.grizzlt.hypixelstatsoverlay.stats.cache.LobbyBasedCacheExpiry;
import com.github.grizzlt.hypixelstatsoverlay.stats.cache.PlayerDataCacheEntry;
import com.github.grizzlt.hypixelstatsoverlay.stats.gui.IPlayerGameData;
import com.github.grizzlt.hypixelstatsoverlay.util.ReflectionContainer;
import com.github.grizzlt.serverbasedmodlibrary.ServerBasedRegisterUtil;
import com.google.gson.JsonElement;
import net.hypixel.api.reply.PlayerReply;
import net.hypixel.api.reply.StatusReply;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BedwarsParser implements IGameParser
{
    private final BedwarsGameGuiOverlay bwGameTabRenderer = new BedwarsGameGuiOverlay(this);

    private final Map<UUID, BedwarsProfile> playersInList = new HashMap<>();

    private boolean isInLobby = false;
    private boolean isActive = false;

    @Override
    public void onPlayerSwitchWorld(@NotNull StatusReply statusReply)
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
    public void loadConfig(Configuration config)
    {
        ConfigCategory bedwarsCat = config.getCategory("stats.bedwars");
        config.get(bedwarsCat.getQualifiedName(), "display_mode", GameMode.OVERALL.getConfigValue(), "Selects what mode's stats will be displayed")
                .setValidValues(Arrays.stream(GameMode.values()).map(GameMode::getConfigValue).toArray(String[]::new));
    }

    @Override
    public void activate()
    {
        this.isActive = true;
    }

    @Override
    public void deActivate()
    {
        this.isActive = false;
    }

    @Override
    public void registerEvents(ServerBasedRegisterUtil serverBasedRegisterUtil)
    {
        serverBasedRegisterUtil.registerToEventBus(this);
    }

    @SubscribeEvent
    public void onPlayerListUpdate(PlayerListUpdateEvent event)
    {
        if (!this.isActive) return;

        this.bwGameTabRenderer.markDirty();
    }

    public BedwarsProfile getPlayerProfile(UUID playerId)
    {
        if (this.playersInList.containsKey(playerId)) {
            return this.playersInList.get(playerId);
        }

        PlayerDataCacheEntry<LobbyBasedCacheExpiry> cacheEntry = HypixelStatsOverlayMod.instance.getPlayerDataCache().getOrLookupPlayer(playerId, new LobbyBasedCacheExpiry(LobbyBasedCacheExpiry.ExpiryType.LEAVE_GAME, 1, this.isInLobby));
        if (cacheEntry.isPending()) {
            return BedwarsProfile.PENDING_PROFILE;
        }

        return this.playersInList.computeIfAbsent(playerId, uuid -> cacheEntry.getPlayerData()
                .map(PlayerReply::getPlayer)
                .map(player -> {
                    if (player.exists()) {
                        return new BedwarsProfile(getBwLevel(player), getWinStreak(player), getFKDR(player), getWinLossRatio(player), getBBLR(player));
                    }
                    return BedwarsProfile.NICKED;
                }).orElse(BedwarsProfile.NICKED));
    }

    public static class BedwarsProfile implements IPlayerGameData
    {

        public static final BedwarsProfile PENDING_PROFILE = new BedwarsProfile(false);
        public static final BedwarsProfile NICKED = new BedwarsProfile(true);

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
            this.score = (Math.max(level, 0) + Math.max(0, fkdr) * 75) * ((Math.pow(2, wlr - 2) / 5) + 0.95) * Math.pow(1.07, winstreak);
        }

        @Override
        public boolean isNicked()
        {
            return isNicked;
        }
    }

    /**
     * @return BW level: -1 = unknown
     */
    private static int getBwLevel(PlayerReply.Player playerObject)
    {
        JsonElement levelElement = playerObject.getProperty("achievements.bedwars_level");
        if (levelElement != null) {
            return levelElement.getAsInt();
        }
        return -1;
    }

    /**
     * @return FKDR: -2 = unknown, -1 = no final deaths
     */
    private static double getFKDR(PlayerReply.Player playerObject)
    {
        JsonElement finalDeathsObj = playerObject.getProperty("stats.Bedwars.final_deaths_bedwars");
        if (finalDeathsObj == null) {
            return -1.0;
        }
        JsonElement finalKillsObj = playerObject.getProperty("stats.Bedwars.final_kills_bedwars");
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
    private static int getWinStreak(PlayerReply.Player playerObject)
    {
        JsonElement wsElement = playerObject.getProperty("stats.Bedwars.winstreak");
        if (wsElement == null) {
            return -1;
        }
        return wsElement.getAsInt();
    }

    /**
     * @return WLR: -2.0 = unknown, -1.0 = no losses
     */
    private static double getWinLossRatio(PlayerReply.Player playerObject)
    {
        JsonElement lossesObj = playerObject.getProperty("stats.Bedwars.losses_bedwars");
        if (lossesObj == null) {
            return -1.0;
        }
        JsonElement winsObj = playerObject.getProperty("stats.Bedwars.wins_bedwars");
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
    private static double getBBLR(PlayerReply.Player playerObject)
    {
        JsonElement lostObj = playerObject.getProperty("stats.Bedwars.beds_lost_bedwars");
        if (lostObj == null) {
            return -1.0;
        }
        JsonElement brokenObj = playerObject.getProperty("stats.Bedwars.beds_broken_bedwars");
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

    public enum GameMode
    {
        LOBBY("LOBBY", "lobby"),
        OVERALL(null, "overall"),
        SOLO("EIGHT_ONE", "solo"),
        DOUBLES("EIGHT_TWO", "doubles"),
        THREES("FOUR_THREE", "3s"),
        FOURS("FOUR_FOUR", "4s"),
        FOURFOUR("TWO_FOUR", "4v4"),
        ULTIMATE2s("EIGHT_TWO_ULTIMATE", "ultimate2s"),
        ULTIMATE4s("FOUR_FOUR_ULTIMATE", "ultimate4s"),
        PRACTICE("PRACTICE", "practice"),
        CASTLE("CASTLE", "castle");

        private final String apiName;
        private final String configValue;

        GameMode(String apiName, String configValue)
        {
            this.apiName = apiName;
            this.configValue = configValue;
        }

        public String getConfigValue()
        {
            return configValue;
        }
    }
}
