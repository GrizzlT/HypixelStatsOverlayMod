package com.github.grizzlt.hypixelstatsoverlay.overlay.builder;

import com.github.grizzlt.hypixelstatsoverlay.overlay.*;
import com.github.grizzlt.hypixelstatsoverlay.overlay.grid.GuiOverlayElementGrid;
import com.github.grizzlt.hypixelstatsoverlay.util.Vector2i;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ResourceLocation;

import java.util.concurrent.Callable;

public interface GuiOverlayBuilder
{
    IGuiOverlayComponent build();

    /**
     * Don't forget to have {@link GuiOverlayBackgroundRectangle#setChild(IGuiOverlayComponent)} called after this!!
     * @param color the color of the background
     * @param margin the margin of the background box
     * @return a background box builder
     */
    static GuiOverlayBackgroundRectangle background(int color, int margin)
    {
        return new GuiOverlayBackgroundRectangle(color, margin);
    }

    static GuiOverlayBackgroundRectangle background(IGuiOverlayComponent child, int color, int margin)
    {
        return new GuiOverlayBackgroundRectangle(child, color, margin);
    }

    static GuiOverlayBackgroundFill fill(int color, int margin)
    {
        return new GuiOverlayBackgroundFill(color, margin);
    }

    /**
     * Don't forget to have {@link GuiOverlayCenteredHorizontal#setChild(IGuiOverlayComponent)} called after this!!
     * @return a centered element builder
     */
    static GuiOverlayCenteredHorizontal centerHorizontal()
    {
        return new GuiOverlayCenteredHorizontal();
    }

    static GuiOverlayCenteredHorizontal centerHorizontal(IGuiOverlayComponent child)
    {
        return new GuiOverlayCenteredHorizontal(child);
    }

    static GuiOverlayElementList verticalList()
    {
        return new GuiOverlayElementList();
    }

    static GuiOverlayElementRow horizontalList()
    {
        return new GuiOverlayElementRow();
    }

    static GuiOverlayElementGrid grid(Vector2i dimension)
    {
        return new GuiOverlayElementGrid(dimension);
    }

    static GuiOverlaySpacing spacing(Vector2i size)
    {
        return new GuiOverlaySpacing(size);
    }

    static GuiOverlayTextElement text(IChatComponent text)
    {
        return new GuiOverlayTextElement(text);
    }

    static GuiOverlayTextElement text(Callable<IChatComponent> textSupp)
    {
        return new GuiOverlayTextElement(textSupp);
    }

    static GuiOverlayTexturedRectangle texturedRect(ResourceLocation texture, Vector2i uv, Vector2i uvSize, Vector2i size, Vector2i tileSize)
    {
        return new GuiOverlayTexturedRectangle(texture, uv, uvSize, size, tileSize);
    }

    static GuiOverlayStackedElements stacked()
    {
        return new GuiOverlayStackedElements();
    }

    static GuiOverlayEmpty empty()
    {
        return new GuiOverlayEmpty();
    }

    /**
     * Without a call to {@link BuilderWrapper#setBuilder(GuiOverlayBuilder)} this is useless!!
     * @return an empty wrapper to get filled in
     */
    static BuilderWrapper wrapper()
    {
        return new BuilderWrapper();
    }

    static BuilderCached cached()
    {
        return new BuilderCached();
    }

    static BuilderCached cached(GuiOverlayBuilder builder)
    {
        return new BuilderCached(builder);
    }
}
