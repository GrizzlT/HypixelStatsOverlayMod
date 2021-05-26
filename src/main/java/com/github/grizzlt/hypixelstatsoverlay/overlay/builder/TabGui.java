package com.github.grizzlt.hypixelstatsoverlay.overlay.builder;

import com.github.grizzlt.hypixelstatsoverlay.overlay.*;
import com.github.grizzlt.hypixelstatsoverlay.overlay.grid.GuiOverlayElementGrid;
import com.github.grizzlt.hypixelstatsoverlay.util.Vector2i;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Callable;

public abstract class TabGui
{
    public static final GuiOverlayEmpty EMPTY = GuiOverlayEmpty.create();

    public static @NotNull GuiOverlayBackgroundRectangle background(int color, int margin)
    {
        return GuiOverlayBackgroundRectangle.create()
                .withColor(color)
                .withMargin(margin);
    }

    public static @NotNull GuiOverlayBackgroundRectangle background(@NotNull IGuiOverlayComponent child, int color, int margin)
    {
        return GuiOverlayBackgroundRectangle.create()
                .withMargin(margin)
                .withColor(color)
                .withChild(child);
    }

    public static @NotNull GuiOverlayBackgroundFill fill(int margin)
    {
        return GuiOverlayBackgroundFill.create()
                .withMargin(margin);
    }

    public static @NotNull GuiOverlayBackgroundFill fill(int color, int margin)
    {
        return GuiOverlayBackgroundFill.create()
                .withColor(color)
                .withMargin(margin);
    }

    public static @NotNull GuiOverlayBackgroundFill fill(Callable<Integer> color)
    {
        return GuiOverlayBackgroundFill.create()
                .withColor(color);
    }

    @Contract(pure = true)
    public static @NotNull GuiOverlayCenteredHorizontal centerHorizontal()
    {
        return GuiOverlayCenteredHorizontal.create();
    }

    public static @NotNull GuiOverlayCenteredHorizontal centerHorizontal(@NotNull IGuiOverlayComponent child)
    {
        return GuiOverlayCenteredHorizontal.create()
                .withChild(child);
    }

    public static @NotNull GuiOverlayElementList verticalList()
    {
        return GuiOverlayElementList.create();
    }

    @Contract(pure = true)
    public static @NotNull GuiOverlayElementRow horizontalList()
    {
        return GuiOverlayElementRow.create();
    }

    public static @NotNull GuiOverlayElementGrid grid(@NotNull Vector2i dimension)
    {
        return GuiOverlayElementGrid.create(dimension);
    }

    @Contract(pure = true)
    public static @NotNull GuiOverlaySpacing spacing()
    {
        return GuiOverlaySpacing.create();
    }

    public static @NotNull GuiOverlaySpacing spacing(@NotNull Vector2i size)
    {
        return GuiOverlaySpacing.create()
                .withMargin(size);
    }

    public static @NotNull GuiOverlayTextElement text(@NotNull IChatComponent text)
    {
        return GuiOverlayTextElement.create()
                .withText(text);
    }

    public static @NotNull GuiOverlayTextElement text(@NotNull Callable<IChatComponent> textSupplier)
    {
        return GuiOverlayTextElement.create()
                .withText(textSupplier);
    }

    public static @NotNull GuiOverlayTexturedRectangle texturedRect(ResourceLocation texture, Vector2i uv, Vector2i uvSize, Vector2i size, Vector2i tileSize)
    {
        return GuiOverlayTexturedRectangle.create()
                .withTexture(texture)
                .withUV(uv)
                .withUVSize(uvSize)
                .withSize(size)
                .withTileSize(tileSize);
    }

    @Contract(pure = true)
    public static @NotNull GuiOverlayStackedElements stacked()
    {
        return GuiOverlayStackedElements.create();
    }
}
