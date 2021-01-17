package com.github.grizzlt.hypixelstatsoverlay.overlay;

import com.github.grizzlt.hypixelstatsoverlay.overlay.builder.GuiOverlayBuilder;
import com.github.grizzlt.hypixelstatsoverlay.util.Vector2i;

import java.util.concurrent.Callable;

public class GuiOverlaySpacing implements IGuiOverlayComponent, GuiOverlayBuilder
{
    protected Vector2i margin;
    Callable<Vector2i> marginCallable;

    public GuiOverlaySpacing(Vector2i margin)
    {
        this(() -> margin);
    }

    public GuiOverlaySpacing(Callable<Vector2i> callable)
    {
        this.marginCallable = callable;
    }

    @Override
    public void prepareForDrawing() throws Exception
    {
        this.margin = marginCallable.call();
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

    @Override
    public IGuiOverlayComponent build()
    {
        return new GuiOverlaySpacing(this.marginCallable);
    }
}
