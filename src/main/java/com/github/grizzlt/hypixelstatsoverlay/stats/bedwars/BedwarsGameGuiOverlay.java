package com.github.grizzlt.hypixelstatsoverlay.stats.bedwars;

import com.github.grizzlt.hypixelstatsoverlay.HypixelStatsOverlayMod;
import com.github.grizzlt.hypixelstatsoverlay.stats.gui.GenericGameGuiOverlay;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.WorldSettings;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BedwarsGameGuiOverlay extends GenericGameGuiOverlay<BedwarsParser.BedwarsProfile>
{
    private static final List<GuiLayoutType> LAYOUT = Lists.newArrayList(
            GuiLayoutType.CUSTOM,
            GuiLayoutType.MINECRAFT,
            GuiLayoutType.MINECRAFT,
            GuiLayoutType.CUSTOM,
            GuiLayoutType.CUSTOM,
            GuiLayoutType.CUSTOM,
            GuiLayoutType.CUSTOM,
            GuiLayoutType.CUSTOM,
            GuiLayoutType.MINECRAFT,
            GuiLayoutType.MINECRAFT
    );
    private static final List<IChatComponent> TITLES = Lists.newArrayList(
            null,
            new ChatComponentText("FKDR"),
            new ChatComponentText("WLR"),
            new ChatComponentText("WS"),
            new ChatComponentText("BBLR"),
            new ChatComponentText("VAL")
    );
    private final List<Function<BedwarsParser.BedwarsProfile, IChatComponent>> STATS = Lists.newArrayList(
            profile -> new ChatComponentText(profile.level == -1 ? "[?✫]" : ("[" + profile.level + "✫]")),
            profile -> new ChatComponentText(profile.fkdr < -1.8 ? "?" : profile.fkdr < -0.8 ? "NaN" : this.df.format(profile.fkdr)),
            profile -> new ChatComponentText(profile.wlr < -1.8 ? "?" : profile.wlr < -0.8 ? "NaN" : this.df.format(profile.wlr)),
            profile -> new ChatComponentText(profile.winstreak == -1 ? "?" : String.valueOf(profile.winstreak)),
            profile -> new ChatComponentText(profile.bblr < -1.8 ? "?" : profile.bblr < -0.8 ? "NaN" : this.df.format(profile.bblr)),
            profile -> new ChatComponentText(profile.isNicked ? "?" : this.df.format(this.getRelativeScore(profile)))
    );

    private final BedwarsParser bwParser;
    protected final DecimalFormat df = new DecimalFormat("0.0#");

    public BedwarsGameGuiOverlay(BedwarsParser bwParser)
    {
        this.bwParser = bwParser;
        this.df.setRoundingMode(RoundingMode.HALF_UP);
    }

    @Override
    protected BedwarsParser.BedwarsProfile getGameDataForPlayer(UUID playerId)
    {
        return bwParser.getPlayerProfile(playerId);
    }

    @Override
    protected List<NetworkPlayerInfo> sortNormalPlayers(List<NetworkPlayerInfo> players)
    {
        return players.stream().sorted((o1, o2) -> {
            BedwarsParser.BedwarsProfile bwProfile1 = this.getGameDataForPlayer(o1.getGameProfile().getId());
            BedwarsParser.BedwarsProfile bwProfile2 = this.getGameDataForPlayer(o2.getGameProfile().getId());

            return ComparisonChain.start()
                    .compareTrueFirst(o1.getGameType() != WorldSettings.GameType.SPECTATOR, o2.getGameType() != WorldSettings.GameType.SPECTATOR)
                    .compareTrueFirst(bwProfile1.isNicked(), bwProfile2.isNicked())
                    .compare(o1.getPlayerTeam() != null ? o1.getPlayerTeam().getRegisteredName() : "", o2.getPlayerTeam() != null ? o2.getPlayerTeam().getRegisteredName() : "")
                    .compare(bwProfile2.score, bwProfile1.score)
                    .compare(bwProfile2.fkdr, bwProfile1.fkdr)
                    .compare(o1.getGameProfile().getName(), o2.getGameProfile().getName())
                    .result();
        }).collect(Collectors.toList());
    }

    @Override
    protected List<NetworkPlayerInfo> sortPartyPlayers(List<NetworkPlayerInfo> players)
    {
        return players.stream().sorted((o1, o2) -> {
            BedwarsParser.BedwarsProfile bwProfile1 = this.bwParser.getPlayerProfile(o1.getGameProfile().getId());
            BedwarsParser.BedwarsProfile bwProfile2 = this.bwParser.getPlayerProfile(o2.getGameProfile().getId());

            return ComparisonChain.start()
                    .compareTrueFirst(HypixelStatsOverlayMod.instance.getPartyManager().getPartyLeader().getSecond().equals(o1.getGameProfile().getId()), HypixelStatsOverlayMod.instance.getPartyManager().getPartyLeader().getSecond().equals(o2.getGameProfile().getId()))
                    .compareTrueFirst(o1.getGameType() != WorldSettings.GameType.SPECTATOR, o2.getGameType() != WorldSettings.GameType.SPECTATOR)
                    .compareTrueFirst(bwProfile1.isNicked, bwProfile2.isNicked) // then get the nicked players
                    .compare(bwProfile2.score, bwProfile1.score)
                    .compare(bwProfile2.fkdr, bwProfile1.fkdr)
                    .compare(o1.getGameProfile().getName(), o2.getGameProfile().getName())
                    .result();
        }).collect(Collectors.toList());
    }

    @Override
    protected List<GuiLayoutType> getStatsLayout()
    {
        return LAYOUT;
    }

    @Override
    protected List<IChatComponent> getTitles()
    {
        return TITLES;
    }

    @Override
    protected List<Function<BedwarsParser.BedwarsProfile, IChatComponent>> getStats()
    {
        return STATS;
    }

    @Override
    protected boolean isDangerous(BedwarsParser.BedwarsProfile data)
    {
        return getRelativeScore(data) > 1.2;
    }

    private double getRelativeScore(BedwarsParser.BedwarsProfile data) {
        return data.score / this.getGameDataForPlayer(Minecraft.getMinecraft().thePlayer.getUniqueID()).score;
    }
}
