package com.github.grizzlt.hypixelstatsoverlay.overlay;

import com.github.grizzlt.hypixelstatsoverlay.overlay.builder.GuiOverlayBuilder;
import com.github.grizzlt.hypixelstatsoverlay.util.Vector2i;

import java.util.Optional;

public class GuiOverlayCenteredHorizontal implements IGuiOverlayComponent, GuiOverlayBuilder
{
    protected IGuiOverlayComponent child;
    protected Optional<GuiOverlayBuilder> chain = Optional.empty();

    public GuiOverlayCenteredHorizontal()
    {
    }

    public GuiOverlayCenteredHorizontal(IGuiOverlayComponent child)
    {
        this.child = child;
    }

    @Override
    public void prepareForDrawing() throws Exception
    {
        this.child.prepareForDrawing();
    }

    @Override
    public void draw(Vector2i offset, Vector2i size)
    {
        Vector2i childSizeVec = new Vector2i(child.getMaxWidth(size), child.getMaxHeight(size));
        this.child.draw(offset.add(new Vector2i((size.x - childSizeVec.x) / 2, 0)), childSizeVec);
    }

    @Override
    public int getMaxWidth(Vector2i size)
    {
        return this.child.getMaxWidth(size);
    }

    @Override
    public int getMaxHeight(Vector2i size)
    {
        return this.child.getMaxHeight(size);
    }

    public GuiOverlayCenteredHorizontal setChild(IGuiOverlayComponent child)
    {
        this.child = child;
        return this;
    }

    public GuiOverlayCenteredHorizontal setBuilder(GuiOverlayBuilder child)
    {
        chain = Optional.of(child);
        return this;
    }

    @Override
    public IGuiOverlayComponent build()
    {
        GuiOverlayCenteredHorizontal newObj = new GuiOverlayCenteredHorizontal();
        if (this.chain.isPresent())
        {
            newObj.setChild(this.chain.get().build());
        }
        return newObj;
    }
}
