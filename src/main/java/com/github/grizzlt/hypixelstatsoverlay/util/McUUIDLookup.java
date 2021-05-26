package com.github.grizzlt.hypixelstatsoverlay.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.util.UUID;

public class McUUIDLookup
{
    private static final HttpClient httpClient = HttpClient.create().secure();
    private static final JsonParser parser = new JsonParser();

    @NotNull
    public static Mono<UUID> getUuidMono(@NotNull String playerName)
    {
        return httpClient.get()
                .uri("https://playerdb.co/api/player/minecraft/" + playerName)
                .responseContent()
                .aggregate()
                .asString()
                .map(parser::parse).map(JsonElement::getAsJsonObject)
                .map(json -> {
                    JsonElement codeObj = json.get("code");
                    if (codeObj != null && codeObj.getAsString().equals("player.found")) {
                        throw new IllegalArgumentException("Request was unsuccessful!");
                    }
                    JsonElement idObj = JsonHelper.getObjectUsingPath(json, "data.player.id");
                    if (idObj == null) {
                        throw new IllegalArgumentException("Request returned unexpected response!");
                    }
                    return idObj.getAsString();
                }).map(UUID::fromString);
    }
}
