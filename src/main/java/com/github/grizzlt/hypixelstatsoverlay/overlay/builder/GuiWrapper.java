package com.github.grizzlt.hypixelstatsoverlay.overlay.builder;

import com.github.grizzlt.hypixelstatsoverlay.overlay.IGuiOverlayComponent;
import com.github.grizzlt.hypixelstatsoverlay.util.Vector2i;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Callable;

public class GuiWrapper implements IGuiOverlayComponent
{
    protected IGuiOverlayComponent componentCache;
    protected Callable<IGuiOverlayComponent> component;

    protected GuiWrapper() {}

    @Override
    public void prepareForDrawing() throws Exception
    {
        this.componentCache = component.call();
        this.componentCache.prepareForDrawing();
    }

    @Override
    public void draw(Vector2i offset, Vector2i size)
    {
        this.componentCache.draw(offset, size);
    }

    @Override
    public int getMaxWidth(Vector2i size)
    {
        return this.componentCache.getMaxWidth(size);
    }

    @Override
    public int getMaxHeight(Vector2i size)
    {
        return this.componentCache.getMaxHeight(size);
    }

    @Contract(value = " -> new", pure = true)
    public static @NotNull GuiWrapper create()
    {
        return new GuiWrapper();
    }

    public GuiWrapper withChild(IGuiOverlayComponent child)
    {
        this.component = () -> child;
        return this;
    }

    public GuiWrapper withChild(Callable<IGuiOverlayComponent> child)
    {
        this.component = child;
        return this;
    }
}
