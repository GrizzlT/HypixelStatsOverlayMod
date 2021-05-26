package com.github.grizzlt.hypixelstatsoverlay.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JsonHelper
{
    /**
     * returns null when not found
     */
    @Nullable
    public static JsonElement getObjectUsingPath(@Nullable JsonObject object,@NotNull String path)
    {
        JsonElement jsonElement = object;
        String[] seg = path.split("\\.");
        for (String element : seg) {
            if (jsonElement != null && jsonElement.isJsonObject()) {
                jsonElement = jsonElement.getAsJsonObject().get(element);
            } else {
                return null;
            }
        }
        return jsonElement;
    }
}
