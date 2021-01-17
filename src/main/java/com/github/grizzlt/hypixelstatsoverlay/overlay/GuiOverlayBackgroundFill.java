package com.github.grizzlt.hypixelstatsoverlay.overlay;

import com.github.grizzlt.hypixelstatsoverlay.util.Vector2i;
import net.minecraft.client.gui.Gui;

public class GuiOverlayBackgroundFill extends GuiOverlayBackgroundRectangle
{

    public GuiOverlayBackgroundFill(int color, int margin)
    {
        super(color, margin);
    }

    public GuiOverlayBackgroundFill(IGuiOverlayComponent child, int color, int margin)
    {
        super(child, color, margin);
    }

    @Override
    public void draw(Vector2i offset, Vector2i size)
    {
        Vector2i childSize = new Vector2i(this.getMaxWidth(size), this.getMaxHeight(size)).substract(new Vector2i(margin * 2, margin * 2));
        Gui.drawRect(offset.x, offset.y, offset.x + size.x, offset.y + size.y, this.color);
        this.child.draw(offset.add(new Vector2i(margin, margin)), childSize);
    }

    @Override
    public IGuiOverlayComponent build()
    {
        GuiOverlayBackgroundFill newObj = new GuiOverlayBackgroundFill(this.color, this.margin);
        if (this.chain.isPresent())
        {
            newObj.setChild(this.chain.get().build());
        }
        return newObj;
    }
}
