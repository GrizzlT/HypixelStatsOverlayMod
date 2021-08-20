package com.github.grizzlt.hypixelstatsoverlay.stats.cache;

public class LobbyBasedCacheExpiry implements CacheExpiry
{
    private int countDown;
    private final ExpiryType expiryType;

    public LobbyBasedCacheExpiry(ExpiryType expiryType, int initCountDown)
    {
        this.countDown = initCountDown;
        this.expiryType = expiryType;
    }

    public void onPlayerChangeWorld(boolean toLobby)
    {
        if (expiryType == ExpiryType.BOTH) {
            this.countDown--;
        } else if (expiryType == ExpiryType.LOBBY && toLobby) {
            this.countDown--;
        } else if (expiryType == ExpiryType.GAME && !toLobby) {
            this.countDown--;
        }
    }

    @Override
    public boolean isExpired()
    {
        return countDown == 0;
    }

    public enum ExpiryType
    {
        LOBBY,
        GAME,
        BOTH
    }
}
