package com.github.grizzlt.hypixelstatsoverlay.overlay;

import com.github.grizzlt.hypixelstatsoverlay.overlay.builder.GuiOverlayBuilder;
import com.github.grizzlt.hypixelstatsoverlay.util.Vector2i;

import java.util.ArrayList;
import java.util.List;

public class GuiOverlayElementList implements IGuiOverlayComponent, GuiOverlayBuilder
{
    protected List<IGuiOverlayComponent> children = new ArrayList<>();
    protected List<GuiOverlayBuilder> childrenBuilders = new ArrayList<>();

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

    public GuiOverlayElementList addChild(IGuiOverlayComponent child)
    {
        this.children.add(child);
        return this;
    }

    public GuiOverlayElementList addBuilder(GuiOverlayBuilder child)
    {
        this.childrenBuilders.add(child);
        return this;
    }

    @Override
    public IGuiOverlayComponent build()
    {
        GuiOverlayElementList newObj = new GuiOverlayElementList();
        for (GuiOverlayBuilder builder : this.childrenBuilders)
        {
            newObj.addChild(builder.build());
        }
        return newObj;
    }
}
