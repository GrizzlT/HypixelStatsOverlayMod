package com.github.grizzlt.hypixelstatsoverlay.util;

import org.jetbrains.annotations.NotNull;

public class Vector2i
{
    public static final Vector2i ZERO = new Vector2i(0, 0);

    public int x, y;

    public Vector2i(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    public void setX(int newX)
    {
        this.x = newX;
    }

    public void setY(int newY)
    {
        this.y = newY;
    }

    @NotNull
    public Vector2i add(@NotNull Vector2i otherVec)
    {
        return new Vector2i(this.x + otherVec.x, this.y + otherVec.y);
    }

    @NotNull
    public Vector2i add(int x, int y)
    {
        return new Vector2i(this.x + x, this.y + y);
    }

    @NotNull
    public Vector2i subtract(@NotNull Vector2i otherVec)
    {
        return new Vector2i(this.x - otherVec.x, this.y - otherVec.y);
    }

    @NotNull
    public Vector2i subtract(int x, int y)
    {
        return new Vector2i(this.x - x, this.y - y);
    }
}
