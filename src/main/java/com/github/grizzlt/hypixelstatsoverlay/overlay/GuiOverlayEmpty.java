package com.github.grizzlt.hypixelstatsoverlay.overlay;

import com.github.grizzlt.hypixelstatsoverlay.util.Vector2i;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class GuiOverlayEmpty implements IGuiOverlayComponent
{
    protected GuiOverlayEmpty() {}

    @Override
    public void prepareForDrawing() throws Exception
    {
        //do nothing
    }

    @Override
    public void draw(Vector2i offset, Vector2i size)
    {
        //do nothing
    }

    @Override
    public int getMaxWidth(Vector2i size)
    {
        //empty
        return 0;
    }

    @Override
    public int getMaxHeight(Vector2i size)
    {
        //empty
        return 0;
    }

    @Contract(value = " -> new", pure = true)
    public static @NotNull GuiOverlayEmpty create()
    {
        return new GuiOverlayEmpty();
    }
}
