package com.github.grizzlt.hypixelstatsoverlay.overlay;

import com.github.grizzlt.hypixelstatsoverlay.overlay.builder.GuiOverlayBuilder;
import com.github.grizzlt.hypixelstatsoverlay.util.Vector2i;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class GuiOverlayTexturedRectangle implements IGuiOverlayComponent, GuiOverlayBuilder
{
    protected ResourceLocation texture;
    protected Vector2i uv;
    protected Vector2i uvSize;
    protected Vector2i size;
    protected Vector2i tileSize;

    public GuiOverlayTexturedRectangle(ResourceLocation texture, Vector2i uv, Vector2i uvSize, Vector2i size, Vector2i tileSize)
    {
        this.texture = texture;
        this.uv = uv;
        this.uvSize = uvSize;
        this.size = size;
        this.tileSize = tileSize;
    }

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

    public GuiOverlayTexturedRectangle setTexture(ResourceLocation newTexture)
    {
        this.texture = newTexture;
        return this;
    }

    public GuiOverlayTexturedRectangle setUV(Vector2i newUV)
    {
        this.uv = newUV;
        return this;
    }

    public GuiOverlayTexturedRectangle setUvSize(Vector2i newUvSize)
    {
        this.uvSize = newUvSize;
        return this;
    }

    public GuiOverlayTexturedRectangle setSize(Vector2i newSize)
    {
        this.size = newSize;
        return this;
    }

    public GuiOverlayTexturedRectangle setTileSize(Vector2i newTileSize)
    {
        this.tileSize = newTileSize;
        return this;
    }

    @Override
    public IGuiOverlayComponent build()
    {
        return new GuiOverlayTexturedRectangle(this.texture, this.uv, this.uvSize, this.size, this.tileSize);
    }
}
