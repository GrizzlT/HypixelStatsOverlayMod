package com.github.grizzlt.hypixelstatsoverlay.overlay;

import com.github.grizzlt.hypixelstatsoverlay.util.Vector2i;

public interface IGuiOverlayComponent
{
    void prepareForDrawing() throws Exception;

    void draw(Vector2i offset, Vector2i size);

    int getMaxWidth(Vector2i size);

    int getMaxHeight(Vector2i size);
}
