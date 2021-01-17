package com.github.grizzlt.hypixelstatsoverlay.overlay;

import com.github.grizzlt.hypixelstatsoverlay.overlay.builder.GuiOverlayBuilder;
import com.github.grizzlt.hypixelstatsoverlay.util.Vector2i;

import java.util.ArrayList;
import java.util.List;

public class GuiOverlayElementRow implements IGuiOverlayComponent, GuiOverlayBuilder
{
    protected List<IGuiOverlayComponent> children = new ArrayList<>();
    protected List<GuiOverlayBuilder> childrenBuilders = new ArrayList<>();

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

    public GuiOverlayElementRow addChild(IGuiOverlayComponent child)
    {
        this.children.add(child);
        return this;
    }

    public GuiOverlayElementRow clear()
    {
        this.children.clear();
        this.childrenBuilders.clear();
        return this;
    }

    public GuiOverlayElementRow addBuilder(GuiOverlayBuilder child)
    {
        this.childrenBuilders.add(child);
        return this;
    }

    @Override
    public IGuiOverlayComponent build()
    {
        GuiOverlayElementRow newObj = new GuiOverlayElementRow();
        for (GuiOverlayBuilder builder : childrenBuilders)
        {
            newObj.addChild(builder.build());
        }
        return newObj;
    }
}
