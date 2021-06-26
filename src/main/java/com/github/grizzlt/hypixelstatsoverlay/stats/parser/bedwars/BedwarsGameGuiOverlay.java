package com.github.grizzlt.hypixelstatsoverlay.stats.parser.bedwars;

import com.github.grizzlt.hypixelstatsoverlay.HypixelStatsOverlayMod;
import com.github.grizzlt.hypixelstatsoverlay.mixin.GuiPlayerTabOverlayMixin;
import com.github.grizzlt.hypixelstatsoverlay.overlay.IGuiOverlayComponent;
import com.github.grizzlt.hypixelstatsoverlay.overlay.builder.TabGui;
import com.github.grizzlt.hypixelstatsoverlay.overlay.grid.GuiOverlayElementGrid;
import com.github.grizzlt.hypixelstatsoverlay.util.Vector2i;
import com.google.common.collect.ComparisonChain;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.WorldSettings;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class BedwarsGameGuiOverlay
{
    private final BedwarsParser bwParser;
    private final Comparator<NetworkPlayerInfo> playerComparator;
    private final Comparator<NetworkPlayerInfo> partyComparator;

    //cached variable
    private final DecimalFormat df;
    private final Vector2i vector8x8 = new Vector2i(8, 8);

    private IGuiOverlayComponent root;
    private IGuiOverlayComponent playerList = TabGui.EMPTY;
    private boolean needsUpdate = true;

    public BedwarsGameGuiOverlay(BedwarsParser parser)
    {
        this.bwParser = parser;
        this.playerComparator = new BedwarsComparator(this.bwParser);
        this.partyComparator = new PartyComparator(this.bwParser);

        this.df = new DecimalFormat("0.0#");
        this.df.setRoundingMode(RoundingMode.HALF_UP);

        this.buildGuiSkeleton();
    }

    private void buildGuiSkeleton()
    {
        root = TabGui.centerHorizontal().withChild(TabGui.background(Integer.MIN_VALUE, 1)
                .withChild(TabGui.verticalList()
                        .withChild(TabGui.centerHorizontal(TabGui.text(() -> ((GuiPlayerTabOverlayMixin)Minecraft.getMinecraft().ingameGUI.getTabList()).getHeader())))
                        .withChild(TabGui.spacing().withHeight(2))
                        .withChild(TabGui.centerHorizontal(TabGui.wrapper().withChild(() -> this.playerList)))
                        .withChild(TabGui.spacing().withHeight(2))
                        .withChild(TabGui.centerHorizontal(TabGui.text(() -> ((GuiPlayerTabOverlayMixin)Minecraft.getMinecraft().ingameGUI.getTabList()).getFooter())))
                )
        );
    }

    public void renderPlayerList(int width, Scoreboard scoreboard, ScoreObjective scoreObjective)
    {
        if (this.needsUpdate) {
            System.out.println("Building gui!");
            this.playerList = buildGui(scoreObjective);
            this.needsUpdate = false;
        }

        try
        {
            root.prepareForDrawing();
            root.draw(new Vector2i(25, 10), new Vector2i(width - 50, 0));
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private IGuiOverlayComponent buildGui(ScoreObjective objective)
    {
        List<NetworkPlayerInfo> playersInGame;
        List<NetworkPlayerInfo> playersInParty;
        Map<String, UUID> partyMembers = HypixelStatsOverlayMod.instance.getPartyManager().getPartyMembers();
        Map<Boolean, List<NetworkPlayerInfo>> playerLists = Minecraft.getMinecraft().getNetHandler().getPlayerInfoMap().stream()
                .collect(Collectors.partitioningBy(info -> partyMembers.containsValue(info.getGameProfile().getId())));
        playersInGame = playerLists.get(false);
        playersInParty = playerLists.get(true);

        //get their stats when not already done so
        this.bwParser.gatherPlayers(playersInGame.stream().map(player -> player.getGameProfile().getId()).collect(Collectors.toList()));
        this.bwParser.gatherPlayers(playersInParty.stream().map(player -> player.getGameProfile().getId()).collect(Collectors.toList()));

        IGuiOverlayComponent gameGrid = TabGui.EMPTY;
        if (!playersInGame.isEmpty())
        {
            List<NetworkPlayerInfo> sortedPlayersInGame = playersInGame.stream().sorted(this.playerComparator).collect(Collectors.toList());

            int rowCount = sortedPlayersInGame.size() + 1;
            GuiOverlayElementGrid tempGrid = TabGui.grid(new Vector2i(10, rowCount));

            //first row with titles
            this.createTitles(tempGrid);

            for (int i = 1; i < rowCount; ++i)
            {
                NetworkPlayerInfo player = sortedPlayersInGame.get(i - 1);
                this.generatePlayerInformationRow(player, objective, tempGrid, i);

                if (i < sortedPlayersInGame.size())
                {
                    tempGrid.withRowSpacing(i, 1);
                }
            }
            gameGrid = TabGui.centerHorizontal().withChild(TabGui.verticalList()
                    .withChild(TabGui.centerHorizontal().withChild(TabGui.text(new ChatComponentText(EnumChatFormatting.BLUE + "== PLAYERS =="))))
                    .withChild(TabGui.spacing().withHeight(1))
                    .withChild(tempGrid));
        }

        IGuiOverlayComponent partyGrid = TabGui.EMPTY;
        if (!playersInParty.isEmpty())
        {
            //sort these players
            List<NetworkPlayerInfo> sortedPlayersInParty = playersInParty.stream().sorted(this.partyComparator).collect(Collectors.toList());

            int rowCount = sortedPlayersInParty.size() + 1;
            GuiOverlayElementGrid tempGrid = TabGui.grid(new Vector2i(10, rowCount));

            //first row with titles
            createTitles(tempGrid);

            for (int i = 1; i < rowCount; ++i)
            {
                NetworkPlayerInfo player = sortedPlayersInParty.get(i - 1);
                this.generatePlayerInformationRow(player, objective, tempGrid, i);

                if (i < sortedPlayersInParty.size())
                {
                    tempGrid.withRowSpacing(i, 1);
                }
            }
            partyGrid = TabGui.centerHorizontal().withChild(
                    TabGui.verticalList()
                            .withChild(TabGui.spacing().withHeight(5))
                            .withChild(TabGui.centerHorizontal().withChild(TabGui.text(new ChatComponentText(EnumChatFormatting.GOLD + "== PARTY =="))))
                            .withChild(TabGui.spacing().withHeight(1))
                            .withChild(tempGrid));
        }

        return TabGui.verticalList()
                .withChild(gameGrid)
                .withChild(partyGrid);
    }

    public void markDirty()
    {
        this.needsUpdate = true;
    }

    public String getPlayerDisplayName(NetworkPlayerInfo networkPlayerInfoIn)
    {
        BedwarsParser.BedwarsProfile profile = getBwProfile(networkPlayerInfoIn);
        String suffix = "";
        if (profile != null) {
            if (profile.isNicked)
            {
                suffix = " (NICK)";
            }
        }
        return getPlayerName(networkPlayerInfoIn) + suffix;
    }

    public int getPlayerNameColor(BedwarsParser.BedwarsProfile profile)
    {
        return profile != null && profile.isNicked ? ((89 & 0xFF) << 24) | ((0xFF) << 16) | ((13 & 0xFF) << 8)  | ((13 & 0xFF)) : 553648127;
    }

    private String getPlayerName(NetworkPlayerInfo info)
    {
        return info.getDisplayName() != null ? info.getDisplayName().getFormattedText() : ScorePlayerTeam.formatPlayerName(info.getPlayerTeam(), info.getGameProfile().getName());
    }

    private BedwarsParser.BedwarsProfile getBwProfile(NetworkPlayerInfo playerInfo)
    {
        return this.bwParser.getPlayerDataMap().get(playerInfo.getGameProfile().getId());
    }

    private void createTitles(GuiOverlayElementGrid grid)
    {
        grid.withRow(0,
                TabGui.EMPTY, TabGui.EMPTY, TabGui.EMPTY,
                TabGui.horizontalList().withChild(TabGui.text(new ChatComponentText("FKDR")))
                        .withChild(TabGui.spacing().withWidth(2)),
                TabGui.horizontalList().withChild(TabGui.text(new ChatComponentText("WLR")))
                        .withChild(TabGui.spacing().withWidth(2)),
                TabGui.horizontalList().withChild(TabGui.text(new ChatComponentText("WS")))
                        .withChild(TabGui.spacing().withWidth(2)),
                TabGui.horizontalList().withChild(TabGui.text(new ChatComponentText("BBLR")))
                        .withChild(TabGui.spacing().withWidth(2)),
                TabGui.horizontalList().withChild(TabGui.text(new ChatComponentText("VAL")))
                        .withChild(TabGui.spacing().withWidth(2)),
                TabGui.EMPTY, TabGui.EMPTY
        );
    }

    private void generatePlayerInformationRow(NetworkPlayerInfo playerInfo, ScoreObjective objective, GuiOverlayElementGrid grid, int row)
    {
        Callable<Integer> backgroundColor = () -> getPlayerNameColor(getBwProfile(playerInfo));

        grid.withRow(row,
                TabGui.fill(backgroundColor).withChild(TabGui.horizontalList()
                        .withChild(TabGui.spacing().withWidth(1))
                        .withChild(TabGui.text(() -> {
                            BedwarsParser.BedwarsProfile profile = getBwProfile(playerInfo);
                            return new ChatComponentText(profile.level == -1 ? " [?✫] " : (" [" + profile.level + "✫] "));
                        }))
                        .withChild(TabGui.spacing().withWidth(1))),
                TabGui.fill(backgroundColor).withChild(TabGui.horizontalList()
                        .withChild(TabGui.stacked()
                                .withChild(TabGui.texturedRect(playerInfo.getLocationSkin(), vector8x8, vector8x8, vector8x8, new Vector2i(64, 64)))
                                .withChild(TabGui.texturedRect(playerInfo.getLocationSkin(), new Vector2i(40, 8), vector8x8, vector8x8, new Vector2i(64, 64))))
                        .withChild(TabGui.spacing().withWidth(1))),
                TabGui.fill(backgroundColor).withChild(TabGui.horizontalList()
                        .withChild(TabGui.text(() -> {
                            String playerDisplayName = getPlayerDisplayName(playerInfo);
                            if (playerInfo.getGameType() == WorldSettings.GameType.SPECTATOR) return new ChatComponentText(EnumChatFormatting.GRAY + (EnumChatFormatting.ITALIC + playerDisplayName));
                            return new ChatComponentText(playerDisplayName);
                        })).withChild(TabGui.spacing().withWidth(5))),
                TabGui.fill(backgroundColor).withChild(TabGui.horizontalList().withChild(TabGui.text(() -> {
                    BedwarsParser.BedwarsProfile profile = getBwProfile(playerInfo);
                    return new ChatComponentText(profile.fkdr < -1.8 ? "?" : profile.fkdr < -0.8 ? "NaN" : this.df.format(profile.fkdr));
                })).withChild(TabGui.spacing().withWidth(3))),
                TabGui.fill(backgroundColor).withChild(TabGui.horizontalList().withChild(TabGui.text(() -> {
                    BedwarsParser.BedwarsProfile profile = getBwProfile(playerInfo);
                    return new ChatComponentText(profile.wlr < -1.8 ? "?" : profile.wlr < -0.8 ? "NaN" : this.df.format(profile.wlr));
                })).withChild(TabGui.spacing().withWidth(3))),
                TabGui.fill(backgroundColor).withChild(TabGui.horizontalList().withChild(TabGui.text(() -> {
                    BedwarsParser.BedwarsProfile profile = getBwProfile(playerInfo);
                    return new ChatComponentText(profile.winstreak == -1 ? "?" : String.valueOf(profile.winstreak));
                })).withChild(TabGui.spacing().withWidth(3))),
                TabGui.fill(backgroundColor).withChild(TabGui.horizontalList().withChild(TabGui.text(() -> {
                    BedwarsParser.BedwarsProfile profile = getBwProfile(playerInfo);
                    return new ChatComponentText(profile.bblr < -1.8 ? "?" : profile.bblr < -0.8 ? "NaN" : this.df.format(profile.bblr));
                })).withChild(TabGui.spacing().withWidth(3))),
                TabGui.fill(backgroundColor).withChild(TabGui.horizontalList().withChild(TabGui.text(() -> {
                    BedwarsParser.BedwarsProfile profile = getBwProfile(playerInfo);
                    return new ChatComponentText(profile.isNicked ? "?" : this.df.format(profile.score / this.bwParser.getPlayerDataMap().get(Minecraft.getMinecraft().thePlayer.getUniqueID()).score));
                })).withChild(TabGui.spacing().withWidth(3))),
                TabGui.fill(backgroundColor).withChild(TabGui.horizontalList()
                        .withChild(TabGui.text(() -> {
                            if (objective == null || playerInfo.getGameType() == WorldSettings.GameType.SPECTATOR) return new ChatComponentText("");
                            int score = objective.getScoreboard().getValueFromObjective(playerInfo.getGameProfile().getName(), objective).getScorePoints();
                            return new ChatComponentText(EnumChatFormatting.YELLOW + String.valueOf(score));
                        })).withChild(TabGui.spacing().withWidth(1))
                ),
                TabGui.fill(backgroundColor).withChild(TabGui.horizontalList()
                        .withChild(TabGui.text(() -> {
                            int ping = playerInfo.getResponseTime();
                            return new ChatComponentText((ping > 600 ? EnumChatFormatting.DARK_RED.toString() : ping > 300 ? EnumChatFormatting.RED.toString() : ping > 150 ? EnumChatFormatting.DARK_GREEN.toString() : EnumChatFormatting.GREEN.toString()) + (playerInfo.getResponseTime() == 0 ? "?" : playerInfo.getResponseTime()));
                        })).withChild(TabGui.spacing().withWidth(1)))
        );
    }

    static class BedwarsComparator implements Comparator<NetworkPlayerInfo>
    {
        private final BedwarsParser bwParser;

        private BedwarsComparator(BedwarsParser bwParser) { this.bwParser = bwParser; }

        @Override
        public int compare(NetworkPlayerInfo o1, NetworkPlayerInfo o2) {
            BedwarsParser.BedwarsProfile bwProfile1 = this.bwParser.getPlayerDataMap().get(o1.getGameProfile().getId());
            BedwarsParser.BedwarsProfile bwProfile2 = this.bwParser.getPlayerDataMap().get(o2.getGameProfile().getId());
            ScorePlayerTeam scoreplayerteam = o1.getPlayerTeam();
            ScorePlayerTeam scoreplayerteam1 = o2.getPlayerTeam();

            return ComparisonChain.start()
                    .compareTrueFirst(o1.getGameType() != WorldSettings.GameType.SPECTATOR, o2.getGameType() != WorldSettings.GameType.SPECTATOR)
                    .compareTrueFirst(bwProfile1.isNicked, bwProfile2.isNicked) // then get the nicked players
                    .compare(scoreplayerteam != null ? scoreplayerteam.getRegisteredName() : "", scoreplayerteam1 != null ? scoreplayerteam1.getRegisteredName() : "")
                    .compare(bwProfile2.score, bwProfile1.score)
                    .compare(bwProfile2.fkdr, bwProfile1.fkdr)
                    .compare(o1.getGameProfile().getName(), o2.getGameProfile().getName())
                    .result();
        }
    }

    public static class PartyComparator implements Comparator<NetworkPlayerInfo>
    {
        private final BedwarsParser bwParser;

        private PartyComparator(BedwarsParser bwParser) { this.bwParser = bwParser; }

        @Override
        public int compare(NetworkPlayerInfo o1, NetworkPlayerInfo o2) {
            BedwarsParser.BedwarsProfile bwProfile1 = this.bwParser.getPlayerDataMap().get(o1.getGameProfile().getId());
            BedwarsParser.BedwarsProfile bwProfile2 = this.bwParser.getPlayerDataMap().get(o2.getGameProfile().getId());

            return ComparisonChain.start()
                    .compareTrueFirst(HypixelStatsOverlayMod.instance.getPartyManager().getPartyLeader().getSecond().equals(o1.getGameProfile().getId()), HypixelStatsOverlayMod.instance.getPartyManager().getPartyLeader().getSecond().equals(o2.getGameProfile().getId()))
                    .compareTrueFirst(o1.getGameType() != WorldSettings.GameType.SPECTATOR, o2.getGameType() != WorldSettings.GameType.SPECTATOR)
                    .compareTrueFirst(bwProfile1.isNicked, bwProfile2.isNicked) // then get the nicked players
                    .compare(bwProfile2.score, bwProfile1.score)
                    .compare(bwProfile2.fkdr, bwProfile1.fkdr)
                    .compare(o1.getGameProfile().getName(), o2.getGameProfile().getName())
                    .result();
        }
    }
}
