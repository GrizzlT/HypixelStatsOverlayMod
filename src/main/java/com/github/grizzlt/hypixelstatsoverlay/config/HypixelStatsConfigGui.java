package com.github.grizzlt.hypixelstatsoverlay.config;

import com.github.grizzlt.hypixelstatsoverlay.HypixelStatsOverlayMod;
import com.github.grizzlt.hypixelstatsoverlay.Reference;
import com.google.common.collect.Lists;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.config.GuiConfig;

public class HypixelStatsConfigGui extends GuiConfig
{
    public HypixelStatsConfigGui(GuiScreen parentScreen)
    {
        super(parentScreen,
                Lists.newArrayList(new ConfigElement(HypixelStatsOverlayMod.instance.getConfigManager().configuration.getCategory("stats"))),
                Reference.MOD_ID,
                "Main Config",
                false, false, "Hypixel Stats Overlay Config");
    }
}
