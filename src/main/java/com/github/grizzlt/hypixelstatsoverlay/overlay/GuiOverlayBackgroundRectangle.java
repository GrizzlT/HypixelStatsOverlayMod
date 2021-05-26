package com.github.grizzlt.hypixelstatsoverlay.overlay;

import com.github.grizzlt.hypixelstatsoverlay.util.Vector2i;
import net.minecraft.client.gui.Gui;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Callable;

public class GuiOverlayBackgroundRectangle implements IGuiOverlayComponent
{
    protected int colorCache;
    protected Callable<Integer> color;
    protected int margin = 0;
    protected IGuiOverlayComponent child;

    protected GuiOverlayBackgroundRectangle() {}

    @Override
    public void prepareForDrawing() throws Exception
    {
        this.colorCache = color.call();
        this.child.prepareForDrawing();
    }

    @Override
    public void draw(@NotNull Vector2i offset, Vector2i size)
    {
        Vector2i childSize = new Vector2i(this.getMaxWidth(size), this.getMaxHeight(size)).subtract(margin * 2, margin * 2);
        Gui.drawRect(offset.x, offset.y, offset.x + childSize.x + margin, offset.y + childSize.y + margin, this.colorCache);
        this.child.draw(offset.add(margin, margin), childSize);
    }

    @Override
    public int getMaxWidth(@NotNull Vector2i size)
    {
        return this.child.getMaxWidth(size.subtract(margin * 2, margin * 2)) + margin * 2;
    }

    @Override
    public int getMaxHeight(@NotNull Vector2i size)
    {
        return this.child.getMaxHeight(size.subtract(margin * 2, margin * 2)) + margin * 2;
    }

    @Contract(value = " -> new", pure = true)
    public static @NotNull GuiOverlayBackgroundRectangle create()
    {
        return new GuiOverlayBackgroundRectangle();
    }

    public GuiOverlayBackgroundRectangle withChild(@NotNull IGuiOverlayComponent child)
    {
        this.child = child;
        return this;
    }

    public GuiOverlayBackgroundRectangle withColor(int color)
    {
        this.color = () -> color;
        return this;
    }

    public GuiOverlayBackgroundRectangle withColor(Callable<Integer> color)
    {
        this.color = color;
        return this;
    }

    public GuiOverlayBackgroundRectangle withMargin(int margin)
    {
        this.margin = margin;
        return this;
    }
}
