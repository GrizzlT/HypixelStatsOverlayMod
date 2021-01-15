package com.github.grizzlt.hypixelstatsoverlay.overlay;

import com.github.grizzlt.hypixelstatsoverlay.overlay.builder.GuiOverlayBuilder;
import com.github.grizzlt.hypixelstatsoverlay.util.Vector2i;
import net.minecraft.client.gui.Gui;

import java.util.Optional;

public class GuiOverlayBackgroundRectangle implements IGuiOverlayComponent, GuiOverlayBuilder
{
    protected int color;
    protected int margin;
    protected IGuiOverlayComponent child = null;
    protected Optional<GuiOverlayBuilder> chain = Optional.empty();

    public GuiOverlayBackgroundRectangle(int color, int margin)
    {
        this.color = color;
        this.margin = margin;
    }

    public GuiOverlayBackgroundRectangle(IGuiOverlayComponent child, int color, int margin)
    {
        this.child = child;
        this.color = color;
        this.margin = margin;
    }

    @Override
    public void draw(Vector2i offset, Vector2i size) throws Exception
    {
        Vector2i childSize = new Vector2i(this.getMaxWidth(size), this.getMaxHeight(size)).substract(new Vector2i(margin * 2, margin * 2));
        Gui.drawRect(offset.x, offset.y, offset.x + childSize.x + margin, offset.y + childSize.y + margin, this.color);
        this.child.draw(offset.add(new Vector2i(margin, margin)), childSize);
    }

    @Override
    public int getMaxWidth(Vector2i size)
    {
        return this.child.getMaxWidth(size.substract(new Vector2i(margin * 2, margin * 2))) + margin * 2;
    }

    @Override
    public int getMaxHeight(Vector2i size)
    {
        return this.child.getMaxHeight(size.substract(new Vector2i(margin * 2, margin * 2))) + margin * 2;
    }

    public GuiOverlayBackgroundRectangle setChild(IGuiOverlayComponent child)
    {
        this.child = child;
        return this;
    }

    public GuiOverlayBackgroundRectangle setColor(int color)
    {
        this.color = color;
        return this;
    }

    public GuiOverlayBackgroundRectangle setMargin(int margin)
    {
        this.margin = margin;
        return this;
    }

    public GuiOverlayBackgroundRectangle setBuilder(GuiOverlayBuilder childBuilder)
    {
        this.chain = Optional.of(childBuilder);
        return this;
    }

    @Override
    public IGuiOverlayComponent build() throws Exception
    {
        GuiOverlayBackgroundRectangle newObj = new GuiOverlayBackgroundRectangle(this.color, this.margin);
        if (this.chain.isPresent())
        {
            newObj.setChild(this.chain.get().build());
        }
        return newObj;
    }
}
