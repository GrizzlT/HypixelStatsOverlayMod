package com.github.grizzlt.hypixelstatsoverlay.stats.parser.bedwars;

import com.github.grizzlt.hypixelstatsoverlay.HypixelStatsOverlayMod;
import com.github.grizzlt.hypixelstatsoverlay.overlay.IGuiOverlayComponent;
import com.github.grizzlt.hypixelstatsoverlay.overlay.builder.BuilderWrapper;
import com.github.grizzlt.hypixelstatsoverlay.overlay.builder.GuiOverlayBuilder;
import com.github.grizzlt.hypixelstatsoverlay.overlay.grid.GuiOverlayElementGrid;
import com.github.grizzlt.hypixelstatsoverlay.util.NetworkPlayerInfoWrapper;
import com.github.grizzlt.hypixelstatsoverlay.util.PartyManager;
import com.github.grizzlt.hypixelstatsoverlay.util.ReflectionContainer;
import com.github.grizzlt.hypixelstatsoverlay.util.Vector2i;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.Tuple;
import net.minecraft.world.WorldSettings;

import java.awt.*;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class BedwarsGameGuiOverlay
{
    /**
     * the instance of the {@link BedwarsParser}
     */
    private final BedwarsParser bwParser;
    private final Comparator<NetworkPlayerInfo> playerComparator;
    private final Comparator<NetworkPlayerInfo> partyComparator;

    //cached variable
    private final DecimalFormat df;

    private GuiOverlayBuilder rootBuilder;
    private IGuiOverlayComponent root;
    private BuilderWrapper contentWrapper = GuiOverlayBuilder.wrapper();
    private List<NetworkPlayerInfoWrapper> cached = new ArrayList<>();

    //cache
    private final Vector2i cached8x8 = new Vector2i(8, 8);

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
        rootBuilder = GuiOverlayBuilder.centerHorizontal().setBuilder(GuiOverlayBuilder.background(Integer.MIN_VALUE, 1)
                .setBuilder(GuiOverlayBuilder.verticalList()
                        .addBuilder(GuiOverlayBuilder.centerHorizontal().setBuilder(
                                GuiOverlayBuilder.text(() -> (IChatComponent)ReflectionContainer.headerField.get(ReflectionContainer.guiTabOverlayField.get(Minecraft.getMinecraft().ingameGUI)))))
                        .addBuilder(GuiOverlayBuilder.spacing(new Vector2i(0, 2)))
                        .addBuilder(GuiOverlayBuilder.centerHorizontal().setBuilder(contentWrapper))
                        .addBuilder(GuiOverlayBuilder.spacing(new Vector2i(0, 2)))
                        .addBuilder(GuiOverlayBuilder.centerHorizontal().setBuilder(
                                GuiOverlayBuilder.text(() -> (IChatComponent)ReflectionContainer.footerField.get(ReflectionContainer.guiTabOverlayField.get(Minecraft.getMinecraft().ingameGUI)))))
                )
        );
    }

    public void renderPlayerList(int width, Scoreboard scoreboard, ScoreObjective scoreObjective)
    {
        List<NetworkPlayerInfoWrapper> players = Minecraft.getMinecraft().getNetHandler().getPlayerInfoMap().stream().map(NetworkPlayerInfoWrapper::new).collect(Collectors.toList());
        if (!players.equals(this.cached))
        {
            System.out.println("Building gui!");
            this.root = this.buildGui(scoreObjective);
            this.cached = players;
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
        List<NetworkPlayerInfo> playersInGame = new ArrayList<>();
        List<NetworkPlayerInfo> playersInParty = new ArrayList<>();
        Map<String, UUID> partyMembers = HypixelStatsOverlayMod.partyManager.getPartyMembers();
        if (!partyMembers.isEmpty())
        {
            for (NetworkPlayerInfo playerInfo : Minecraft.getMinecraft().getNetHandler().getPlayerInfoMap())
            {
                if (partyMembers.containsValue(playerInfo.getGameProfile().getId()))
                {
                    playersInParty.add(playerInfo);
                } else {
                    playersInGame.add(playerInfo);
                }
            }
        } else {
            playersInGame = Lists.newArrayList(Minecraft.getMinecraft().getNetHandler().getPlayerInfoMap());
        }
        //get their stats when not already done so
        this.bwParser.gatherPlayers(playersInGame.stream().map(player -> player.getGameProfile().getId()).collect(Collectors.toList()));
        this.bwParser.gatherPlayers(playersInParty.stream().map(player -> player.getGameProfile().getId()).collect(Collectors.toList()));

        GuiOverlayBuilder gameGrid = GuiOverlayBuilder.empty();
        if (!playersInGame.isEmpty())
        {
            //sort these players
            List<NetworkPlayerInfo> sortedPlayersInGame = playersInGame.stream().sorted(this.playerComparator).collect(Collectors.toList());

            int rowCount = sortedPlayersInGame.size() + 1;
            GuiOverlayElementGrid tempGrid = GuiOverlayBuilder.grid(new Vector2i(10, rowCount));

            //first row with titles
            tempGrid.setRowBuilders(0,
                    GuiOverlayBuilder.empty(),
                    GuiOverlayBuilder.empty(),
                    GuiOverlayBuilder.empty(),
                    GuiOverlayBuilder.horizontalList().addBuilder(GuiOverlayBuilder.text(new ChatComponentText("FKDR")))
                            .addBuilder(GuiOverlayBuilder.spacing(new Vector2i(2, 0))),
                    GuiOverlayBuilder.horizontalList().addBuilder(GuiOverlayBuilder.text(new ChatComponentText("WLR")))
                            .addBuilder(GuiOverlayBuilder.spacing(new Vector2i(2, 0))),
                    GuiOverlayBuilder.horizontalList().addBuilder(GuiOverlayBuilder.text(new ChatComponentText("WS")))
                            .addBuilder(GuiOverlayBuilder.spacing(new Vector2i(2, 0))),
                    GuiOverlayBuilder.horizontalList().addBuilder(GuiOverlayBuilder.text(new ChatComponentText("BBLR")))
                            .addBuilder(GuiOverlayBuilder.spacing(new Vector2i(2, 0))),
                    GuiOverlayBuilder.horizontalList().addBuilder(GuiOverlayBuilder.text(new ChatComponentText("VAL")))
                            .addBuilder(GuiOverlayBuilder.spacing(new Vector2i(2, 0))),
                    GuiOverlayBuilder.empty(),
                    GuiOverlayBuilder.empty()
            );

            for (int i = 1; i < rowCount; ++i)
            {
                NetworkPlayerInfo player = sortedPlayersInGame.get(i - 1);
                BedwarsParser.BedwarsProfile profile = this.bwParser.getPlayerDataMap().get(player.getGameProfile().getId()).getSecond();
                Tuple<String, Integer> backgroundColorPlayer = this.getPlayerName(player, profile);
                tempGrid.setRowBuilders(i,
                        GuiOverlayBuilder.fill(backgroundColorPlayer.getSecond(), 0).setBuilder(GuiOverlayBuilder.horizontalList()
                                .addBuilder(GuiOverlayBuilder.spacing(new Vector2i(1, 0)))
                                .addBuilder(GuiOverlayBuilder.text(() -> new ChatComponentText(profile.level == -1 || profile.level == -2 ? " [?✫] " : (" [" + profile.level + "✫] "))))
                                .addBuilder(GuiOverlayBuilder.spacing(new Vector2i(1, 0)))
                        ),
                        GuiOverlayBuilder.fill(backgroundColorPlayer.getSecond(), 0).setBuilder(GuiOverlayBuilder.horizontalList()
                                .addBuilder(GuiOverlayBuilder.stacked()
                                        .addBuilder(GuiOverlayBuilder.texturedRect(player.getLocationSkin(), cached8x8, cached8x8, cached8x8, new Vector2i(64, 64)))
                                        .addBuilder(GuiOverlayBuilder.texturedRect(player.getLocationSkin(), new Vector2i(40, 8), cached8x8, cached8x8, new Vector2i(64, 64))))
                                .addBuilder(GuiOverlayBuilder.spacing(new Vector2i(1, 0)))),
                        GuiOverlayBuilder.fill(backgroundColorPlayer.getSecond(), 0).setBuilder(GuiOverlayBuilder.horizontalList()
                                .addBuilder(GuiOverlayBuilder.text(() -> {
                                    if (player.getGameType() == WorldSettings.GameType.SPECTATOR) return new ChatComponentText(EnumChatFormatting.GRAY + (EnumChatFormatting.ITALIC + backgroundColorPlayer.getFirst()));
                                    return new ChatComponentText(backgroundColorPlayer.getFirst());
                                })).addBuilder(GuiOverlayBuilder.spacing(new Vector2i(5, 0)))),
                        GuiOverlayBuilder.fill(backgroundColorPlayer.getSecond(), 0).setBuilder(GuiOverlayBuilder.horizontalList().addBuilder(GuiOverlayBuilder.text(() -> new ChatComponentText(profile.fkdr == -3 || profile.fkdr == -2 ? "?" : profile.fkdr == -1 ? "NaN" : this.df.format(profile.fkdr))))
                                .addBuilder(GuiOverlayBuilder.spacing(new Vector2i(3, 0)))),
                        GuiOverlayBuilder.fill(backgroundColorPlayer.getSecond(), 0).setBuilder(GuiOverlayBuilder.horizontalList().addBuilder(GuiOverlayBuilder.text(() -> new ChatComponentText(profile.wlr == -3 || profile.wlr == -2 ? "?" : profile.wlr == -1 ? "NaN" : this.df.format(profile.wlr))))
                                .addBuilder(GuiOverlayBuilder.spacing(new Vector2i(3, 0)))),
                        GuiOverlayBuilder.fill(backgroundColorPlayer.getSecond(), 0).setBuilder(GuiOverlayBuilder.horizontalList().addBuilder(GuiOverlayBuilder.text(() -> new ChatComponentText(profile.winstreak == -2 || profile.winstreak == -1 ? "?" : String.valueOf(profile.winstreak))))
                                .addBuilder(GuiOverlayBuilder.spacing(new Vector2i(3, 0)))),
                        GuiOverlayBuilder.fill(backgroundColorPlayer.getSecond(), 0).setBuilder(GuiOverlayBuilder.horizontalList().addBuilder(GuiOverlayBuilder.text(() -> new ChatComponentText(profile.bblr == -3 || profile.bblr == -2 ? "?" : profile.bblr == -1 ? "NaN" : this.df.format(profile.bblr))))
                                .addBuilder(GuiOverlayBuilder.spacing(new Vector2i(3, 0)))),
                        GuiOverlayBuilder.fill(backgroundColorPlayer.getSecond(), 0).setBuilder(GuiOverlayBuilder.horizontalList().addBuilder(GuiOverlayBuilder.text(() -> new ChatComponentText(this.df.format(profile.score / this.bwParser.getPlayerDataMap().get(Minecraft.getMinecraft().thePlayer.getUniqueID()).getSecond().score))))
                                .addBuilder(GuiOverlayBuilder.spacing(new Vector2i(3, 0)))),
                        GuiOverlayBuilder.empty(),
                        GuiOverlayBuilder.fill(backgroundColorPlayer.getSecond(), 0).setBuilder(GuiOverlayBuilder.horizontalList()
                                .addBuilder(GuiOverlayBuilder.text(() -> {
                                    int ping = player.getResponseTime();
                                    return new ChatComponentText((ping > 600 ? EnumChatFormatting.DARK_RED.toString() : ping > 300 ? EnumChatFormatting.RED.toString() : ping > 150 ? EnumChatFormatting.DARK_GREEN.toString() : EnumChatFormatting.GREEN.toString()) + (player.getResponseTime() == 0 ? "?" : player.getResponseTime()));
                                }))
                                .addBuilder(GuiOverlayBuilder.spacing(new Vector2i(1, 0))))
                );

                if (objective != null && player.getGameType() != WorldSettings.GameType.SPECTATOR)
                {
                    int score = objective.getScoreboard().getValueFromObjective(player.getGameProfile().getName(), objective).getScorePoints();
                    tempGrid.setBuilder(i, 8, GuiOverlayBuilder.fill(backgroundColorPlayer.getSecond(), 0).setBuilder(GuiOverlayBuilder.horizontalList()
                            .addBuilder(GuiOverlayBuilder.text(() -> new ChatComponentText(EnumChatFormatting.YELLOW + String.valueOf(score))))
                            .addBuilder(GuiOverlayBuilder.spacing(new Vector2i(1, 0)))));
                }

                if (i < sortedPlayersInGame.size())
                {
                    tempGrid.setRowSpacing(i, 1);
                }
            }
            gameGrid = GuiOverlayBuilder.centerHorizontal().setBuilder(GuiOverlayBuilder.verticalList()
                    .addBuilder(GuiOverlayBuilder.centerHorizontal().setBuilder(GuiOverlayBuilder.text(new ChatComponentText(EnumChatFormatting.BLUE + "== PLAYERS =="))))
                    .addBuilder(GuiOverlayBuilder.spacing(new Vector2i(0, 1)))
                    .addBuilder(tempGrid));
        }

        GuiOverlayBuilder partyGrid = GuiOverlayBuilder.empty();
        if (!playersInParty.isEmpty())
        {
            //sort these players
            List<NetworkPlayerInfo> sortedPlayersInParty = playersInParty.stream().sorted(this.partyComparator).collect(Collectors.toList());

            int rowCount = sortedPlayersInParty.size() + 1;
            GuiOverlayElementGrid tempGrid = GuiOverlayBuilder.grid(new Vector2i(10, rowCount));

            //first row with titles
            tempGrid.setRowBuilders(0,
                    GuiOverlayBuilder.empty(),
                    GuiOverlayBuilder.empty(),
                    GuiOverlayBuilder.empty(),
                    GuiOverlayBuilder.horizontalList().addBuilder(GuiOverlayBuilder.text(new ChatComponentText("FKDR")))
                            .addBuilder(GuiOverlayBuilder.spacing(new Vector2i(2, 0))),
                    GuiOverlayBuilder.horizontalList().addBuilder(GuiOverlayBuilder.text(new ChatComponentText("WLR")))
                            .addBuilder(GuiOverlayBuilder.spacing(new Vector2i(2, 0))),
                    GuiOverlayBuilder.horizontalList().addBuilder(GuiOverlayBuilder.text(new ChatComponentText("WS")))
                            .addBuilder(GuiOverlayBuilder.spacing(new Vector2i(2, 0))),
                    GuiOverlayBuilder.horizontalList().addBuilder(GuiOverlayBuilder.text(new ChatComponentText("BBLR")))
                            .addBuilder(GuiOverlayBuilder.spacing(new Vector2i(2, 0))),
                    GuiOverlayBuilder.horizontalList().addBuilder(GuiOverlayBuilder.text(new ChatComponentText("VAL")))
                            .addBuilder(GuiOverlayBuilder.spacing(new Vector2i(2, 0))),
                    GuiOverlayBuilder.empty(),
                    GuiOverlayBuilder.empty()
            );

            for (int i = 1; i < rowCount; ++i)
            {
                NetworkPlayerInfo player = sortedPlayersInParty.get(i - 1);
                BedwarsParser.BedwarsProfile profile = this.bwParser.getPlayerDataMap().get(player.getGameProfile().getId()).getSecond();
                Tuple<String, Integer> backgroundColorPlayer = this.getPlayerName(player, profile);
                tempGrid.setRowBuilders(i,
                        GuiOverlayBuilder.fill(backgroundColorPlayer.getSecond(), 0).setBuilder(GuiOverlayBuilder.horizontalList()
                                .addBuilder(GuiOverlayBuilder.spacing(new Vector2i(1, 0)))
                                .addBuilder(GuiOverlayBuilder.text(() -> new ChatComponentText(profile.level == -1 || profile.level == -2 ? " [?✫] " : (" [" + profile.level + "✫] "))))
                                .addBuilder(GuiOverlayBuilder.spacing(new Vector2i(1, 0)))
                        ),
                        GuiOverlayBuilder.fill(backgroundColorPlayer.getSecond(), 0).setBuilder(GuiOverlayBuilder.horizontalList()
                                .addBuilder(GuiOverlayBuilder.stacked()
                                        .addBuilder(GuiOverlayBuilder.texturedRect(player.getLocationSkin(), cached8x8, cached8x8, cached8x8, new Vector2i(64, 64)))
                                        .addBuilder(GuiOverlayBuilder.texturedRect(player.getLocationSkin(), new Vector2i(40, 8), cached8x8, cached8x8, new Vector2i(64, 64))))
                                .addBuilder(GuiOverlayBuilder.spacing(new Vector2i(1, 0)))),
                        GuiOverlayBuilder.fill(backgroundColorPlayer.getSecond(), 0).setBuilder(GuiOverlayBuilder.horizontalList()
                                .addBuilder(GuiOverlayBuilder.text(() -> {
                                    if (player.getGameType() == WorldSettings.GameType.SPECTATOR) return new ChatComponentText(EnumChatFormatting.GRAY + (EnumChatFormatting.ITALIC + backgroundColorPlayer.getFirst()));
                                    return new ChatComponentText(backgroundColorPlayer.getFirst());
                                })).addBuilder(GuiOverlayBuilder.spacing(new Vector2i(5, 0)))),
                        GuiOverlayBuilder.fill(backgroundColorPlayer.getSecond(), 0).setBuilder(GuiOverlayBuilder.horizontalList().addBuilder(GuiOverlayBuilder.text(() -> new ChatComponentText(profile.fkdr == -3 || profile.fkdr == -2 ? "?" : profile.fkdr == -1 ? "NaN" : this.df.format(profile.fkdr))))
                                .addBuilder(GuiOverlayBuilder.spacing(new Vector2i(3, 0)))),
                        GuiOverlayBuilder.fill(backgroundColorPlayer.getSecond(), 0).setBuilder(GuiOverlayBuilder.horizontalList().addBuilder(GuiOverlayBuilder.text(() -> new ChatComponentText(profile.wlr == -3 || profile.wlr == -2 ? "?" : profile.wlr == -1 ? "NaN" : this.df.format(profile.wlr))))
                                .addBuilder(GuiOverlayBuilder.spacing(new Vector2i(3, 0)))),
                        GuiOverlayBuilder.fill(backgroundColorPlayer.getSecond(), 0).setBuilder(GuiOverlayBuilder.horizontalList().addBuilder(GuiOverlayBuilder.text(() -> new ChatComponentText(profile.winstreak == -2 || profile.winstreak == -1 ? "?" : String.valueOf(profile.winstreak))))
                                .addBuilder(GuiOverlayBuilder.spacing(new Vector2i(3, 0)))),
                        GuiOverlayBuilder.fill(backgroundColorPlayer.getSecond(), 0).setBuilder(GuiOverlayBuilder.horizontalList().addBuilder(GuiOverlayBuilder.text(() -> new ChatComponentText(profile.bblr == -3 || profile.bblr == -2 ? "?" : profile.bblr == -1 ? "NaN" : this.df.format(profile.bblr))))
                                .addBuilder(GuiOverlayBuilder.spacing(new Vector2i(3, 0)))),
                        GuiOverlayBuilder.fill(backgroundColorPlayer.getSecond(), 0).setBuilder(GuiOverlayBuilder.horizontalList().addBuilder(GuiOverlayBuilder.text(() -> new ChatComponentText(this.df.format(profile.score / this.bwParser.getPlayerDataMap().get(Minecraft.getMinecraft().thePlayer.getUniqueID()).getSecond().score))))
                                .addBuilder(GuiOverlayBuilder.spacing(new Vector2i(3, 0)))),
                        GuiOverlayBuilder.empty(),
                        GuiOverlayBuilder.fill(backgroundColorPlayer.getSecond(), 0).setBuilder(GuiOverlayBuilder.horizontalList()
                                .addBuilder(GuiOverlayBuilder.text(() -> {
                                    int ping = player.getResponseTime();
                                    return new ChatComponentText((ping > 600 ? EnumChatFormatting.DARK_RED.toString() : ping > 300 ? EnumChatFormatting.RED.toString() : ping > 150 ? EnumChatFormatting.DARK_GREEN.toString() : EnumChatFormatting.GREEN.toString()) + (player.getResponseTime() == 0 ? "?" : player.getResponseTime()));
                                }))
                                .addBuilder(GuiOverlayBuilder.spacing(new Vector2i(1, 0))))
                );

                if (objective != null && player.getGameType() != WorldSettings.GameType.SPECTATOR)
                {
                    int score = objective.getScoreboard().getValueFromObjective(player.getGameProfile().getName(), objective).getScorePoints();
                    tempGrid.setBuilder(i, 8, GuiOverlayBuilder.fill(backgroundColorPlayer.getSecond(), 0).setBuilder(GuiOverlayBuilder.horizontalList()
                            .addBuilder(GuiOverlayBuilder.text(() -> new ChatComponentText(EnumChatFormatting.YELLOW + String.valueOf(score))))
                            .addBuilder(GuiOverlayBuilder.spacing(new Vector2i(1, 0)))));
                }

                if (i < sortedPlayersInParty.size())
                {
                    tempGrid.setRowSpacing(i, 1);
                }
            }
            partyGrid = GuiOverlayBuilder.centerHorizontal().setBuilder(
                    GuiOverlayBuilder.verticalList()
                            .addBuilder(GuiOverlayBuilder.spacing(new Vector2i(0, 5)))
                            .addBuilder(GuiOverlayBuilder.centerHorizontal().setBuilder(GuiOverlayBuilder.text(new ChatComponentText(EnumChatFormatting.GOLD + "== PARTY =="))))
                            .addBuilder(GuiOverlayBuilder.spacing(new Vector2i(0, 1)))
                            .addBuilder(tempGrid));
        }

        contentWrapper.setBuilder(GuiOverlayBuilder.verticalList()
                .addBuilder(gameGrid)
                .addBuilder(partyGrid)
        );

        return rootBuilder.build();
    }

    /**
     * Returns the playername but with certain suffixes appended when available
     * (e.g. username {@literal (NICK)})
     *
     * @param networkPlayerInfoIn the {@link NetworkPlayerInfo} to identify the player
     * @return a {@link Tuple} containing the username with suffix (first) and a possible color when necessary (second)
     */
    public Tuple<String, Integer> getPlayerName(NetworkPlayerInfo networkPlayerInfoIn, BedwarsParser.BedwarsProfile profile)
    {
        String suffix = "";
        int color = 553648127;
        if (profile != null) {
            if (profile.fkdr == -3)
            {
                suffix = " (NICK)";
                color = new Color(255, 13, 13, 89).getRGB();
            }
        }
        /*if (HypixelStatsOverlayMod.partyManager.getPartyMembers().containsKey(networkPlayerInfoIn.getGameProfile().getName()))
        {
            //color = new Color(255, 255, 255, 153).getRGB();
        }*/
        return new Tuple<>(networkPlayerInfoIn.getDisplayName() != null ? networkPlayerInfoIn.getDisplayName().getFormattedText() + suffix : ScorePlayerTeam.formatPlayerName(networkPlayerInfoIn.getPlayerTeam(), networkPlayerInfoIn.getGameProfile().getName() + suffix), color);
    }

    static class BedwarsComparator implements Comparator<NetworkPlayerInfo>
    {
        private final BedwarsParser bwParser;

        private BedwarsComparator(BedwarsParser bwParser) { this.bwParser = bwParser; }

        @Override
        public int compare(NetworkPlayerInfo o1, NetworkPlayerInfo o2) {
            BedwarsParser.BedwarsProfile bwProfile1 = this.bwParser.getPlayerDataMap().get(o1.getGameProfile().getId()).getSecond();
            BedwarsParser.BedwarsProfile bwProfile2 = this.bwParser.getPlayerDataMap().get(o2.getGameProfile().getId()).getSecond();
            ScorePlayerTeam scoreplayerteam = o1.getPlayerTeam();
            ScorePlayerTeam scoreplayerteam1 = o2.getPlayerTeam();
            return ComparisonChain.start()
                    .compareTrueFirst(o1.getGameType() != WorldSettings.GameType.SPECTATOR, o2.getGameType() != WorldSettings.GameType.SPECTATOR)
                    .compareTrueFirst(bwProfile1.fkdr == -3, bwProfile2.fkdr == -3) // then get the nicked players
                    .compare(scoreplayerteam != null ? scoreplayerteam.getRegisteredName() : "", scoreplayerteam1 != null ? scoreplayerteam1.getRegisteredName() : "")
                    .compare(bwProfile2.score, bwProfile1.score)
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
            BedwarsParser.BedwarsProfile bwProfile1 = this.bwParser.getPlayerDataMap().get(o1.getGameProfile().getId()).getSecond();
            BedwarsParser.BedwarsProfile bwProfile2 = this.bwParser.getPlayerDataMap().get(o2.getGameProfile().getId()).getSecond();
            return ComparisonChain.start()
                    .compareTrueFirst(HypixelStatsOverlayMod.partyManager.getPartyLeader().getSecond().equals(o1.getGameProfile().getId()), HypixelStatsOverlayMod.partyManager.getPartyLeader().getSecond().equals(o2.getGameProfile().getId()))
                    .compareTrueFirst(o1.getGameType() != WorldSettings.GameType.SPECTATOR, o2.getGameType() != WorldSettings.GameType.SPECTATOR)
                    .compareTrueFirst(bwProfile1.fkdr == -3, bwProfile2.fkdr == -3) // then get the nicked players
                    .compare(bwProfile2.score, bwProfile1.score)
                    .compare(o1.getGameProfile().getName(), o2.getGameProfile().getName())
                    .result();
        }
    }
}
