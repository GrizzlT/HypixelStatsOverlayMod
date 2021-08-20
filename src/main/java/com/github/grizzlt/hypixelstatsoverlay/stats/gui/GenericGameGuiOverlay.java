package com.github.grizzlt.hypixelstatsoverlay.stats.gui;

import com.github.grizzlt.hypixelstatsoverlay.HypixelStatsOverlayMod;
import com.github.grizzlt.hypixelstatsoverlay.mixin.GuiPlayerTabOverlayMixin;
import com.github.grizzlt.hypixelstatsoverlay.overlay.GuiOverlayElementRow;
import com.github.grizzlt.hypixelstatsoverlay.overlay.IGuiOverlayComponent;
import com.github.grizzlt.hypixelstatsoverlay.overlay.builder.TabGui;
import com.github.grizzlt.hypixelstatsoverlay.overlay.grid.GuiOverlayElementGrid;
import com.github.grizzlt.hypixelstatsoverlay.util.GuiUtil;
import com.github.grizzlt.hypixelstatsoverlay.util.Vector2i;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.WorldSettings;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class GenericGameGuiOverlay<T extends IPlayerGameData>
{
    protected final List<IGuiColumnDefault> DEFAULT_ELEMENTS = Lists.newArrayList(
            (row, playerInfo, objective) -> row.withChild(TabGui.stacked()
                    .withChild(TabGui.texturedRect(playerInfo.getLocationSkin(), GuiUtil.vector8x8, GuiUtil.vector8x8, GuiUtil.vector8x8, new Vector2i(64, 64)))
                    .withChild(TabGui.texturedRect(playerInfo.getLocationSkin(), new Vector2i(40, 8), GuiUtil.vector8x8, GuiUtil.vector8x8, new Vector2i(64, 64))))
                    .withChild(TabGui.spacing().withWidth(1)),
            (row, playerInfo, scoreObjective) -> row.withChild(TabGui.text(() -> {
                        String playerDisplayName = getPlayerDisplayName(playerInfo);
                        if (playerInfo.getGameType() == WorldSettings.GameType.SPECTATOR) return new ChatComponentText(EnumChatFormatting.GRAY + (EnumChatFormatting.ITALIC + playerDisplayName));
                        return new ChatComponentText(playerDisplayName);
                    })).withChild(TabGui.spacing().withWidth(5)),
            (row, playerInfo, scoreObjective) -> row
                    .withChild(TabGui.spacing().withWidth(3))
                    .withChild(TabGui.text(() -> {
                        if (scoreObjective == null || playerInfo.getGameType() == WorldSettings.GameType.SPECTATOR) return new ChatComponentText("");
                        int score = scoreObjective.getScoreboard().getValueFromObjective(playerInfo.getGameProfile().getName(), scoreObjective).getScorePoints();
                        return new ChatComponentText(EnumChatFormatting.YELLOW + String.valueOf(score));
                    })).withChild(TabGui.spacing().withWidth(4)),
            (row, playerInfo, scoreObjective) -> row
                    .withChild(TabGui.text(() -> {
                        int ping = playerInfo.getResponseTime();
                        return new ChatComponentText((ping > 600 ? EnumChatFormatting.DARK_RED.toString() : ping > 300 ? EnumChatFormatting.RED.toString() : ping > 150 ? EnumChatFormatting.DARK_GREEN.toString() : EnumChatFormatting.GREEN.toString()) + (playerInfo.getResponseTime() == 0 ? "?" : playerInfo.getResponseTime()));
                    })).withChild(TabGui.spacing().withWidth(2))
    );

    //cached variable
    protected IGuiOverlayComponent root;
    protected IGuiOverlayComponent content = TabGui.EMPTY;
    protected boolean needsUpdate = true;

    public GenericGameGuiOverlay()
    {
        this.buildGuiSkeleton();
    }

    protected abstract T getGameDataForPlayer(UUID playerId);

    protected abstract List<NetworkPlayerInfo> sortNormalPlayers(List<NetworkPlayerInfo> players);

    protected abstract List<NetworkPlayerInfo> sortPartyPlayers(List<NetworkPlayerInfo> players);

    protected abstract List<GuiLayoutType> getStatsLayout();

    protected abstract List<IChatComponent> getTitles();

    protected abstract List<Function<T, IChatComponent>> getStats();

    protected abstract boolean isDangerous(T data);

    protected void buildGuiSkeleton()
    {
        root = TabGui.centerHorizontal().withChild(TabGui.background(Integer.MIN_VALUE, 1)
                .withChild(TabGui.verticalList()
                        .withChild(TabGui.centerHorizontal(TabGui.text(() -> ((GuiPlayerTabOverlayMixin) Minecraft.getMinecraft().ingameGUI.getTabList()).getHeader())))
                        .withChild(TabGui.spacing().withHeight(2))
                        .withChild(TabGui.centerHorizontal(TabGui.wrapper().withChild(() -> this.content)))
                        .withChild(TabGui.spacing().withHeight(2))
                        .withChild(TabGui.centerHorizontal(TabGui.text(() -> ((GuiPlayerTabOverlayMixin)Minecraft.getMinecraft().ingameGUI.getTabList()).getFooter())))
                )
        );
    }

    public void renderPlayerList(int width, ScoreObjective scoreObjective)
    {
        if (this.needsUpdate) {
            System.out.println("Building gui!");
            this.content = buildGui(scoreObjective);
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

    protected IGuiOverlayComponent buildGui(ScoreObjective objective)
    {
        List<NetworkPlayerInfo> playersInGame;
        List<NetworkPlayerInfo> playersInParty;
        Map<String, UUID> partyMembers = HypixelStatsOverlayMod.instance.getPartyManager().getPartyMembers();
        Map<Boolean, List<NetworkPlayerInfo>> playerLists = Minecraft.getMinecraft().getNetHandler().getPlayerInfoMap().stream()
                .collect(Collectors.partitioningBy(info -> partyMembers.containsValue(info.getGameProfile().getId())));
        playersInGame = playerLists.get(false);
        playersInParty = playerLists.get(true);

        List<GuiLayoutType> layoutType = getStatsLayout();
        IGuiOverlayComponent gameGrid = TabGui.EMPTY;
        if (!playersInGame.isEmpty())
        {
            List<NetworkPlayerInfo> sortedPlayersInGame = sortNormalPlayers(playersInGame);

            int rowCount = sortedPlayersInGame.size() + 1;
            GuiOverlayElementGrid tempGrid = TabGui.grid(new Vector2i(layoutType.size(), rowCount));

            //first row with titles
            this.createTitles(tempGrid, layoutType);

            for (int i = 1; i < rowCount; ++i)
            {
                NetworkPlayerInfo player = sortedPlayersInGame.get(i - 1);
                this.generatePlayerInformationRow(player, objective, tempGrid, i, layoutType);

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
            List<NetworkPlayerInfo> sortedPlayersInParty = sortPartyPlayers(playersInParty);

            int rowCount = sortedPlayersInParty.size() + 1;
            GuiOverlayElementGrid tempGrid = TabGui.grid(new Vector2i(layoutType.size(), rowCount));

            //first row with titles
            createTitles(tempGrid, layoutType);

            for (int i = 1; i < rowCount; ++i)
            {
                NetworkPlayerInfo player = sortedPlayersInParty.get(i - 1);
                this.generatePlayerInformationRow(player, objective, tempGrid, i, layoutType);

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

    protected void createTitles(GuiOverlayElementGrid grid, List<GuiLayoutType> layoutType)
    {
        Iterator<IChatComponent> titleIterator = this.getTitles().iterator();
        for (int i = 0; i < layoutType.size(); ++i) {
            if (layoutType.get(i) == GuiLayoutType.CUSTOM) {
                IChatComponent title = titleIterator.next();
                if (title != null) {
                    grid.withChild(0, i, TabGui.horizontalList().withChild(TabGui.text(title))
                            .withChild(TabGui.spacing().withWidth(2)));
                } else {
                    grid.withChild(0, i, TabGui.EMPTY);
                }
            } else {
                grid.withChild(0, i, TabGui.EMPTY);
            }
        }
    }

    protected void generatePlayerInformationRow(NetworkPlayerInfo playerInfo, ScoreObjective objective, GuiOverlayElementGrid grid, int row, List<GuiLayoutType> guiLayout)
    {
        UUID playerId = playerInfo.getGameProfile().getId();
        Callable<Integer> backgroundColor = () -> getPlayerNameColor(getGameDataForPlayer(playerId));

        Iterator<IGuiColumnDefault> defaultIterator = DEFAULT_ELEMENTS.iterator();
        Iterator<Function<T, IChatComponent>> statIterator = getStats().iterator();
        for (int i = 0; i < guiLayout.size(); ++i) {
            if (guiLayout.get(i) == GuiLayoutType.CUSTOM) {
                Function<T, IChatComponent> stat = statIterator.next();
                grid.withChild(row, i, TabGui.fill(backgroundColor).withChild(
                        TabGui.horizontalList()
                                .withChild(TabGui.spacing().withWidth(2))
                                .withChild(TabGui.text(() -> stat.apply(getGameDataForPlayer(playerId))))
                                .withChild(TabGui.spacing().withWidth(2))));
            } else {
                grid.withChild(row, i, TabGui.fill(backgroundColor).withChild(
                        defaultIterator.next().onBuildGui(TabGui.horizontalList(), playerInfo, objective)
                ));
            }
        }
    }

    public String getPlayerDisplayName(NetworkPlayerInfo playerInfo)
    {
        T data = this.getGameDataForPlayer(playerInfo.getGameProfile().getId());
        if (data != null && data.isNicked()) {
            return getPlayerName(playerInfo) + " (NICK)";
        }
        return getPlayerName(playerInfo);
    }

    protected String getPlayerName(NetworkPlayerInfo info)
    {
        return info.getDisplayName() != null ? info.getDisplayName().getFormattedText() : ScorePlayerTeam.formatPlayerName(info.getPlayerTeam(), info.getGameProfile().getName());
    }

    public int getPlayerNameColor(T playerData)
    {
        if (playerData.isNicked())
            return GuiUtil.rbgToInt(255, 13, 13, 89);
        if (this.isDangerous(playerData))
            return GuiUtil.rbgToInt(255, 255, 2, 89);
        return 553648127;
    }

    public void markDirty()
    {
        this.needsUpdate = true;
    }

    public enum GuiLayoutType
    {
        MINECRAFT,
        CUSTOM
    }

    private interface IGuiColumnDefault
    {
        IGuiOverlayComponent onBuildGui(GuiOverlayElementRow row, NetworkPlayerInfo playerInfo, ScoreObjective scoreObjective);
    }
}
