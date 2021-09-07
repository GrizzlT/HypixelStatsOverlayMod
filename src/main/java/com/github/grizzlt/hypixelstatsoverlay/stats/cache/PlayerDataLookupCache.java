package com.github.grizzlt.hypixelstatsoverlay.stats.cache;

import com.github.grizzlt.hypixelstatsoverlay.HypixelStatsOverlayMod;
import com.github.grizzlt.hypixelstatsoverlay.events.PlayerChangedServerWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import reactor.core.publisher.Mono;

import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

public class PlayerDataLookupCache
{
    private final ConcurrentMap<UUID, PlayerDataCacheEntry<LobbyBasedCacheExpiry>> LOBBY_PLAYER_DATA = new ConcurrentHashMap<>();

    public PlayerDataCacheEntry<LobbyBasedCacheExpiry> getOrLookupPlayer(UUID playerUuid, LobbyBasedCacheExpiry cacheExpiry)
    {
        return LOBBY_PLAYER_DATA.computeIfAbsent(playerUuid, uuidToPlayerReply(cacheExpiry));
    }

    @SubscribeEvent
    public void onPlayerChangedServerWorld(PlayerChangedServerWorldEvent event)
    {
        Iterator<PlayerDataCacheEntry<LobbyBasedCacheExpiry>> iterator = LOBBY_PLAYER_DATA.values().iterator();
        while (iterator.hasNext())
        {
            PlayerDataCacheEntry<LobbyBasedCacheExpiry> entry = iterator.next();
            entry.getCacheExpiry().onPlayerChangeWorld(event.isInLobby());

            if (entry.getCacheExpiry().isExpired()) {
                entry.onDisposeEntry();
                iterator.remove();
            }
        }

    }

    private static <E extends CacheExpiry> Function<UUID, PlayerDataCacheEntry<E>> uuidToPlayerReply(E cacheExpiry)
    {
        return uuid -> new PlayerDataCacheEntry<>(
                Mono.fromFuture(HypixelStatsOverlayMod.instance.getHypixelApiMod().handleHypixelAPIRequest(api -> api.getPlayerByUuid(uuid))),
                cacheExpiry
        );
    }
}
