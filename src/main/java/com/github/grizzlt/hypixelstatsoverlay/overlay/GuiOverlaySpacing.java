package com.github.grizzlt.hypixelstatsoverlay.overlay;

import com.github.grizzlt.hypixelstatsoverlay.util.Vector2i;

public class GuiOverlaySpacing implements IGuiOverlayComponent
{
    protected Vector2i margin = Vector2i.ZERO;

    protected GuiOverlaySpacing() {}

    @Override
    public void prepareForDrawing() throws Exception
    {
    }

    @Override
    public void draw(Vector2i offset, Vector2i size)
    {
        //skip drawing
    }

    @Override
    public int getMaxWidth(Vector2i size)
    {
        return this.margin.x;
    }

    @Override
    public int getMaxHeight(Vector2i size)
    {
        return this.margin.y;
    }

    public static GuiOverlaySpacing create()
    {
        return new GuiOverlaySpacing();
    }

    public GuiOverlaySpacing withMargin(Vector2i margin)
    {
        this.margin = margin;
        return this;
    }

    public GuiOverlaySpacing withWidth(int width)
    {
        this.margin.setX(width);
        return this;
    }

    public GuiOverlaySpacing withHeight(int height)
    {
        this.margin.setY(height);
        return this;
    }
}
