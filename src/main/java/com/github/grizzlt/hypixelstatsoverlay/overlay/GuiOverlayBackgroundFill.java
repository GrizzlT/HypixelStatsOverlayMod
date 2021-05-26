package com.github.grizzlt.hypixelstatsoverlay.overlay;

import com.github.grizzlt.hypixelstatsoverlay.util.Vector2i;
import net.minecraft.client.gui.Gui;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Callable;

public class GuiOverlayBackgroundFill extends GuiOverlayBackgroundRectangle
{
    @Override
    public void draw(@NotNull Vector2i offset, Vector2i size)
    {
        Vector2i childSize = new Vector2i(this.getMaxWidth(size), this.getMaxHeight(size)).subtract(margin * 2, margin * 2);
        Gui.drawRect(offset.x, offset.y, offset.x + size.x, offset.y + size.y, this.colorCache);
        this.child.draw(offset.add(margin, margin), childSize);
    }

    @Contract(value = " -> new", pure = true)
    public static @NotNull GuiOverlayBackgroundFill create()
    {
        return new GuiOverlayBackgroundFill();
    }

    @Override
    public GuiOverlayBackgroundFill withChild(@NotNull IGuiOverlayComponent child)
    {
        return (GuiOverlayBackgroundFill)super.withChild(child);
    }

    @Override
    public GuiOverlayBackgroundFill withColor(int color)
    {
        return (GuiOverlayBackgroundFill)super.withColor(color);
    }

    @Override
    public GuiOverlayBackgroundFill withColor(Callable<Integer> color)
    {
        return (GuiOverlayBackgroundFill)super.withColor(color);
    }

    @Override
    public GuiOverlayBackgroundFill withMargin(int margin)
    {
        return (GuiOverlayBackgroundFill)super.withMargin(margin);
    }
}
