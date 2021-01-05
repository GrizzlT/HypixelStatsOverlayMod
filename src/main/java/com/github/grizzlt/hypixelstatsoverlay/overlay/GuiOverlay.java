package com.github.grizzlt.hypixelstatsoverlay.overlay;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;

/**
 * A GUI base class to easily render different parts on the screen
 */
public class GuiOverlay
{
    /**
     * A list of draw actions that have to be executed in order
     */
    protected List<BiConsumer<GuiOverlay, GuiOverlayContext>> onDrawConsumers = new ArrayList<>();
    /**
     * A list of children to allow easier compositing
     */
    protected List<GuiOverlay> children = new ArrayList<>();

    /**
     * The cached maxWidth only calculated once per draw call
     */
    protected int maxWidth = 0;
    /**
     * The cached maxHeight only calculated once per draw call
     */
    protected int maxHeight = 0;
    /**
     * boolean to mark maxWidth as calculated
     */
    protected boolean calculatedMaxWidth = false; //caching for performance!
    /**
     * boolean to mark maxHeight as calculated
     */
    protected boolean calculatedMaxHeight = false;

    /**
     * the calculation lambda for maxWidth (defaults to 0)
     */
    protected BiFunction<? super GuiOverlay, GuiOverlayContext, Integer> maxWidthCalculation = (a, b) -> 0;
    /**
     * the calculation lambda for maxHeight (defaults to 0)
     */
    protected BiFunction<? super GuiOverlay, GuiOverlayContext, Integer> maxHeightCalculation = (a, b) -> 0;

    /**
     * the main draw call!
     * @param context the {@link GuiOverlayContext} containing necessary information
     */
    public void draw(GuiOverlayContext context)
    {
        //first draw ourselves
        for (BiConsumer<GuiOverlay, GuiOverlayContext> consumer : this.onDrawConsumers)
        {
            consumer.accept(this, context);
        }

        //then draw our children
        for (GuiOverlay child : this.children)
        {
            child.draw(context);
        }

        //clear cache
        this.resetSizes(); //reset everything for next draw call
    }

    /**
     * Adds an action to perform when drawing
     * @param action the specified lambda when drawing
     * @return ourselves to continue building
     */
    public GuiOverlay addDrawAction(BiConsumer<GuiOverlay, GuiOverlayContext> action)
    {
        this.onDrawConsumers.add(action);
        return this;
    }

    /**
     * Adds a child to the list with children
     * @param child
     * @return
     */
    public GuiOverlay addChild(GuiOverlay child)
    {
        this.children.add(child);
        return this;
    }

    /**
     * The calculation lamda for the maxWidth
     */
    public GuiOverlay withMaxWidthCalculation(BiFunction<GuiOverlay, GuiOverlayContext, Integer> maxWidthCalculation)
    {
        this.maxWidthCalculation = maxWidthCalculation;
        return this;
    }

    /**
     * The calculation lambda for the maxHeight
     */
    public GuiOverlay withMaxHeightCalculation(BiFunction<GuiOverlay, GuiOverlayContext, Integer> maxHeightCalculation)
    {
        this.maxHeightCalculation = maxHeightCalculation;
        return this;
    }

    /**
     * @param context the current draw context
     * @return the max width of this {@link GuiOverlay} object
     */
    public int getMaxWidth(GuiOverlayContext context)
    {
        if (!this.calculatedMaxWidth) {
            this.maxWidth = this.maxWidthCalculation.apply(this, context);
            this.calculatedMaxWidth = true;
        }
        return this.maxWidth;
    }

    /**
     * @param context the current draw context
     * @return the max height of this {@link GuiOverlay} object
     */
    public int getMaxHeight(GuiOverlayContext context)
    {
        if (!this.calculatedMaxHeight) {
            this.maxHeight = this.maxHeightCalculation.apply(this, context);
            this.calculatedMaxHeight = true;
        }
        return this.maxHeight;
    }

    /**
     * @return all the children of this {@link GuiOverlay} object
     */
    public List<GuiOverlay> getChildren()
    {
        return this.children;
    }

    /**
     * Clears the maxWidth and maxHeight caches
     */
    public void resetSizes()
    {
        this.calculatedMaxHeight = false;
        this.calculatedMaxWidth = false;
        for (GuiOverlay child : children)
        {
            child.resetSizes();
        }
    }

    public static class GuiOverlayContext
    {
        private List<GuiOverlayDataParameter> parameters = new ArrayList<>();

        public <T> void set(String name, Class<T> dataClass, T newValue)
        {
            if (this.parameters.stream().anyMatch(param -> param.name.equals(name)))
            {
                this.parameters.stream().filter(param -> param.name.equals(name)).forEach(param -> param.set(newValue, dataClass));
            } else {
                this.parameters.add(new GuiOverlayDataParameter(name, dataClass, newValue));
            }
        }

        public <T> void update(String name, Class<T> dataClass, UnaryOperator<T> updateFunc)
        {
            if (this.parameters.stream().anyMatch(param -> param.name.equals(name)))
            {
                this.parameters.stream().filter(param -> param.name.equals(name)).forEach(param -> param.set(updateFunc.apply(param.get(dataClass)), dataClass));
            }
        }

        public <T> T get(String name, Class<T> dataClass)
        {
            return this.parameters.stream().filter(parameter -> parameter.name.equals(name)).map(parameter -> parameter.get(dataClass)).findFirst().orElse(null);
        }
    }
}
