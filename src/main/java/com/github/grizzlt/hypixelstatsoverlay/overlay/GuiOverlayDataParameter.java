package com.github.grizzlt.hypixelstatsoverlay.overlay;

public class GuiOverlayDataParameter
{
    public String name;
    public Class<?> dataClass;

    private Object data;

    public GuiOverlayDataParameter(String name, Class<?> dataClass)
    {
        this.name = name;
        this.dataClass = dataClass;
        data = null;
    }

    public <T> GuiOverlayDataParameter(String name, Class<T> dataClass, T obj)
    {
        this.name = name;
        this.dataClass = dataClass;
        this.set(obj, dataClass);
    }

    public <T> T get(Class<T> typeClass)
    {
        if(dataClass.equals(typeClass))
        {
            return typeClass.cast(data);
        }
        return null;
    }

    public <T> T set(T obj, Class<T> typeClass)
    {
        if(dataClass.equals(typeClass))
        {
            T toreturn = typeClass.cast(data);
            data = obj;
            return toreturn;
        }
        return null;
    }
}
