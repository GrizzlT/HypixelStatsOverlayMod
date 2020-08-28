package com.github.ThomasVDP.hypixelmod.statsoverlay.stats.parser.bedwars;

import com.github.ThomasVDP.hypixelmod.statsoverlay.HypixelStatsOverlayMod;
import net.labymod.main.LabyMod;
import net.labymod.user.User;
import net.labymod.user.UserManager;
import net.labymod.user.group.LabyGroup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.scoreboard.IScoreObjectiveCriteria;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Tuple;
import net.minecraft.world.WorldSettings;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.lang.reflect.Field;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class BedwarsGuiTabListOverlay
{
    /**
     * the instance of the {@link BedwarsParser}
     */
    private final BedwarsParser bwParser;

    /**
     * whether the user is using LabyMod
     */
    private boolean isUsingLaby = false;

    //minecraft code copy
    private long lastTimeOpened;
    private boolean isBeingRendered;
    protected float zLevel;

    //cached variable
    private final DecimalFormat df;

    /**
     * Reflection fields
     */
    private final Field headerField;
    private final Field footerField;
    private final Field guiTabOverlayField;

    public BedwarsGuiTabListOverlay(BedwarsParser parser)
    {
        this.bwParser = parser;

        this.df = new DecimalFormat("0.0#");
        this.df.setRoundingMode(RoundingMode.HALF_UP);

        Class<?> guiPlayerTabOverlayClass;
        Class<?> guiIngameClass;
        try {
            Class<?> labyMainClazz = Class.forName("net.labymod.main.LabyMod");
            this.isUsingLaby = true;
            //labymod active
            guiIngameClass = Class.forName("net.labymod.core_implementation.mc18.gui.GuiIngameCustom");
            guiPlayerTabOverlayClass = Class.forName("net.labymod.core_implementation.mc18.gui.ModPlayerTabOverlay");
        } catch (ClassNotFoundException e) {
            //e.printStackTrace();
            //no labymod active
            guiIngameClass = GuiIngame.class;
            guiPlayerTabOverlayClass = GuiPlayerTabOverlay.class;
        }
        this.headerField = ReflectionHelper.findField(guiPlayerTabOverlayClass, "header", "field_175256_i");
        this.footerField = ReflectionHelper.findField(guiPlayerTabOverlayClass, "footer", "field_175255_h");
        this.guiTabOverlayField = ReflectionHelper.findField(guiIngameClass, "overlayPlayerList", "field_175196_v");
    }

    public void renderPlayerList(int width, Scoreboard scoreboard, ScoreObjective scoreObjective)
    {
        //******************//
        // CALCULATE WIDTHS //
        //******************//

        //gather all players currently in tablist
        Collection<NetworkPlayerInfo> playersInTabList = Minecraft.getMinecraft().getNetHandler().getPlayerInfoMap();
        //get their stats when not already done so
        this.bwParser.gatherPlayers(playersInTabList);
        //sort these players
        List<NetworkPlayerInfo> sortedPlayers = playersInTabList.stream()
                .sorted(this.bwParser.getBwComparator()).collect(Collectors.toList());

        /*
        Calculate the maximum width a playername can have (this includes tags such as nicked)
        Calculate the maximum width a player's score can have
         */
        int maxNameAndTagsWidth = 0;
        int maxScoreBoardWidth = 0;
        for (NetworkPlayerInfo playerInfo : sortedPlayers)
        {
            int len = Minecraft.getMinecraft().fontRendererObj.getStringWidth(this.getPlayerName(playerInfo).getFirst());
            maxNameAndTagsWidth = Math.max(len, maxNameAndTagsWidth);
            if (scoreObjective != null) {
                if (scoreObjective.getRenderType() == IScoreObjectiveCriteria.EnumRenderType.HEARTS) {
                    maxScoreBoardWidth = 90;
                } else {
                    len = Minecraft.getMinecraft().fontRendererObj.getStringWidth(" " + scoreboard.getValueFromObjective(playerInfo.getGameProfile().getName(), scoreObjective).getScorePoints());
                    maxScoreBoardWidth = Math.max(len, maxScoreBoardWidth);
                }
            }
        }

        //calculate the widths for the stats texts (divided by two as they will be rendered at half scale)
        int maxFDKRWidth = Minecraft.getMinecraft().fontRendererObj.getStringWidth("FKDR") / 2;
        int maxWLRWidth = Minecraft.getMinecraft().fontRendererObj.getStringWidth("WLR") / 2;
        int maxWSWidth = Minecraft.getMinecraft().fontRendererObj.getStringWidth("WS") / 2;
        int maxBBLRWidth = Minecraft.getMinecraft().fontRendererObj.getStringWidth("BBLR") / 2;
        int maxLevelWidth = 0;
        //go through each player and check if their stat's width is bigger than the text divided by two (text will be rendered twice as small)
        for (NetworkPlayerInfo playerInfo : sortedPlayers)
        {
            UUID playerId = playerInfo.getGameProfile().getId();
            if (!this.bwParser.getPlayerDataMap().containsKey(playerId)) {
                this.bwParser.gatherPlayers(sortedPlayers);
            }
            double fkdr = this.bwParser.getPlayerDataMap().get(playerId).getSecond().fkdr;
            double wlr = this.bwParser.getPlayerDataMap().get(playerId).getSecond().wlr;
            int ws = this.bwParser.getPlayerDataMap().get(playerId).getSecond().winstreak;
            double bblr = this.bwParser.getPlayerDataMap().get(playerId).getSecond().bblr;
            int bwLevel = this.bwParser.getPlayerDataMap().get(playerId).getSecond().level;
            String fkdrStr = fkdr == -3 || fkdr == -2 ? "?" : fkdr == -1 ? "NaN" : this.df.format(fkdr);
            String wlrStr = wlr == -3 || wlr == -2 ? "?" : wlr == -1 ? "NaN" : this.df.format(wlr);
            String wsStr = ws == -2 || ws == -1 ? "?" : String.valueOf(ws);
            String bblrStr = bblr == -3 || bblr == -2 ? "?" : bblr == -1 ? "NaN" : this.df.format(bblr);
            String bwLevelStr = bwLevel == -1 || bwLevel == -2 ? " [?✫] " : (" [" + bwLevel + "✫] ");
            maxFDKRWidth = Math.max(maxFDKRWidth, Minecraft.getMinecraft().fontRendererObj.getStringWidth(fkdrStr));
            maxWLRWidth = Math.max(maxWLRWidth, Minecraft.getMinecraft().fontRendererObj.getStringWidth(wlrStr));
            maxWSWidth = Math.max(maxWSWidth, Minecraft.getMinecraft().fontRendererObj.getStringWidth(wsStr));
            maxBBLRWidth = Math.max(maxBBLRWidth, Minecraft.getMinecraft().fontRendererObj.getStringWidth(bblrStr));
            maxLevelWidth = Math.max(maxLevelWidth, Minecraft.getMinecraft().fontRendererObj.getStringWidth(bwLevelStr));
        }

        //get the maximum renderWidth per player!
        //this will be:
        //their level (has spacing) + their head (default 8 pixels) + 1 spacing + labymod ? 8 + 1 spacing : 0 + maxPlayerNameWidth + 5 spacing + maxFDKRWidth + 5 spacing + maxWLRWidth + 5 spacing + maxWSWidth + 5 spacing + maxBBLRWidht + 1 spacing + maxScoreBoardWidth + 13 (for ping)
        int maxPlayerRenderWidth = Math.min(maxLevelWidth + 9 + (isUsingLaby ? 9 : 0) + maxNameAndTagsWidth + 5 + maxFDKRWidth + 5 + maxWLRWidth + 5 + maxWSWidth + 5 + maxBBLRWidth + 1 + maxScoreBoardWidth + 13, width - 50);
        int playerEntryLeftPos = width / 2 - maxPlayerRenderWidth / 2;
        // start getting maximum tab list width with maxPlayerRenderWidth
        int maxWidth = maxPlayerRenderWidth;
        // get the headers and footers using reflection
        List<String> list1 = null;
        List<String> list2 = null;
        try {
            if (headerField.get(guiTabOverlayField.get(Minecraft.getMinecraft().ingameGUI)) != null) {
                list1 = Minecraft.getMinecraft().fontRendererObj.listFormattedStringToWidth(((IChatComponent)headerField.get(guiTabOverlayField.get(Minecraft.getMinecraft().ingameGUI))).getFormattedText(), width - 50);
                for (String s : list1) {
                    maxWidth = Math.max(maxWidth, Minecraft.getMinecraft().fontRendererObj.getStringWidth(s));
                }
            }

            if (footerField.get(guiTabOverlayField.get(Minecraft.getMinecraft().ingameGUI)) != null) {
                list2 = Minecraft.getMinecraft().fontRendererObj.listFormattedStringToWidth(((IChatComponent)footerField.get(guiTabOverlayField.get(Minecraft.getMinecraft().ingameGUI))).getFormattedText(), width - 50);
                for (String s : list2) {
                    maxWidth = Math.max(maxWidth, Minecraft.getMinecraft().fontRendererObj.getStringWidth(s));
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }



        //******************//
        // START OF DRAWING //
        //******************//

        //we start 10 from the top of the screen
        int marginTop = 10;

        // draw headers
        if (list1 != null) {
            //draw a dark grey transparent box that'll contain the header
            //this box is has 1 margin at all sides
            Gui.drawRect(width / 2 - maxWidth / 2 - 1, marginTop - 1, width / 2 + maxWidth / 2 + 1, marginTop + list1.size() * Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT, Integer.MIN_VALUE);

            //go through the wrapped header list
            for (String s3 : list1)
            {
                //get the width of the wrapped header
                int strWidth = Minecraft.getMinecraft().fontRendererObj.getStringWidth(s3);
                //draw the header
                Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(s3, (float)(width / 2 - strWidth / 2), (float)marginTop, -1);
                //increase the height for following elements
                marginTop += Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT;
            }
            //add 1 spacing
            ++marginTop;
        }

        //draw stat titles

        //draw a dark grey transparent box that'll contain the stat titles
        //this box has 1 margin at all sides
        Gui.drawRect(width / 2 - maxWidth / 2 - 1, marginTop - 1, width / 2 + maxWidth / 2 + 1, marginTop + Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT, Integer.MIN_VALUE);
        //scale renderer down to half-scale
        GL11.glPushMatrix();
        GlStateManager.scale(0.5d, 0.5d, 0.5d);

        //draw the titles after the playernames
        Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow("FKDR", 2 * (playerEntryLeftPos + maxLevelWidth + 9 + maxNameAndTagsWidth + 5), 2 * (marginTop + (float)Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT / 4), -1);
        Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow("WLR", 2 * (playerEntryLeftPos + maxLevelWidth + 9 + maxNameAndTagsWidth + 5 + maxFDKRWidth + 5), 2 * (marginTop + (float)Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT / 4), -1);
        Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow("WS", 2 * (playerEntryLeftPos + maxLevelWidth + 9 + maxNameAndTagsWidth + 5 + maxFDKRWidth + 5 + maxWLRWidth + 5), 2 * (marginTop + (float)Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT / 4), -1);
        Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow("BBLR", 2 * (playerEntryLeftPos + maxLevelWidth + 9 + maxNameAndTagsWidth + 5 + maxFDKRWidth + 5 + maxWLRWidth + 5 + maxWSWidth + 5), 2 * (marginTop + (float)Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT / 4), -1);

        //increase marginTop for following elements
        //add 1 spacing
        marginTop += Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT + 1;

        //remove scaling
        GL11.glPopMatrix();

        //draw playerArea

        //draw a dark grey transparent box that'll contain the players
        //this box has 1 margin at all sides
        Gui.drawRect(width / 2 - maxWidth / 2 - 1, marginTop - 1, width / 2 + maxWidth / 2 + 1, marginTop + sortedPlayers.size() * Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT, Integer.MIN_VALUE);

        //go through all players
        for (int i = 0; i < sortedPlayers.size(); ++i)
        {
            //start x at playerEntryLeftPos
            int left = playerEntryLeftPos;
            //set y at the correct entrylevel
            int top = marginTop + i * 9;

            //get the player information
            NetworkPlayerInfo playerInfo = sortedPlayers.get(i);
            Tuple<String, Integer> playerNameTuple = this.getPlayerName(playerInfo);

            //draw light_gray playerRect
            Gui.drawRect(left, top, left + maxPlayerRenderWidth, top + 8, playerNameTuple.getSecond());
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.enableAlpha();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

            if (!this.bwParser.getPlayerDataMap().containsKey(playerInfo.getGameProfile().getId())) {
                this.bwParser.gatherPlayers(sortedPlayers);
            }
            //get bwLevel of player
            int bwLevel = this.bwParser.getPlayerDataMap().get(playerInfo.getGameProfile().getId()).getSecond().level;
            String bwLevelStr = bwLevel == -1 || bwLevel == -2 ? " [?✫] " : (" [" + bwLevel + "✫] ");
            //draw bwLevel right alligned
            Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(bwLevelStr, left + maxLevelWidth - Minecraft.getMinecraft().fontRendererObj.getStringWidth(bwLevelStr), top, -1);
            //increase left for following player data
            left += maxLevelWidth;

            //draw playerHead
            //minecraft code
            EntityPlayer entityplayer = Minecraft.getMinecraft().theWorld.getPlayerEntityByUUID(playerInfo.getGameProfile().getId());
            boolean flag1 = entityplayer != null && entityplayer.isWearing(EnumPlayerModelParts.CAPE) && (playerInfo.getGameProfile().getName().equals("Dinnerbone") || playerInfo.getGameProfile().getName().equals("Grumm"));
            Minecraft.getMinecraft().getTextureManager().bindTexture(playerInfo.getLocationSkin());
            int v = 8 + (flag1 ? 8 : 0);
            int vHeight = 8 * (flag1 ? -1 : 1);
            Gui.drawScaledCustomSizeModalRect(left, top, 8.0F, (float)v, 8, vHeight, 8, 8, 64.0F, 64.0F);

            if (entityplayer != null && entityplayer.isWearing(EnumPlayerModelParts.HAT))
            {
                int v2 = 8 + (flag1 ? 8 : 0);
                int vHeight2 = 8 * (flag1 ? -1 : 1);
                Gui.drawScaledCustomSizeModalRect(left, top, 40.0F, (float)v2, 8, vHeight2, 8, 8, 64.0F, 64.0F);
            }
            //end minecraft code
            //increase left for following elements
            //add 1 spacing
            left += 9;

            //draw labymod user when possible
            if (isUsingLaby) {
                UserManager userManager = LabyMod.getInstance().getUserManager();
                User user = userManager.getUser(playerInfo.getGameProfile().getId());
                if (user.isFamiliar()) {
                    /*GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
                    GlStateManager.enableAlpha();
                    GlStateManager.enableBlend();
                    ResourceLocation texture = ModTextures.BADGE_FAMILIAR_SMALL;
                    Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
                    Gui.drawScaledCustomSizeModalRect(left, top, 0, 0, 24, 24, 8, 8, 24.0F, 24.0F);*/
                    LabyGroup group = user.getGroup();
                    if (group != null) {
                        group.renderBadge(left, top, 8.0D, 8.0D, true);
                    }
                    //LabyMod.getInstance().getDrawUtils().drawTexture(left, top, 255.0D, 255.0D, 8.0D, 8.0D, 1.1F);
                }
                left += 9;
            }

            //draw playerName
            //minecraft code
            if (playerInfo.getGameType() == WorldSettings.GameType.SPECTATOR)
            {
                String playerDisplayName = EnumChatFormatting.ITALIC + playerNameTuple.getFirst();
                Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(playerDisplayName, (float)left - (isUsingLaby ? LabyMod.getInstance().getUserManager().getUser(playerInfo.getGameProfile().getId()).isFamiliar() ? 0 : 9 : 9), (float)top, -1862270977);
            }
            else
            {
                Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(playerNameTuple.getFirst(), (float)left - (isUsingLaby ? LabyMod.getInstance().getUserManager().getUser(playerInfo.getGameProfile().getId()).isFamiliar() ? 0 : 9 : 9), (float)top, -1);
            }
            //end minecraft code
            //incrase left for following elements
            left += maxNameAndTagsWidth;

            //draw stats!!
            if (!this.bwParser.getPlayerDataMap().containsKey(playerInfo.getGameProfile().getId())) {
                this.bwParser.gatherPlayers(sortedPlayers);
            }
            double fkdr = this.bwParser.getPlayerDataMap().get(playerInfo.getGameProfile().getId()).getSecond().fkdr;
            double wlr = this.bwParser.getPlayerDataMap().get(playerInfo.getGameProfile().getId()).getSecond().wlr;
            int ws = this.bwParser.getPlayerDataMap().get(playerInfo.getGameProfile().getId()).getSecond().winstreak;
            double bblr = this.bwParser.getPlayerDataMap().get(playerInfo.getGameProfile().getId()).getSecond().bblr;
            String fkdrStr = fkdr == -3 || fkdr == -2 ? "?" : fkdr == -1 ? "NaN" : this.df.format(fkdr);
            String wlrStr = wlr == -3 || wlr == -2 ? "?" : wlr == -1 ? "NaN" : this.df.format(wlr);
            String wsStr = ws == -2 || ws == -1 ? "?" : String.valueOf(ws);
            String bblrStr = bblr == -3 || bblr == -2 ? "?" : bblr == -1 ? "NaN" : this.df.format(bblr);

            //draw each stat right alligned
            left += 5;
            Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(fkdrStr, left + maxFDKRWidth - Minecraft.getMinecraft().fontRendererObj.getStringWidth(fkdrStr), top, -1);
            left += maxFDKRWidth + 5;
            Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(wlrStr,  left + maxWLRWidth - Minecraft.getMinecraft().fontRendererObj.getStringWidth(wlrStr), top, -1);
            left += maxWLRWidth + 5;
            Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(wsStr,  left + maxWSWidth - Minecraft.getMinecraft().fontRendererObj.getStringWidth(wsStr), top, -1);
            left += maxWSWidth + 5;
            Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(bblrStr,  left + maxBBLRWidth - Minecraft.getMinecraft().fontRendererObj.getStringWidth(bblrStr), top, -1);
            left += maxBBLRWidth;

            //draw scoreboard objective!
            if (scoreObjective != null && playerInfo.getGameType() != WorldSettings.GameType.SPECTATOR)
            {
                //start of scoreboard valueWidth (add 1 spacing to left)
                int k5 = left + 1;
                //end of scoreboard valueWidth
                int l5 = k5 + maxScoreBoardWidth;

                if (l5 - k5 > 5)
                {
                    //draw scoreboard
                    this.drawScoreboardValues(scoreObjective, top, playerInfo.getGameProfile().getName(), k5, l5, playerInfo);
                }
            }

            //draw ping
            this.drawPing(maxPlayerRenderWidth, playerEntryLeftPos, top, playerInfo);
        }

        //draw footer
        if (list2 != null) {
            marginTop += sortedPlayers.size() * 9 + 1;
            Gui.drawRect(width / 2 - maxWidth / 2 - 1, marginTop - 1, width / 2 + maxWidth / 2 + 1, marginTop + list2.size() * Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT, -2147483648);

            for (String s : list2) {
                int strWidth = Minecraft.getMinecraft().fontRendererObj.getStringWidth(s);
                Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(s, (float)(width / 2 - strWidth / 2), (float)marginTop, -1);
                marginTop += Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT;
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
    public Tuple<String, Integer> getPlayerName(NetworkPlayerInfo networkPlayerInfoIn)
    {
        String suffix = "";
        int color = 553648127;
        if (this.bwParser.getPlayerDataMap().containsKey(networkPlayerInfoIn.getGameProfile().getId())) {
            boolean isNicked = this.bwParser.getPlayerDataMap().get(networkPlayerInfoIn.getGameProfile().getId()).getSecond().fkdr == -3;
            int hax = this.bwParser.getPlayerDataMap().get(networkPlayerInfoIn.getGameProfile().getId()).getSecond().hax;
            boolean isSniper = this.bwParser.getPlayerDataMap().get(networkPlayerInfoIn.getGameProfile().getId()).getSecond().sniper;
            String suffixTemp = (isNicked ? "NICK" : "") + (hax > 0 ? (isNicked ? "/" : "") + "HAX#" + hax : "") + (isSniper ? (isNicked || hax > 0 ? "/" : "") + "SNIPE" : "");
            suffix = !suffixTemp.equals("") ? " (" + suffixTemp  + ")" : "";
            color = this.getColorPrefix(networkPlayerInfoIn.getGameProfile().getId(), isNicked, hax, isSniper);
        }
        return new Tuple<>(networkPlayerInfoIn.getDisplayName() != null ? networkPlayerInfoIn.getDisplayName().getFormattedText() + suffix : ScorePlayerTeam.formatPlayerName(networkPlayerInfoIn.getPlayerTeam(), networkPlayerInfoIn.getGameProfile().getName() + suffix), color);
    }

    /**
     * Returns the background color for the playername in the playerList
     * @param playerId the id of the player
     * @param isNicked whether the player is nicked
     * @param hax whether the player has been reported for hacks
     * @param isSniper whether the player has been spotted for sniping
     * @return the background color based on params
     */
    private int getColorPrefix(UUID playerId, boolean isNicked, int hax, boolean isSniper)
    {
        if (HypixelStatsOverlayMod.partyManager.getPartyMembers().contains(Minecraft.getMinecraft().getNetHandler().getPlayerInfo(playerId).getGameProfile().getName()))
            return new Color(255, 255, 255, 164).getRGB();

        int severity = (isNicked ? 1 : 0) + (hax > 0 ? 1 : 0) + (isSniper ? 1 : 0);
        switch (severity) {
            case 1:
                return new Color(249, 255, 85, 153).getRGB();
            case 2:
                return new Color(255, 170, 0, 153).getRGB();
            case 3:
                return new Color(170, 0, 0, 153).getRGB();
            default:
                return 553648127;
        }
    }

    /**
     * Straight up copy  of minecraft code
     */
    private void drawScoreboardValues(ScoreObjective p_175247_1_, int p_175247_2_, String p_175247_3_, int p_175247_4_, int p_175247_5_, NetworkPlayerInfo p_175247_6_) {
        int i = p_175247_1_.getScoreboard().getValueFromObjective(p_175247_3_, p_175247_1_).getScorePoints();
        if (p_175247_1_.getRenderType() == IScoreObjectiveCriteria.EnumRenderType.HEARTS) {
            Minecraft.getMinecraft().getTextureManager().bindTexture(Gui.icons);
            if (this.lastTimeOpened == p_175247_6_.func_178855_p()) {
                if (i < p_175247_6_.func_178835_l()) {
                    p_175247_6_.func_178846_a(Minecraft.getSystemTime());
                    p_175247_6_.func_178844_b((long)(Minecraft.getMinecraft().ingameGUI.getUpdateCounter() + 20));
                } else if (i > p_175247_6_.func_178835_l()) {
                    p_175247_6_.func_178846_a(Minecraft.getSystemTime());
                    p_175247_6_.func_178844_b((long)(Minecraft.getMinecraft().ingameGUI.getUpdateCounter() + 10));
                }
            }

            if (Minecraft.getSystemTime() - p_175247_6_.func_178847_n() > 1000L || this.lastTimeOpened != p_175247_6_.func_178855_p()) {
                p_175247_6_.func_178836_b(i);
                p_175247_6_.func_178857_c(i);
                p_175247_6_.func_178846_a(Minecraft.getSystemTime());
            }

            p_175247_6_.func_178843_c(this.lastTimeOpened);
            p_175247_6_.func_178836_b(i);
            int j = MathHelper.ceiling_float_int((float)Math.max(i, p_175247_6_.func_178860_m()) / 2.0F);
            //int j = LabyModCore.getMath().ceiling_float_int((float)Math.max(i, p_175247_6_.func_178860_m()) / 2.0F);
            int k = Math.max(MathHelper.ceiling_float_int((float)(i / 2)), Math.max(MathHelper.ceiling_float_int((float)(p_175247_6_.func_178860_m() / 2)), 10));
            //int k = Math.max(LabyModCore.getMath().ceiling_float_int((float)(i / 2)), Math.max(LabyModCore.getMath().ceiling_float_int((float)(p_175247_6_.func_178860_m() / 2)), 10));
            boolean flag = p_175247_6_.func_178858_o() > (long)Minecraft.getMinecraft().ingameGUI.getUpdateCounter() && (p_175247_6_.func_178858_o() - (long)Minecraft.getMinecraft().ingameGUI.getUpdateCounter()) / 3L % 2L == 1L;
            if (j > 0) {
                float f = Math.min((float)(p_175247_5_ - p_175247_4_ - 4) / (float)k, 9.0F);
                if (f > 3.0F) {
                    int j1;
                    for(j1 = j; j1 < k; ++j1) {
                        Minecraft.getMinecraft().ingameGUI.drawTexturedModalRect((float)p_175247_4_ + (float)j1 * f, (float)p_175247_2_, flag ? 25 : 16, 0, 9, 9);
                    }

                    for(j1 = 0; j1 < j; ++j1) {
                        Minecraft.getMinecraft().ingameGUI.drawTexturedModalRect((float)p_175247_4_ + (float)j1 * f, (float)p_175247_2_, flag ? 25 : 16, 0, 9, 9);
                        if (flag) {
                            if (j1 * 2 + 1 < p_175247_6_.func_178860_m()) {
                                Minecraft.getMinecraft().ingameGUI.drawTexturedModalRect((float)p_175247_4_ + (float)j1 * f, (float)p_175247_2_, 70, 0, 9, 9);
                            }

                            if (j1 * 2 + 1 == p_175247_6_.func_178860_m()) {
                                Minecraft.getMinecraft().ingameGUI.drawTexturedModalRect((float)p_175247_4_ + (float)j1 * f, (float)p_175247_2_, 79, 0, 9, 9);
                            }
                        }

                        if (j1 * 2 + 1 < i) {
                            Minecraft.getMinecraft().ingameGUI.drawTexturedModalRect((float)p_175247_4_ + (float)j1 * f, (float)p_175247_2_, j1 >= 10 ? 160 : 52, 0, 9, 9);
                        }

                        if (j1 * 2 + 1 == i) {
                            Minecraft.getMinecraft().ingameGUI.drawTexturedModalRect((float)p_175247_4_ + (float)j1 * f, (float)p_175247_2_, j1 >= 10 ? 169 : 61, 0, 9, 9);
                        }
                    }
                } else {
                    float f1 = MathHelper.clamp_float((float)i / 20.0F, 0.0F, 1.0F);
                    //float f1 = LabyModCore.getMath().clamp_float((float)i / 20.0F, 0.0F, 1.0F);
                    int i1 = (int)((1.0F - f1) * 255.0F) << 16 | (int)(f1 * 255.0F) << 8;
                    String s = "" + (float)i / 2.0F;
                    if (p_175247_5_ - Minecraft.getMinecraft().fontRendererObj.getStringWidth(s + "hp") >= p_175247_4_) {
                        s = s + "hp";
                    }

                    Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(s, (float)((p_175247_5_ + p_175247_4_) / 2 - Minecraft.getMinecraft().fontRendererObj.getStringWidth(s) / 2), (float)p_175247_2_, i1);
                    //LabyModCore.getMinecraft().getFontRenderer().drawStringWithShadow(s, (float)((p_175247_5_ + p_175247_4_) / 2 - LabyModCore.getMinecraft().getFontRenderer().getStringWidth(s) / 2), (float)p_175247_ 2_, i1);
                }
            }
        } else {
            String s1 = EnumChatFormatting.YELLOW + "" + i;
            Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(s1, (float)(p_175247_5_ - Minecraft.getMinecraft().fontRendererObj.getStringWidth(s1)), (float)p_175247_2_, 16777215);
            //sLabyModCore.getMinecraft().getFontRenderer().drawStringWithShadow(s1, (float)(p_175247_5_ - LabyModCore.getMinecraft().getFontRenderer().getStringWidth(s1)), (float)p_175247_2_, 16777215);
        }
    }

    /**
     * Straight copy of minecraft code (Labymod)
     */
    protected void drawPing(int p_175245_1_, int p_175245_2_, int p_175245_3_, NetworkPlayerInfo networkPlayerInfoIn) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        //DrawUtils draw = LabyMod.getInstance().getDrawUtils();
        GL11.glPushMatrix();
        GlStateManager.scale(0.5D, 0.5D, 0.5D);
        int ping = networkPlayerInfoIn.getResponseTime();
        if (ping >= 1000) {
            ping = 999;
        }

        if (ping < 0) {
            ping = 0;
        }

        //boolean useColors = LabyMod.getSettings().tabPing_colored;
        String c = "GREEN"; //useColors ? "a" : "f";
        //if (useColors) {
        if (ping > 150) {
            c = "DARK_GREEN";
        }

        if (ping > 300) {
            c = "RED";
        }

        if (ping > 600) {
            c = "DARK_RED";
        }
        //}

        String pingStr = EnumChatFormatting.getValueByName(c).toString() + (ping == 0 ? "?" : ping);
        int x = ((p_175245_2_ + p_175245_1_) * 2 - 12) - (Minecraft.getMinecraft().fontRendererObj.getStringWidth(pingStr) / 2);
        Minecraft.getMinecraft().fontRendererObj.drawString(pingStr, x, (p_175245_3_ * 2 + 5), -1);
        //draw.drawCenteredString(ModColor.cl(c) + (ping == 0 ? "?" : ping), (double)((p_175245_2_ + p_175245_1_) * 2 - 12), (double)(p_175245_3_ * 2 + 5));

        GL11.glPopMatrix();
        this.zLevel -= 100.0F;
    }

    /**
     * Straight copy of minecraft code (labymod as well)
     */
    public void updatePlayerList(boolean willBeRendered) {
        if (willBeRendered && !this.isBeingRendered) {
            this.lastTimeOpened = Minecraft.getSystemTime();
        }

        this.isBeingRendered = willBeRendered;
    }
}
