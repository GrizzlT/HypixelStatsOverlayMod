package com.github.grizzlt.hypixelstatsoverlay.overlay;

import com.github.grizzlt.hypixelstatsoverlay.overlay.builder.GuiOverlayBuilder;
import com.github.grizzlt.hypixelstatsoverlay.util.Vector2i;
import net.minecraft.client.Minecraft;
import net.minecraft.util.IChatComponent;

import java.util.List;
import java.util.concurrent.Callable;

public class GuiOverlayTextElement implements IGuiOverlayComponent, GuiOverlayBuilder
{
    protected IChatComponent text = null;
    protected Callable<IChatComponent> textCallable;

    public GuiOverlayTextElement(IChatComponent text)
    {
        this(() -> text);
    }

    public GuiOverlayTextElement(Callable<IChatComponent> textCallableIn)
    {
        this.textCallable = textCallableIn;
    }

    @Override
    public void prepareForDrawing() throws Exception
    {
        this.text = this.textCallable.call();
    }

    @Override
    public void draw(Vector2i offset, Vector2i size)
    {
        List<String> lines = Minecraft.getMinecraft().fontRendererObj.listFormattedStringToWidth(this.text.getFormattedText(), size.x);
        int marginTop = 0;
        for (String line : lines)
        {
            int strWidth = Minecraft.getMinecraft().fontRendererObj.getStringWidth(line);
            Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(line, offset.x + ((size.x - strWidth) / 2.0f), offset.y + marginTop, -1);

            marginTop += Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT;
        }
    }

    @Override
    public int getMaxWidth(Vector2i size)
    {
        return Math.min(size.x, Minecraft.getMinecraft().fontRendererObj.getStringWidth(this.text.getFormattedText()));
    }

    @Override
    public int getMaxHeight(Vector2i size)
    {
        return Minecraft.getMinecraft().fontRendererObj.listFormattedStringToWidth(this.text.getFormattedText(), size.x).size() * Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT - 1;
    }

    @Override
    public IGuiOverlayComponent build()
    {
        return new GuiOverlayTextElement(this.textCallable);
    }
}
