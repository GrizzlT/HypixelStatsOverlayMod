package com.github.grizzlt.hypixelstatsoverlay.overlay;

import com.github.grizzlt.hypixelstatsoverlay.util.Vector2i;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class GuiOverlayStackedElements implements IGuiOverlayComponent
{
    protected List<IGuiOverlayComponent> children = new ArrayList<>();

    protected GuiOverlayStackedElements() {}

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
        for (IGuiOverlayComponent child : children)
        {
            child.draw(offset, size);
        }
    }

    @Override
    public int getMaxWidth(Vector2i size)
    {
        int maxWidth = 0;
        for (IGuiOverlayComponent child : children)
        {
            maxWidth = Math.max(maxWidth, child.getMaxWidth(size));
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
    public static @NotNull GuiOverlayStackedElements create()
    {
        return new GuiOverlayStackedElements();
    }

    public GuiOverlayStackedElements withChild(@NotNull IGuiOverlayComponent child)
    {
        this.children.add(child);
        return this;
    }
}
