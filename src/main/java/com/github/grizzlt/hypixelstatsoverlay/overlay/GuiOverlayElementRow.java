package com.github.grizzlt.hypixelstatsoverlay.overlay;

import com.github.grizzlt.hypixelstatsoverlay.util.Vector2i;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class GuiOverlayElementRow implements IGuiOverlayComponent
{
    protected List<IGuiOverlayComponent> children = new ArrayList<>();

    @Override
    public void prepareForDrawing() throws Exception
    {
        for (IGuiOverlayComponent child : children)
        {
            child.prepareForDrawing();
        }
    }

    @Override
    public void draw(Vector2i offset, Vector2i size)
    {
        Vector2i relativeChildOffset = new Vector2i(0, 0);
        int maxHeight = this.getMaxHeight(size);
        for (IGuiOverlayComponent child : this.children)
        {
            Vector2i childSize = new Vector2i(child.getMaxWidth(size), maxHeight);
            child.draw(offset.add(relativeChildOffset), childSize);
            relativeChildOffset.x += childSize.x;
        }
    }

    @Override
    public int getMaxWidth(Vector2i size)
    {
        int maxWidth = 0;
        for (IGuiOverlayComponent child : children)
        {
            maxWidth += child.getMaxWidth(size);
        }
        return maxWidth;
    }

    @Override
    public int getMaxHeight(Vector2i size)
    {
        int maxHeight = 0;
        for (IGuiOverlayComponent child : children)
        {
            maxHeight = Math.max(maxHeight, child.getMaxHeight(size));
        }
        return maxHeight;
    }

    @Contract(value = " -> new", pure = true)
    public static @NotNull GuiOverlayElementRow create()
    {
        return new GuiOverlayElementRow();
    }

    public GuiOverlayElementRow withChild(@NotNull IGuiOverlayComponent child)
    {
        this.children.add(child);
        return this;
    }

    public GuiOverlayElementRow clear()
    {
        this.children.clear();
        return this;
    }
}
