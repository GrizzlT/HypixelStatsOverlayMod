package com.github.ThomasVDP.hypixelmod.statsoverlay;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;

public class KeyBindManager
{
    public static KeyBinding TAB_KEY_BIND;

    public static void init()
    {
        TAB_KEY_BIND = new KeyBinding("Show player list", Keyboard.KEY_NONE, "HypixelStatsOverlayMod");

        ClientRegistry.registerKeyBinding(TAB_KEY_BIND);
    }
}
