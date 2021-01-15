package com.github.grizzlt.hypixelstatsoverlay.overlay.builder;

import com.github.grizzlt.hypixelstatsoverlay.overlay.IGuiOverlayComponent;

public class BuilderCached implements GuiOverlayBuilder
{
    private boolean built = false;
    private GuiOverlayBuilder builder;
    private IGuiOverlayComponent component;

    public BuilderCached() {}

    public BuilderCached(GuiOverlayBuilder builder)
    {
        this.builder = builder;
    }

    public BuilderCached setBuilder(GuiOverlayBuilder builder)
    {
        this.builder = builder;
        return this;
    }

    @Override
    public IGuiOverlayComponent build() throws Exception
    {
        if (!this.built)
        {
            this.component = builder.build();
        }
        return this.component;
    }
}
