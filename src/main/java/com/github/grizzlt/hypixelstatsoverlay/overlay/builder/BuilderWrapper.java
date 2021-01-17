package com.github.grizzlt.hypixelstatsoverlay.overlay.builder;

import com.github.grizzlt.hypixelstatsoverlay.overlay.IGuiOverlayComponent;

public class BuilderWrapper implements GuiOverlayBuilder
{
    private GuiOverlayBuilder wrapper;

    public BuilderWrapper()
    {
    }

    public BuilderWrapper setBuilder(GuiOverlayBuilder builder)
    {
        this.wrapper = builder;
        return this;
    }

    @Override
    public IGuiOverlayComponent build()
    {
        return this.wrapper.build();
    }
}
