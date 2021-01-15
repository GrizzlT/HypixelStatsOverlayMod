package com.github.grizzlt.hypixelstatsoverlay.overlay;

import com.github.grizzlt.hypixelstatsoverlay.overlay.builder.GuiOverlayBuilder;
import com.github.grizzlt.hypixelstatsoverlay.util.Vector2i;

public class GuiOverlayEmpty implements IGuiOverlayComponent, GuiOverlayBuilder
{
    @Override
    public void draw(Vector2i offset, Vector2i size) throws Exception
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

    @Override
    public IGuiOverlayComponent build() throws Exception
    {
        return this;
    }
}
