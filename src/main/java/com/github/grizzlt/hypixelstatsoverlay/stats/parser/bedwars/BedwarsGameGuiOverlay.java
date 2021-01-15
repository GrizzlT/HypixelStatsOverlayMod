package com.github.grizzlt.hypixelstatsoverlay.stats.parser.bedwars;

import com.github.grizzlt.hypixelstatsoverlay.HypixelStatsOverlayMod;
import com.github.grizzlt.hypixelstatsoverlay.overlay.builder.BuilderWrapper;
import com.github.grizzlt.hypixelstatsoverlay.overlay.builder.GuiOverlayBuilder;
import com.github.grizzlt.hypixelstatsoverlay.overlay.grid.GuiOverlayElementGrid;
import com.github.grizzlt.hypixelstatsoverlay.util.PartyManager;
import com.github.grizzlt.hypixelstatsoverlay.util.ReflectionContainer;
import com.github.grizzlt.hypixelstatsoverlay.util.Vector2i;
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
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class BedwarsGameGuiOverlay
{
    /**
     * the instance of the {@link BedwarsParser}
     */
    private final BedwarsParser bwParser;

    //cached variable
    private final DecimalFormat df;

    private GuiOverlayBuilder rootBuilder;
    private BuilderWrapper contentWrapper = GuiOverlayBuilder.wrapper();

    //cache
    private Vector2i cached8x8 = new Vector2i(8, 8);

    public BedwarsGameGuiOverlay(BedwarsParser parser)
    {
        this.bwParser = parser;

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
        if (true)
        {
            Collection<NetworkPlayerInfo> playersInTabList = Minecraft.getMinecraft().getNetHandler().getPlayerInfoMap();
            //get their stats when not already done so
            this.bwParser.gatherPlayers(playersInTabList);
            //sort these players
            List<NetworkPlayerInfo> sortedPlayers = playersInTabList.stream().sorted(this.bwParser.getBwComparator()).collect(Collectors.toList());

            //GuiOverlayElementList nameList = GuiOverlayBuilder.verticalList();
            int columnCount = sortedPlayers.size() + 1;
            GuiOverlayElementGrid grid = GuiOverlayBuilder.grid(new Vector2i(9, columnCount));

            //first row with titles
            grid.setRowBuilders(0,
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
                    GuiOverlayBuilder.empty(),
                    GuiOverlayBuilder.empty()
            );

            for (int i = 1; i < columnCount; ++i)
            {
                NetworkPlayerInfo player = sortedPlayers.get(i - 1);
                BedwarsParser.BedwarsProfile profile = this.bwParser.getPlayerDataMap().get(player.getGameProfile().getId()).getSecond();
                Tuple<String, Integer> backgroundColorPlayer = this.getPlayerName(player, profile);
                grid.setRowBuilders(i,
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
                        GuiOverlayBuilder.empty(),
                        GuiOverlayBuilder.fill(backgroundColorPlayer.getSecond(), 0).setBuilder(GuiOverlayBuilder.horizontalList()
                                .addBuilder(GuiOverlayBuilder.text(() -> {
                                    int ping = player.getResponseTime();
                                    return new ChatComponentText((ping > 600 ? EnumChatFormatting.DARK_RED.toString() : ping > 300 ? EnumChatFormatting.RED.toString() : ping > 150 ? EnumChatFormatting.DARK_GREEN.toString() : EnumChatFormatting.GREEN.toString()) + (player.getResponseTime() == 0 ? "?" : player.getResponseTime()));
                                }))
                                .addBuilder(GuiOverlayBuilder.spacing(new Vector2i(1, 0))))
                );

                if (scoreObjective != null && player.getGameType() != WorldSettings.GameType.SPECTATOR)
                {
                    int score = scoreObjective.getScoreboard().getValueFromObjective(player.getGameProfile().getName(), scoreObjective).getScorePoints();
                    grid.setBuilder(i, 7, GuiOverlayBuilder.fill(backgroundColorPlayer.getSecond(), 0).setBuilder(GuiOverlayBuilder.horizontalList()
                            .addBuilder(GuiOverlayBuilder.text(() -> new ChatComponentText(EnumChatFormatting.YELLOW + String.valueOf(score))))
                            .addBuilder(GuiOverlayBuilder.spacing(new Vector2i(1, 0)))));
                }

                if (i < sortedPlayers.size() - 1)
                {
                    grid.setRowSpacing(i, 1);
                }
            }

            contentWrapper.setBuilder(grid);

            try
            {
                rootBuilder.build().draw(new Vector2i(25, 10), new Vector2i(width - 50, 0));
            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * Returns the playername but with certain suffixes appended when available
     * (e.g. username {@literal (NICK/HAX#4/SNIPE)})
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
                color = new Color(249, 255, 85, 153).getRGB();
            }
        }
        if (HypixelStatsOverlayMod.partyManager.getPartyMembers().containsKey(networkPlayerInfoIn.getGameProfile().getName()))
        {
            color = new Color(255, 255, 255, 153).getRGB();
        }
        return new Tuple<>(networkPlayerInfoIn.getDisplayName() != null ? networkPlayerInfoIn.getDisplayName().getFormattedText() + suffix : ScorePlayerTeam.formatPlayerName(networkPlayerInfoIn.getPlayerTeam(), networkPlayerInfoIn.getGameProfile().getName() + suffix), color);
    }
}
