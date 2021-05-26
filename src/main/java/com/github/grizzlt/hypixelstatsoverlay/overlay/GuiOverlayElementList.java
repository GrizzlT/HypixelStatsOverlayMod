package com.github.grizzlt.hypixelstatsoverlay.overlay;

import com.github.grizzlt.hypixelstatsoverlay.util.Vector2i;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class GuiOverlayElementList implements IGuiOverlayComponent
{
    protected List<IGuiOverlayComponent> children = new ArrayList<>();

    protected GuiOverlayElementList() {}

    @Override
    public void prepareForDrawing() throws Exception
    {
        for(IGuiOverlayComponent child : children)
        {
            child.prepareForDrawing();
        }
    }

    @Override
    public void draw(Vector2i offset, Vector2i size)
    {
        Vector2i relativeChildOffset = new Vector2i(0, 0);
        int maxWidth = this.getMaxWidth(size);
        for (IGuiOverlayComponent child : this.children)
        {
            Vector2i childSize = new Vector2i(maxWidth, child.getMaxHeight(size));
            child.draw(offset.add(relativeChildOffset), childSize);
            relativeChildOffset.y += childSize.y;
        }
    }

    @Override
    public int getMaxWidth(Vector2i size)
    {
        int maxWidth = 0;
        for (IGuiOverlayComponent child : children)
        {
            maxWidth = Math.max(child.getMaxWidth(size), maxWidth);
        }
        return maxWidth;
    }

    @Override
    public int getMaxHeight(Vector2i size)
    {
        int maxHeight = 0;
        for (IGuiOverlayComponent child : children)
        {
            maxHeight += child.getMaxHeight(size);
        }
        return maxHeight;
    }

    @Contract(value = " -> new", pure = true)
    public static @NotNull GuiOverlayElementList create()
    {
        return new GuiOverlayElementList();
    }

    public GuiOverlayElementList withChild(@NotNull IGuiOverlayComponent child)
    {
        this.children.add(child);
        return this;
    }
}
