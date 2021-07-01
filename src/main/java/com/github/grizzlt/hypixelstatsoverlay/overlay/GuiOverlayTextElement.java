package com.github.grizzlt.hypixelstatsoverlay.overlay;

import com.github.grizzlt.hypixelstatsoverlay.util.Vector2i;
import net.minecraft.client.Minecraft;
import net.minecraft.util.IChatComponent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.Callable;

public class GuiOverlayTextElement implements IGuiOverlayComponent
{
    private IChatComponent textCache;
    protected Callable<IChatComponent> text = null;

    protected GuiOverlayTextElement() {}

    @Override
    public void prepareForDrawing() throws Exception
    {
        this.textCache = text.call();
    }

    @Override
    public void draw(Vector2i offset, @NotNull Vector2i size)
    {
        if (this.textCache == null) return;

        List<String> lines = Minecraft.getMinecraft().fontRendererObj.listFormattedStringToWidth(this.textCache.getFormattedText(), size.x);
        if (lines.size() > 1) {
            int marginTop = 0;
            for (String line : lines)
            {
                int strWidth = Minecraft.getMinecraft().fontRendererObj.getStringWidth(line);
                Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(line, offset.x + ((size.x - strWidth) / 2.0f), offset.y + marginTop, -1);

                marginTop += Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT;
            }
        } else {
            Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(lines.get(0), offset.x, offset.y, -1);
        }
    }

    @Override
    public int getMaxWidth(@NotNull Vector2i size)
    {
        if (this.textCache == null) return 0;
        return Math.min(size.x, Minecraft.getMinecraft().fontRendererObj.getStringWidth(this.textCache.getFormattedText())) + 2;
    }

    @Override
    public int getMaxHeight(@NotNull Vector2i size)
    {
        if (this.textCache == null) return 0;
        return Minecraft.getMinecraft().fontRendererObj.listFormattedStringToWidth(this.textCache.getFormattedText(), size.x).size() * Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT - 1;
    }

    @Contract(value = " -> new", pure = true)
    public static @NotNull GuiOverlayTextElement create()
    {
        return new GuiOverlayTextElement();
    }

    public GuiOverlayTextElement withText(@NotNull IChatComponent text)
    {
        this.text = () -> text;
        return this;
    }

    public GuiOverlayTextElement withText(@NotNull Callable<IChatComponent> text)
    {
        this.text = text;
        return this;
    }
}
