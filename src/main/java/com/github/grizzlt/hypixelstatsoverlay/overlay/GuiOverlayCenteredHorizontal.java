package com.github.grizzlt.hypixelstatsoverlay.overlay;

import com.github.grizzlt.hypixelstatsoverlay.util.Vector2i;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class GuiOverlayCenteredHorizontal implements IGuiOverlayComponent
{
    protected IGuiOverlayComponent child;

    protected GuiOverlayCenteredHorizontal() {}

    @Override
    public void prepareForDrawing() throws Exception
    {
        this.child.prepareForDrawing();
    }

    @Override
    public void draw(@NotNull Vector2i offset, Vector2i size)
    {
        Vector2i childSizeVec = new Vector2i(child.getMaxWidth(size), child.getMaxHeight(size));
        this.child.draw(offset.add((size.x - childSizeVec.x) / 2, 0), childSizeVec);
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

    @Contract(value = " -> new", pure = true)
    public static @NotNull GuiOverlayCenteredHorizontal create()
    {
        return new GuiOverlayCenteredHorizontal();
    }

    public GuiOverlayCenteredHorizontal withChild(@NotNull IGuiOverlayComponent child)
    {
        this.child = child;
        return this;
    }
}
