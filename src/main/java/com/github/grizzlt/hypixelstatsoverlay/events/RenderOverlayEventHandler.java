package com.github.grizzlt.hypixelstatsoverlay.events;

import com.github.grizzlt.hypixelstatsoverlay.HypixelStatsOverlayMod;
import com.github.grizzlt.hypixelstatsoverlay.KeyBindManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;

/**
 * The main class for emitting onRenderOverlayEvent to the current GameParser
 */
public class RenderOverlayEventHandler
{
    boolean isOnLabymod = false;
    private final Field guiTabOverlayField;

    public RenderOverlayEventHandler()
    {
        Class<?> guiIngameClass;
        Class<?> guiPlayerTabOverlayClass;
        try {
            Class.forName("net.labymod.main.LabyMod");
            guiIngameClass = Class.forName("net.labymod.core_implementation.mc18.gui.GuiIngameCustom");
            guiPlayerTabOverlayClass = Class.forName("net.labymod.core_implementation.mc18.gui.ModPlayerTabOverlay");
            isOnLabymod = true;
        } catch (ClassNotFoundException e) {
            //e.printStackTrace();
            guiIngameClass = GuiIngame.class;
            guiPlayerTabOverlayClass = GuiPlayerTabOverlay.class;
        }
        this.guiTabOverlayField = ReflectionHelper.findField(guiIngameClass, "overlayPlayerList", "field_175196_v");
    }

    @SubscribeEvent(receiveCanceled = true)
    public void onGameOverlayRenderNoLaby(RenderGameOverlayEvent.Post event)
    {
        if (isOnLabymod) return;

        //System.out.println("Labymod present");
        if (event.type != RenderGameOverlayEvent.ElementType.ALL) return;

        onGameOverlayRender(event);
    }

    @SubscribeEvent(receiveCanceled = true)
    public void onGameOverlayRenderWithLaby(RenderGameOverlayEvent event)
    {
        if (!isOnLabymod) return;

        //System.out.println("No Labymod!");

        onGameOverlayRender(event);
    }

    private void onGameOverlayRender(RenderGameOverlayEvent event)
    {
        if (HypixelStatsOverlayMod.gameParsers.getCurrentGameParser() != null) {
            HypixelStatsOverlayMod.gameParsers.getCurrentGameParser().onRenderGameOverlayEvent(event);
        } else {
            try {
                if (!(Minecraft.getMinecraft().gameSettings.keyBindPlayerList.isKeyDown() || !KeyBindManager.TAB_KEY_BIND.isKeyDown())) {
                    Scoreboard scoreboard = Minecraft.getMinecraft().theWorld.getScoreboard();
                    ScoreObjective scoreObjective = scoreboard.getObjectiveInDisplaySlot(0);
                    ((GuiPlayerTabOverlay)guiTabOverlayField.get(Minecraft.getMinecraft().ingameGUI)).updatePlayerList(true);
                    ((GuiPlayerTabOverlay) guiTabOverlayField.get(Minecraft.getMinecraft().ingameGUI)).renderPlayerlist(event.resolution.getScaledWidth(), scoreboard, scoreObjective);

                } else {
                    ((GuiPlayerTabOverlay)guiTabOverlayField.get(Minecraft.getMinecraft().ingameGUI)).updatePlayerList(false);
                }
            } catch (IllegalAccessException ex) {
                //ex.printStackTrace();
            }
        }
    }
}
