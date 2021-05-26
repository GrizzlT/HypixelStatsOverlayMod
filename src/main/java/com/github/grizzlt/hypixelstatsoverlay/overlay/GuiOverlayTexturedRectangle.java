package com.github.grizzlt.hypixelstatsoverlay.overlay;

import com.github.grizzlt.hypixelstatsoverlay.util.Vector2i;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class GuiOverlayTexturedRectangle implements IGuiOverlayComponent
{
    protected ResourceLocation texture;
    protected Vector2i uv;
    protected Vector2i uvSize;
    protected Vector2i size;
    protected Vector2i tileSize;

    protected GuiOverlayTexturedRectangle() {};

    @Override
    public void prepareForDrawing() throws Exception
    {
        //do nothing
    }

    @Override
    public void draw(Vector2i offset, Vector2i renderSize)
    {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        Minecraft.getMinecraft().getTextureManager().bindTexture(this.texture);
        Gui.drawScaledCustomSizeModalRect(offset.x, offset.y, uv.x, uv.y, uvSize.x, uvSize.y, this.size.x, this.size.y, tileSize.x, tileSize.y);
    }

    @Override
    public int getMaxWidth(Vector2i renderSize)
    {
        return this.size.x;
    }

    @Override
    public int getMaxHeight(Vector2i renderSize)
    {
        return this.size.y;
    }

    @Contract(value = " -> new", pure = true)
    public static @NotNull GuiOverlayTexturedRectangle create()
    {
        return new GuiOverlayTexturedRectangle();
    }

    public GuiOverlayTexturedRectangle withTexture(ResourceLocation newTexture)
    {
        this.texture = newTexture;
        return this;
    }

    public GuiOverlayTexturedRectangle withUV(Vector2i newUV)
    {
        this.uv = newUV;
        return this;
    }

    public GuiOverlayTexturedRectangle withUVSize(Vector2i newUvSize)
    {
        this.uvSize = newUvSize;
        return this;
    }

    public GuiOverlayTexturedRectangle withSize(Vector2i newSize)
    {
        this.size = newSize;
        return this;
    }

    public GuiOverlayTexturedRectangle withTileSize(Vector2i newTileSize)
    {
        this.tileSize = newTileSize;
        return this;
    }
}
