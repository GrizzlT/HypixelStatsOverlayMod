package com.github.grizzlt.hypixelstatsoverlay.overlay.grid;

import com.github.grizzlt.hypixelstatsoverlay.overlay.IGuiOverlayComponent;
import com.github.grizzlt.hypixelstatsoverlay.overlay.builder.GuiOverlayBuilder;
import com.github.grizzlt.hypixelstatsoverlay.util.Vector2i;

import java.util.stream.IntStream;

public class GuiOverlayElementGrid implements IGuiOverlayComponent, GuiOverlayBuilder
{
    protected IGuiOverlayComponent[] children;
    protected GuiOverlayBuilder[] childrenBuilders;
    protected Vector2i dimension;
    protected int[] rowSpacing;
    protected int[] columnSpacing;

    public GuiOverlayElementGrid(Vector2i dimension)
    {
        this.dimension = dimension;
        this.children = new IGuiOverlayComponent[dimension.x * dimension.y];
        this.childrenBuilders = new GuiOverlayBuilder[dimension.x * dimension.y];
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
        int offsetX = 0;
        int[] heights = new int[this.dimension.y];
        for (int i = 0; i < this.dimension.y; ++i)
        {
            heights[i] = getRowMaxHeight(i, size);
        }
        for (int i = 0; i < this.dimension.x; ++i)
        {
            int offsetY = 0;
            int maxWidth = this.getColumnMaxWidth(i, size);
            for (int j = 0; j < this.dimension.y; ++j)
            {
                this.children[j * this.dimension.x + i].draw(offset.add(new Vector2i(offsetX, offsetY)), new Vector2i(maxWidth, heights[j]));
                offsetY += heights[j];
                if (j < this.dimension.y - 1) offsetY += rowSpacing[j];
            }
            offsetX += maxWidth;
            if (i < this.dimension.x - 1) offsetX += columnSpacing[i];
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
            maxWidth = Math.max(maxWidth, this.children[i * this.dimension.x + column].getMaxWidth(size));
        }
        return maxWidth;
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
            maxHeight = Math.max(maxHeight, this.children[row * this.dimension.x + i].getMaxHeight(size));
        }
        return maxHeight;
    }

    public GuiOverlayElementGrid setRow(int row, IGuiOverlayComponent... elements)
    {
        for (int i = 0; i < dimension.x; ++i)
        {
            this.setChild(row, i, elements[i]);
        }
        return this;
    }

    public GuiOverlayElementGrid setColumn(int column, IGuiOverlayComponent... elements)
    {
        for (int i = 0; i < dimension.y; ++i)
        {
            this.setChild(i, column, elements[i]);
        }
        return this;
    }

    /**
     * This method is not checked!! Don't make mistakes please
     */
    public GuiOverlayElementGrid setRowSpacing(int row, int spacing)
    {
        this.rowSpacing[row] = spacing;
        return this;
    }

    /**
     * This method is not checked!!! Don't make mistakes please
     */
    public GuiOverlayElementGrid setColumnSpacing(int column, int spacing)
    {
        this.columnSpacing[column] = spacing;
        return this;
    }

    /**
     * This method is not checked, don't make a mistake as developer please!!
     * @return
     */
    public GuiOverlayElementGrid setChild(int row, int column, IGuiOverlayComponent child)
    {
        this.children[row * this.dimension.x + column] = child;
        return this;
    }

    public GuiOverlayElementGrid setRowBuilders(int row, GuiOverlayBuilder... elements)
    {
        for (int i = 0; i < dimension.x; ++i)
        {
            this.setBuilder(row, i, elements[i]);
        }
        return this;
    }

    public GuiOverlayElementGrid setColumnBuilders(int column, GuiOverlayBuilder... elements)
    {
        for (int i = 0; i < dimension.y; ++i)
        {
            this.setBuilder(i, column, elements[i]);
        }
        return this;
    }

    /**
     * This method is not checked, don't make a mistake as developer please!!
     * @param row
     * @param column
     * @param child
     * @return
     */
    public GuiOverlayElementGrid setBuilder(int row, int column, GuiOverlayBuilder child)
    {
        this.childrenBuilders[row * this.dimension.x + column] = child;
        return this;
    }

    @Override
    public IGuiOverlayComponent build()
    {
        GuiOverlayElementGrid newObj = new GuiOverlayElementGrid(this.dimension);
        for (int i = 0; i < this.childrenBuilders.length; ++i)
        {
            newObj.children[i] = this.childrenBuilders[i].build();
        }
        newObj.rowSpacing = this.rowSpacing.clone();
        newObj.columnSpacing = this.columnSpacing.clone();
        return newObj;
    }
}
