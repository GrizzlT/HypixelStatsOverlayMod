package com.github.ThomasVDP.hypixelmod.statsoverlay;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;

public class KeyBindManager
{
    public static KeyBinding TAB_KEY_BIND;

    public static void init()
    {
        TAB_KEY_BIND = new KeyBinding("key.statsoverlay.show", Keyboard.KEY_NONE, "key.hpstatsoverlay.category");

        ClientRegistry.registerKeyBinding(TAB_KEY_BIND);
    }
}
