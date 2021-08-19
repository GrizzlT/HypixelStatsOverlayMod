package com.github.grizzlt.hypixelstatsoverlay.config;

import com.github.grizzlt.hypixelstatsoverlay.stats.GameParsers;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Loader;

import java.io.File;

public class ConfigManager
{
    public Configuration configuration;

    public ConfigManager()
    {
        this.configuration = new Configuration(new File(Loader.instance().getConfigDir(), "HypixelStatsMod.cfg"));
        this.configuration.load();
    }

    public void load()
    {
        GameParsers.PARSERS.forEach((serverType, iGameParser) -> iGameParser.loadConfig(configuration));
        this.configuration.save();
    }
}
