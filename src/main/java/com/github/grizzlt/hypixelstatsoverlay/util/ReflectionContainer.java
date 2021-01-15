package com.github.grizzlt.hypixelstatsoverlay.util;

import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;

public class ReflectionContainer
{
    public static Field headerField;
    public static Field footerField;
    public static Field guiTabOverlayField;

    public static boolean isUsingLaby = false;

    private static boolean initialized = false;

    public static void Init()
    {
        if (initialized) return;
        initialized = true;

        Class<?> guiPlayerTabOverlayClass;
        Class<?> guiIngameClass;
        try {
            Class<?> labyMainClazz = Class.forName("net.labymod.main.LabyMod");
            //labymod active
            System.out.println("Labymod detected!!");
            isUsingLaby = true;
            guiIngameClass = Class.forName("net.labymod.core_implementation.mc18.gui.GuiIngameCustom");
            guiPlayerTabOverlayClass = Class.forName("net.labymod.core_implementation.mc18.gui.ModPlayerTabOverlay");
        } catch (ClassNotFoundException e) {
            //e.printStackTrace();
            //no labymod active
            guiIngameClass = GuiIngame.class;
            guiPlayerTabOverlayClass = GuiPlayerTabOverlay.class;
        }
        headerField = ReflectionHelper.findField(guiPlayerTabOverlayClass, "header", "field_175256_i");
        footerField = ReflectionHelper.findField(guiPlayerTabOverlayClass, "footer", "field_175255_h");
        guiTabOverlayField = ReflectionHelper.findField(guiIngameClass, "overlayPlayerList", "field_175196_v");
    }

    static {
    }
}
