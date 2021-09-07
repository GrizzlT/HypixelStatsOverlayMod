package com.github.grizzlt.hypixelstatsoverlay.stats.cache;

import net.hypixel.api.reply.PlayerReply;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class PlayerDataCacheEntry<E extends CacheExpiry>
{
    private AtomicReference<PlayerReply> playerData = new AtomicReference<>();
    private final E cacheExpiry;

    private AtomicBoolean isPending = new AtomicBoolean(true);
    private final Disposable pendingRequest;

    public PlayerDataCacheEntry(Mono<PlayerReply> playerRequest, E cacheExpiry)
    {
        this.cacheExpiry = cacheExpiry;
        this.pendingRequest = playerRequest.subscribe(reply -> {
            this.playerData.set(reply);
            this.isPending.set(false);
        });
    }

    public void onDisposeEntry()
    {
        this.pendingRequest.dispose();
    }

    public Optional<PlayerReply> getPlayerData()
    {
        return Optional.ofNullable(this.playerData.get());
    }

    public E getCacheExpiry()
    {
        return this.cacheExpiry;
    }

    public boolean isPending()
    {
        return this.isPending.get();
    }
}
