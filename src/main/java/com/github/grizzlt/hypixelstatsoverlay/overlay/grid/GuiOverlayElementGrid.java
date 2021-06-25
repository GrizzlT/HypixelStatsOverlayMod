package com.github.grizzlt.hypixelstatsoverlay.overlay.grid;

import com.github.grizzlt.hypixelstatsoverlay.overlay.IGuiOverlayComponent;
import com.github.grizzlt.hypixelstatsoverlay.util.Vector2i;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.stream.IntStream;

@SuppressWarnings("UnusedReturnValue")
public class GuiOverlayElementGrid implements IGuiOverlayComponent
{
    protected IGuiOverlayComponent[] children;
    protected Vector2i dimension;
    protected int[] rowSpacing;
    protected int[] columnSpacing;

    public GuiOverlayElementGrid(@NotNull Vector2i dimension)
    {
        this.dimension = dimension;
        this.children = new IGuiOverlayComponent[dimension.x * dimension.y];
        this.rowSpacing = IntStream.generate(() -> 0).limit(dimension.y - 1).toArray();
        this.columnSpacing = IntStream.generate(() -> 0).limit(dimension.x - 1).toArray();
    }

    @Override
    public void prepareForDrawing() throws Exception
    {
        for (IGuiOverlayComponent child : children)
        {
            child.prepareForDrawing();
        }
    }

    @Override
    public void draw(Vector2i offset, Vector2i size)
    {
        int[] heights = new int[this.dimension.y];
        for (int i = 0; i < this.dimension.y; ++i)
        {
            heights[i] = getRowMaxHeight(i, size);
        }

        int offsetX = 0;
        for (int x = 0; x < this.dimension.x; ++x)
        {
            int offsetY = 0;
            int maxWidth = this.getColumnMaxWidth(x, size);
            for (int y = 0; y < this.dimension.y; ++y)
            {
                this.children[this.getArrayIndexFromPos(x, y)].draw(offset.add(new Vector2i(offsetX, offsetY)), new Vector2i(maxWidth, heights[y]));
                offsetY += heights[y];
                if (y < this.dimension.y - 1) offsetY += rowSpacing[y];
            }
            offsetX += maxWidth;
            if (x < this.dimension.x - 1) offsetX += columnSpacing[x];
        }
    }

    @Override
    public int getMaxWidth(Vector2i size)
    {
        int maxWidth = 0;
        for (int i = 0; i < dimension.x; ++i)
        {
            maxWidth += this.getColumnMaxWidth(i, size);
            if (i < this.dimension.x - 1) maxWidth += this.columnSpacing[i];
        }
        return maxWidth;
    }

    public int getColumnMaxWidth(int column, Vector2i size)
    {
        int maxWidth = 0;
        for (int i = 0; i < dimension.y; ++i)
        {
            maxWidth = Math.max(maxWidth, this.children[this.getArrayIndexFromPos(column, i)].getMaxWidth(size));
        }
        return maxWidth;
    }

    protected int getArrayIndexFromPos(int x, int y) {
        return y * this.dimension.x + x;
    }

    @Override
    public int getMaxHeight(Vector2i size)
    {
        int maxHeight = 0;
        for (int i = 0; i < dimension.y; ++i)
        {
            maxHeight += this.getRowMaxHeight(i, size);
            if (i < this.dimension.y - 1) maxHeight += rowSpacing[i];
        }
        return maxHeight;
    }

    public int getRowMaxHeight(int row, Vector2i size)
    {
        int maxHeight = 0;
        for (int i = 0; i < dimension.x; ++i)
        {
            maxHeight = Math.max(maxHeight, this.children[this.getArrayIndexFromPos(i, row)].getMaxHeight(size));
        }
        return maxHeight;
    }

    @Contract("_ -> new")
    public static @NotNull GuiOverlayElementGrid create(@NotNull Vector2i dimension)
    {
        return new GuiOverlayElementGrid(dimension);
    }

    public GuiOverlayElementGrid withRow(int row, @NotNull IGuiOverlayComponent... elements)
    {
        for (int i = 0; i < dimension.x; ++i)
        {
            this.withChild(row, i, elements[i]);
        }
        return this;
    }

    public GuiOverlayElementGrid withColumn(int column, @NotNull IGuiOverlayComponent... elements)
    {
        for (int i = 0; i < dimension.y; ++i)
        {
            this.withChild(i, column, elements[i]);
        }
        return this;
    }

    /**
     * This method is not checked!! Don't make mistakes please
     */
    public GuiOverlayElementGrid withRowSpacing(int row, int spacing)
    {
        this.rowSpacing[row] = spacing;
        return this;
    }

    /**
     * This method is not checked!!! Don't make mistakes please
     */
    public GuiOverlayElementGrid withColumnSpacing(int column, int spacing)
    {
        this.columnSpacing[column] = spacing;
        return this;
    }

    /**
     * This method is not checked, don't make a mistake as developer please!!
     * @return
     */
    public GuiOverlayElementGrid withChild(int row, int column, @NotNull IGuiOverlayComponent child)
    {
        this.children[this.getArrayIndexFromPos(column, row)] = child;
        return this;
    }
}
