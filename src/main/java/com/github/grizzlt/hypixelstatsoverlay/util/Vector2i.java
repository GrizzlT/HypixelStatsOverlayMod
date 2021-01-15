package com.github.grizzlt.hypixelstatsoverlay.util;

public class Vector2i
{
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

    public Vector2i add(Vector2i otherVec)
    {
        return new Vector2i(this.x + otherVec.x, this.y + otherVec.y);
    }

    public Vector2i substract(Vector2i otherVec)
    {
        return new Vector2i(this.x - otherVec.x, this.y - otherVec.y);
    }
}
