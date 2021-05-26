package com.github.grizzlt.hypixelstatsoverlay.events;

import com.github.grizzlt.hypixelstatsoverlay.HypixelStatsOverlayMod;
import com.github.grizzlt.hypixelstatsoverlay.KeyBindManager;
import com.github.grizzlt.hypixelstatsoverlay.util.ReflectionContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class RenderOverlayEventHandler
{
    @SubscribeEvent(receiveCanceled = true)
    public void onGameOverlayRenderEvent(RenderGameOverlayEvent.Post event)
    {
        ReflectionContainer.Init();
        if (event.type != RenderGameOverlayEvent.ElementType.EXPERIENCE) return;

        onGameOverlayRender(event);
    }

    private void onGameOverlayRender(RenderGameOverlayEvent event)
    {
        if (HypixelStatsOverlayMod.instance.getGameParsers().getCurrentGameParser() != null) {
            HypixelStatsOverlayMod.instance.getGameParsers().getCurrentGameParser().onRenderGameOverlayEvent(event);
        } else {
            try {
                if (!(Minecraft.getMinecraft().gameSettings.keyBindPlayerList.isKeyDown() || !KeyBindManager.TAB_KEY_BIND.isKeyDown())) {
                    Scoreboard scoreboard = Minecraft.getMinecraft().theWorld.getScoreboard();
                    ScoreObjective scoreObjective = scoreboard.getObjectiveInDisplaySlot(0);
                    ((GuiPlayerTabOverlay)ReflectionContainer.guiTabOverlayField.get(Minecraft.getMinecraft().ingameGUI)).updatePlayerList(true);
                    ((GuiPlayerTabOverlay)ReflectionContainer.guiTabOverlayField.get(Minecraft.getMinecraft().ingameGUI)).renderPlayerlist(event.resolution.getScaledWidth(), scoreboard, scoreObjective);

                } else {
                    ((GuiPlayerTabOverlay)ReflectionContainer.guiTabOverlayField.get(Minecraft.getMinecraft().ingameGUI)).updatePlayerList(false);
                }
            } catch (IllegalAccessException ex) {
                //ex.printStackTrace();
            }
        }
    }
}
