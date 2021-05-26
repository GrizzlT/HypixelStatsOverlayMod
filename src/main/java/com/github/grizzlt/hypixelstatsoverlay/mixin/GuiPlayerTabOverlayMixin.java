package com.github.grizzlt.hypixelstatsoverlay.mixin;

import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GuiPlayerTabOverlay.class)
public interface GuiPlayerTabOverlayMixin
{
    @Accessor
    IChatComponent getHeader();

    @Accessor
    IChatComponent getFooter();
}
