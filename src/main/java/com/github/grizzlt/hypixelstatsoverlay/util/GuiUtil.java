package com.github.grizzlt.hypixelstatsoverlay.util;

public class GuiUtil
{
    public static final Vector2i vector8x8 = new Vector2i(8, 8);

    public static int rbgToInt(int r, int g, int b, int a) {
        return  ((a & 0xFF) << 24) |
                ((r & 0xFF) << 16) |
                ((g & 0xFF) << 8)  |
                ((b & 0xFF));
    }
}
