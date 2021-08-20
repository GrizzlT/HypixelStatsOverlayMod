package com.github.grizzlt.hypixelstatsoverlay.stats.cache;

import net.hypixel.api.reply.PlayerReply;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.util.Optional;

public class PlayerDataCacheEntry<E extends CacheExpiry>
{
    private PlayerReply playerData = null;
    private final E cacheExpiry;
    private final Disposable pendingRequest;

    public PlayerDataCacheEntry(Mono<PlayerReply> playerRequest, E cacheExpiry)
    {
        this.cacheExpiry = cacheExpiry;
        this.pendingRequest = playerRequest.subscribe(reply -> this.playerData = reply);
    }

    public void onDisposeEntry()
    {
        this.pendingRequest.dispose();
    }

    public Optional<PlayerReply> getPlayerData()
    {
        return Optional.ofNullable(this.playerData);
    }

    public E getCacheExpiry()
    {
        return this.cacheExpiry;
    }
}
